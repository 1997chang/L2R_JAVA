/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.object;

import cn.edu.kmust.utils.FileUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Object_AL_Run {
    
    /**
     * 模型的文件
     */
    private String modelFilePath;
    /**
     * 数据集的文件
     */
    private String dataSetFilePath;
    /**
     * 保存得分结果的文件
     */
    private String savedScoreFilePath;
    /**
     * 某一棵树中的叶子最大数量
     */
    private int maxLeaves=8;
    /**
     * 迭代的次数
     */
    private int rounds=10;
    
    public static void main(String[] args) {
        if(args.length<4){
            System.out.println("Usage: java -cp ./L2R_JAVA/cn.edu.kmust.object.Object_AL_Run <Params>.");
            System.out.println("Params:");
            System.out.println("\t-model <File>\tThe trained model file to load.");
            System.out.println("\t-test <File>\tYou want to evaluate the trained model on this data.");
            System.out.println("\t-maxLeaves <Number>\tYhe max number of leaf of this trained model.[Default = 8 leaves]");
            System.out.println("\t-rounds <Number>\tThe round number of evaluate the dataset.[Default = 10 rounds]");
            System.out.println("\t-save <FIle>\tThe score of dataset save to where?");
            System.exit(-1);
        }
        Object_AL_Run object_AL_Run=new Object_AL_Run();
        for(int i=0;i<args.length;i++){
            if(args[i].compareTo("-model")==0){
                object_AL_Run.modelFilePath=args[++i];
            }else if(args[i].compareTo("-test")==0){
                object_AL_Run.dataSetFilePath=args[++i];
            }else if(args[i].compareTo("-maxLeaves")==0){
                object_AL_Run.maxLeaves=Integer.parseInt(args[++i]);
            }else if(args[i].compareTo("-rounds")==0){
                object_AL_Run.rounds=Integer.parseInt(args[++i]);
            }else if(args[i].compareTo("-save")==0){
                object_AL_Run.savedScoreFilePath=args[++i];
            }
        }
        object_AL_Run.getScore_1();
    }

    private void getScore_1() {
//        -----------------------------------------读取ensemble中的树的开始----------------------------
        BufferedReader in=null;
        //ensemble中树的个数
        int treesSize=0;
        Object_AL []root=null;
        try {
            in=new BufferedReader(new InputStreamReader(new FileInputStream(modelFilePath)));
            treesSize=Integer.parseInt(in.readLine());
            root=new Object_AL[treesSize];
            int nodesSize=maxLeaves<<1;
            for(int t=0;t<treesSize;t++){
                //读取一棵树的深度
                in.readLine();
                Object_AL []pointers=new Object_AL[nodesSize];
                String line;
                //表示某一棵树的下标号
                int subindex=0;
                while(!(line=in.readLine()).equals("end")){
                    //某一个节点的表示服
                    long id;
                    line=line.trim();
                    if(line.length()==0){
                        continue;
                    }
                    String []split=line.split(" ");
                    id=Long.parseLong(split[1]);
                    if(split[0].compareTo("root")==0){
                        int fid=Integer.parseInt(split[2]);
                        float threshold=Float.parseFloat(split[3]);
                        root[t]=new Object_AL(id, fid, threshold);
                        pointers[subindex]=root[t];
                    }else if(split[0].compareTo("node")==0){
                        long pid;
                        int fid;
                        float threshold;
                        int leftChild;
                        pid=Long.parseLong(split[2]);
                        fid=Integer.parseInt(split[3]);
                        leftChild=Integer.parseInt(split[4]);
                        threshold=Float.parseFloat(split[5]);
                        int parentIndex=0;
                        for(;parentIndex<nodesSize;parentIndex++){
                            if(pointers[parentIndex].getId()==pid){
                                break;
                            }
                        }
                        pointers[subindex]=new Object_AL(id, fid, threshold);
                        if(leftChild==1){
                            pointers[parentIndex].setLeft(pointers[subindex]);
                        }else{
                            pointers[parentIndex].setRight(pointers[subindex]);
                        }
                    }else if(split[0].compareTo("leaf")==0){
                        long pid;
                        int leftChild;
                        float value;
                        pid=Long.parseLong(split[2]);
                        leftChild=Integer.parseInt(split[3]);
                        value=Float.parseFloat(split[4]);
                        int parentIndex=0;
                        for(;parentIndex<nodesSize;parentIndex++){
                            if(pointers[parentIndex].getId()==pid){
                                break;
                            }
                        }
                        pointers[subindex]=new Object_AL(id, 0, value);
                        if(leftChild==1){
                            pointers[parentIndex].setLeft(pointers[subindex]);
                        }else{
                            pointers[parentIndex].setRight(pointers[subindex]);
                        }
                    }
                    subindex++;
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Object_AL_Run.class.getName()).log(Level.SEVERE, null, e);
        }finally{
            try {
                if(in!=null){
                    in.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Object_AL_Run.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        ---------------------------------读取ensemble文件结束--------------------------------------
//        ----------------------------------读取数据集文件开始---------------------------------------
        int numberOfInstances=0;
        int numberOfFeatures;
        float [][]features=null;
        try {
            in=new BufferedReader(new InputStreamReader(new FileInputStream(dataSetFilePath)));
            String []split=in.readLine().split(" ");
            numberOfInstances=Integer.parseInt(split[0]);
            numberOfFeatures=Integer.parseInt(split[1]);
            features=new float[numberOfInstances][numberOfFeatures];
            String line;
            int feature_index=0;
            while((line=in.readLine())!=null){
                line=line.trim();
                if(line.length()==0){
                    continue;
                }
                if(line.startsWith("#")){
                    continue;
                }
                if(line.contains("#")){
                    int idx=line.lastIndexOf("#");
                    line=line.substring(0, idx).trim();
                }
                split=line.split(" ");
                for(int i=2;i<split.length;i++){
                    features[feature_index][i-2]=Float.parseFloat(split[i].split(":")[1]);
                }
                feature_index++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Object_AL_Run.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Object_AL_Run.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                if(in!=null){
                    in.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Object_AL_Run.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        ----------------------------------读取数据集文件结束---------------------------------------
//        ----------------------------------计算实例文档的得分开始-------------------------------------
        float []scores=new float[numberOfInstances];
        long start=System.nanoTime();
        for(int r=0;r<rounds;r++){
            Arrays.fill(scores, 0);
            for(int i=0;i<numberOfInstances;i++){
                for(int t=0;t<treesSize;t++){
                    scores[i]+=root[t].getLeaf(features[i]).getThreshold();
                }
            }
        }
        long end=System.nanoTime();
        long total_time=end-start;
        //保存到文件中
        if(savedScoreFilePath.compareTo("")!=0){
            //将得分放入到List<String>中，写入到文件
            List<String> content=new ArrayList<>();
            if(scores!=null){
                for(double s:scores){
                    content.add(s+"");
                }
            }
            //将计算时间加入到内容中
            content.add(rounds+" rounds Tatal Time: "+total_time/1e3+" Microseconds");
            content.add("per rounds Tatal Time: "+(total_time/rounds/1e3)+" Microseconds");
            content.add("per instance total time:"+(total_time/rounds/numberOfInstances/1e3) +" Microseconds");
            FileUtils.writeFile(savedScoreFilePath, content);
        }
    }
    
}
