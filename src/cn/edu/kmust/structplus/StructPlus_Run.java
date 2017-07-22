/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.structplus;

import cn.edu.kmust.utils.FileUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class StructPlus_Run {
    
    /**
     * 循环多少轮。对应-rounds的参数
     */
    private int rounds_num=10;
    /**
     * 叶子节点的个数（对应-maxLeaves参数）
     */
    private int maxNumberOfLeaves=8;
    /**
     * 模型（决策树）文件路径(对应-model参数)
     */
    private String modelFilePath;
    /**
     * 查询文档对文件路径（对应-test参数）
     */
    private String featureFilePath;
    /**
     * 用于将得分保存到文件中（对应-save的参数）
     */
    private String savedFilePath;
    
    public static void main(String []args){
        if(args.length<4){
            System.out.println("Usage: java -cp ./L2R_JAVA.jar cn.edu.kmust.structplus.StructPlus_Run <Params>.");
            System.out.println("Params:");
            System.out.println("\t-model <File>\tThe trained model file to load.");
            System.out.println("\t-test <File>\tYou want to evaluate the trained model on this data.");
            System.out.println("\t-maxLeaves <Number>\tYhe max number of leaf of this trained model.[Default = 8 leaves]");
            System.out.println("\t-rounds <Number>\tThe round number of evaluate the dataset.[Default = 10 rounds]");
            System.out.println("\t-save <FIle>\tThe score of dataset save to where?");
            System.exit(-1);
        }
        StructPlus_Run structPlus_Run=new StructPlus_Run();
        for(int i=0;i<args.length;i++){
            if(args[i].compareTo("-model")==0){
                structPlus_Run.modelFilePath=args[++i];
            }else if(args[i].compareTo("-test")==0){
                structPlus_Run.featureFilePath=args[++i];
            }else if(args[i].compareTo("-maxLeaves")==0){
                structPlus_Run.maxNumberOfLeaves=Integer.parseInt(args[++i]);
            }else if(args[i].compareTo("-rounds")==0){
                structPlus_Run.rounds_num=Integer.parseInt(args[++i]);
            }else if(args[i].compareTo("-save")==0){
                structPlus_Run.savedFilePath=args[++i];
            }
        }
//        structPlus_Run.featureFilePath="dataset/msn-5k/v_test1.txt";
//        structPlus_Run.modelFilePath="model/Vpred-ensemble-model.txt";
        structPlus_Run.getScore_1();
    }
    
    /**
     * 得到每一个查询文档对的得分的算法1
     */
    public void getScore_1(){
        FileReader fileReader=null;
        BufferedReader bufferedReader=null;
        //用于保存每一个树中的节点，他是一个二维数组，第一维用于表示树的个数。
        //第二维用于表示某一棵树中的某一个节点信息
        StructPlus [][]trees=null;
        //ensemble中树的个数
        int trees_size=0;
        try {
            fileReader=new FileReader(modelFilePath);
            bufferedReader=new BufferedReader(fileReader);
//            读取一共有多少个树
            trees_size=Integer.parseInt(bufferedReader.readLine());
            if(trees_size<=0){
                System.out.println("The number of trees in the model file is NOT great zero.");
                return ;
            }
            trees=new StructPlus[trees_size][];
//            一棵树中节点个数的最大值
            int maxTreeSize=maxNumberOfLeaves<<1;
            //用于遍历每一棵树，从而将信息放入到trees中
            for(int i=0;i<trees_size;i++){
                //用于表示在某一棵树下，某一个节点的下标号。（也可用来表示一棵树中节点的个数）
                int subindex=0;
                //用于读取某一棵树的深度，在StructPlus中没有作用
                bufferedReader.readLine();
                //创建当前树的所有节点对象
                trees[i]=StructPlus.createNodes(maxTreeSize);
                String line;
                while(!(line=bufferedReader.readLine()).equals("end")){
                    //用于保存一行的内容信息
                    String []split=line.split(" ");
                    //用于表示一个节点的ID标识号，用于识别某一个节点的父节点
                    long id=Integer.parseInt(split[1]);
                    if(split[0].equals("root")){
                        //特征下标
                        int fid=Integer.parseInt(split[2]);
                        //阈值
                        float threshold=Float.parseFloat(split[3]);
                        trees[i][subindex]=new StructPlus();
                        trees[i][subindex].setNode(id, fid, threshold);
                    }else if(split[0].equals("node")){
                        //父节点的ID值
                        long pid;
                        //特征下标
                        int fid;
                        //判断是不是左孩子（1是     0不是）
                        int leftChild=0;
                        float threshold;
                        pid=Long.parseLong(split[2]);
                        fid=Integer.parseInt(split[3]);
                        leftChild=Integer.parseInt(split[4]);
                        threshold=Float.parseFloat(split[5]);
                        
                        //发现父节点在trees[i]中的下标值
                        int parentIndex=0;
                        for(;parentIndex<maxTreeSize;parentIndex++){
                            if(trees[i][parentIndex].getId()==pid){
                                break;
                            }
                        }
                        trees[i][subindex]=new StructPlus();
                        if(leftChild==1){
                            trees[i][parentIndex].setLeft(trees[i][subindex]);
                        }else{
                            trees[i][parentIndex].setRight(trees[i][subindex]);
                        }
                        trees[i][subindex].setNode(id, fid, threshold);
                    }else if(split[0].compareTo("leaf")==0){
                        long pid;
                        int leftChild=0;
                        float value;
                        pid=Long.parseLong(split[2]);
                        leftChild=Integer.parseInt(split[3]);
                        value=Float.parseFloat(split[4]);
                        int parentIndex=0;
                        for(;parentIndex<maxTreeSize;parentIndex++){
                            if(trees[i][parentIndex].getId()==pid){
                                break;
                            }
                        }
                        trees[i][subindex]=new StructPlus();
                        if(leftChild==1){
                            trees[i][parentIndex].setLeft(trees[i][subindex]);
                        }else{
                            trees[i][parentIndex].setRight(trees[i][subindex]);
                        }
                        trees[i][subindex].setNode(id, 0, value);
                    }
                    subindex++;
                }
                trees[i]=StructPlus.compress(trees[i][0]);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StructPlus_Run.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StructPlus_Run.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                if(bufferedReader!=null){
                    bufferedReader.close();
                }
                if(fileReader!=null){
                    fileReader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(StructPlus_Run.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        检查读取模型文件是否正确
//        for(int i=0;i<trees.length;i++){
//            System.out.println(trees[i].length);
//            trees[i][0].printtree();
//        }
//        -------------------------------read model file end-------------------------------------
//        --------------------------------read dataset file begin--------------------------------
        //每一篇文档的特征值的个数
        int numberOfFeatures=0;
        //一共有多少篇文档
        int numberOfInstances=0;
        //用于保存每一篇文档的特征值，他是一个二维数组
        //第一维用于表示文档的下标，第几篇文档的特征值，第二维用于表示特征的下标，第几个下标的特征值
        float [][]features=null;
        try {
            fileReader=new FileReader(featureFilePath);
            bufferedReader=new BufferedReader(fileReader);
            String []split=bufferedReader.readLine().split(" ");
            numberOfInstances=Integer.parseInt(split[0]);
            numberOfFeatures=Integer.parseInt(split[1]);
            features=new float[numberOfInstances][numberOfFeatures];
            //读取到的一行信息
            String line;
            //用于表示features中第一维的下标，一行表示一个文档
            int instaces_index=0;
            while((line=bufferedReader.readLine())!=null){
                line=line.trim();
                if(line.length()==0){
                    continue;
                }
                //表示注释信息
                if(line.startsWith("#")){
                    continue;
                }
                //删除后面的文档的说明部分
                if(line.contains("#")){
                    int idx=line.lastIndexOf("#");
                    line=line.substring(0, idx).trim();
                }
                split=line.split(" ");
                for(int i=2;i<split.length;i++){
                    features[instaces_index][i-2]=Float.parseFloat(split[i].split(":")[1]);
                }
                //一行读取完成，从而换到下一篇文档的下标
                instaces_index++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StructPlus_Run.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(StructPlus_Run.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                if(bufferedReader!=null){
                    bufferedReader.close();
                }
                if(fileReader!=null){
                    fileReader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(StructPlus_Run.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        读取特征文件是否正确
//        for(int i=0;i<numberOfInstances;i++){
//            for(int j=0;j<numberOfFeatures;j++){
//                System.out.print(features[i][j]+"  ");
//            }
//            System.out.println("");
//        }
//        ----------------------------read dataset file end------------------------------------
//        ----------------------------计算每一篇文档的得分-------------------------------------
        double []score=null;
        long time_begin=System.nanoTime();
        //一共迭代多少轮
        for(int i=0;i<rounds_num;i++){
            score=new double[numberOfInstances];
            //遍历所有的文档
            for(int instance_index=0;instance_index<numberOfInstances;instance_index++){
                //遍历所有的树
                for(int tree_index=0;tree_index<trees_size;tree_index++){
                    score[instance_index]+=trees[tree_index][0].getLeaf(features[instance_index]).getThreshold();
                }
            }
        }
        long time_end=System.nanoTime();
        long time_total=time_end-time_begin;
        //保存到文件中
        if(savedFilePath.compareTo("")!=0){
            //将得分放入到List<String>中，写入到文件
            List<String> content=new ArrayList<>();
            if(score!=null){
                for(double s:score){
                    content.add(s+"");
                }
            }
            //将计算时间加入到内容中
            content.add(rounds_num+" rounds Tatal Time: "+time_total/1e3+" Microseconds");
            content.add("per rounds Tatal Time: "+(time_total/rounds_num/1e3)+" Microseconds");
            content.add("per instance total time:"+(time_total/rounds_num/numberOfInstances/1e3) +" Microseconds");
            FileUtils.writeFile(savedFilePath, content);
        }
    }
}
