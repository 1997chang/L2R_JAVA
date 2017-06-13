/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.structplus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class StructPlus_Run {
    
    /**
     * 循环多少轮
     */
    private int rounds_num=10;
    /**
     * 叶子节点的个数
     */
    private int maxNumberOfLeaves=8;
    /**
     * 模型（决策树）文件路径
     */
    private String modelFilePath;
    /**
     * 查询文档对文件路径
     */
    private String featureFilePath;
    
    public static void main(String []args){
        
        StructPlus_Run structPlus_Run=new StructPlus_Run();
        structPlus_Run.featureFilePath="dataset/msn-5k/v_test1.txt";
        structPlus_Run.modelFilePath="model/Vpred-ensemble-model.txt";
        structPlus_Run.getScore_1();
    }
    
    /**
     * 得到每一个查询文档对的得分的算法1
     */
    public void getScore_1(){
        FileReader fileReader=null;
        BufferedReader bufferedReader=null;
        StructPlus [][]trees=null;
        int trees_size=0;
        try {
            fileReader=new FileReader(modelFilePath);
            bufferedReader=new BufferedReader(fileReader);
//            读取一共有多少个树
            trees_size=Integer.parseInt(bufferedReader.readLine());
            trees=new StructPlus[trees_size][];
//            最大的节点个数
            int maxTreeSize=maxNumberOfLeaves<<1;
            int treeSize;
            for(int i=0;i<trees_size;i++){
                int subindex=0;
                treeSize=Integer.parseInt(bufferedReader.readLine());
                trees[i]=StructPlus.createNodes(maxTreeSize);
                String line;
                while(!(line=bufferedReader.readLine()).equals("end")){
                    String []split=line.split(" ");
                    long id=Integer.parseInt(split[1]);
                    if(split[0].equals("root")){
                        //特征下标
                        int fid=Integer.parseInt(split[2]);
                        //阈值
                        float threshold=Float.parseFloat(split[3]);
                        trees[i][0]=new StructPlus();
                        trees[i][0].setNode(id, fid, threshold);
                    }else if(split[0].equals("node")){
                        //父节点的ID值
                        long pid;
                        int fid;
                        //判断是不是左孩子（1是0不是）
                        int leftChild=0;
                        float threshold;
                        pid=Long.parseLong(split[2]);
                        fid=Integer.parseInt(split[3]);
                        leftChild=Integer.parseInt(split[4]);
                        threshold=Float.parseFloat(split[5]);
                        
                        //发现父节点
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
                    }else{
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
                trees[i]=StructPlus.compress(trees[i]);
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
        int numberOfFeatures=0;
        int numberOfInstances=0;
        float [][]features=null;
        try {
            fileReader=new FileReader(featureFilePath);
            bufferedReader=new BufferedReader(fileReader);
            String []split=bufferedReader.readLine().split(" ");
            numberOfInstances=Integer.parseInt(split[0]);
            numberOfFeatures=Integer.parseInt(split[1]);
            features=new float[numberOfInstances][numberOfFeatures];
            float fvalue;
            String line;
            int instaces_index=0;
            while((line=bufferedReader.readLine())!=null){
                if(line.startsWith("#")){
                    continue;
                }
                if(line.contains("#")){
                    int idx=line.lastIndexOf("#");
                    line=line.substring(0, idx).trim();
                }
                split=line.split(" ");
                for(int i=2;i<split.length;i++){
                    features[instaces_index][i-2]=Float.parseFloat(split[i].split(":")[1]);
                }
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
        double []score=new double[numberOfInstances];
        long time_begin=System.nanoTime();
        for(int i=0;i<rounds_num;i++){
            score=new double[numberOfInstances];
            for(int instance_index=0;instance_index<numberOfInstances;instance_index++){
                for(int tree_index=0;tree_index<trees_size;tree_index++){
                    score[instance_index]+=trees[tree_index][0].getLeaf(features[instance_index]).getThreshold();
                }
            }
        }
        long time_end=System.nanoTime();
        long time_total=time_end-time_begin;
        System.out.println(rounds_num+" rounds Tatal Time: "+time_total+" ns");
        System.out.println("per rounds Tatal Time: "+(time_total/rounds_num)+" ns");
        System.out.println("per instance total time:"+(time_total/rounds_num/numberOfInstances) +"ns");
        
        
//        for(int i=0;i<score.length;i++){
//            System.out.println(score[i]);
//        }
    }
}
