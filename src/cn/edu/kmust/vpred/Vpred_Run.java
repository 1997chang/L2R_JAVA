/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.vpred;

import cn.edu.kmust.structplus.StructPlus;
import cn.edu.kmust.structplus.StructPlus_Run;
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
public class Vpred_Run {
    
    
    
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
        Vpred_Run vpred_Run=new Vpred_Run();
        vpred_Run.featureFilePath="dataset/msn-5k/v_test1.txt";
        vpred_Run.modelFilePath="model/Vpred-ensemble-model.txt";
        vpred_Run.getScore_1();
//        System.out.println(Boolean.compare(false, false));
    }
    
    public void getScore_1(){
        FileReader fileReader=null;
        BufferedReader bufferedReader=null;
//        StructPlus [][]trees=null;
        //节点的总个数
        long totalNodes=0;
        //每一个树中节点的个数
        int []node_size_tree=null;
        //每一个树的深度
        int []deeps_tree=null;
        Vpred [][]nodes=null;
        //树的个数
        int trees_size=0;
        try {
            fileReader=new FileReader(modelFilePath);
            bufferedReader=new BufferedReader(fileReader);
//            读取一共有多少个树
            trees_size=Integer.parseInt(bufferedReader.readLine());
            node_size_tree=new int[trees_size];
            deeps_tree=new int[trees_size];
            nodes=new Vpred[trees_size][];
//            最大的节点个数
            int maxTreeSize=maxNumberOfLeaves<<1;
            for(int i=0;i<trees_size;i++){
                int subindex=0;
                deeps_tree[i]=Integer.parseInt(bufferedReader.readLine());
                StructPlus []pointers=new StructPlus[maxTreeSize];
                StructPlus root=null;
                String line;
                while(!(line=bufferedReader.readLine()).equals("end")){
                    long id;
                    String []split=line.split(" ");
                    id=Long.parseLong(split[1]);
                    if(split[0].equals("root")){
                        int fid=Integer.parseInt(split[2]);
                        float threshold=Float.parseFloat(split[3]);
                        root=new StructPlus();
                        root.setNode(id, fid, threshold);
                        pointers[subindex]=root;
                    }else if(split[0].equals("node")){
                        int fid;
                        long pid;
                        float threshold;
                        int leftChild=0;
                        int parentIndex=0;
                        pid=Long.parseLong(split[2]);
                        fid=Integer.parseInt(split[3]);
                        leftChild=Integer.parseInt(split[4]);
                        threshold=Float.parseFloat(split[5]);
                        for(;parentIndex<maxTreeSize;parentIndex++){
                            if(pointers[parentIndex].getId()==pid){
                                break;
                            }
                        }
                        pointers[subindex]=new StructPlus();
                        if(leftChild==1){
                            pointers[parentIndex].setLeft(pointers[subindex]);
                        }else{
                            pointers[parentIndex].setRight(pointers[subindex]);
                        }
                        pointers[subindex].setNode(id, fid, threshold);
                    }else{
                        long pid;
                        int leftChild=0;
                        float value;
                        pid=Long.parseLong(split[2]);
                        leftChild=Integer.parseInt(split[3]);
                        value=Float.parseFloat(split[4]);
                        int parentIndex=0;
                        for(;parentIndex<maxTreeSize;parentIndex++){
                            if(pointers[parentIndex].getId()==pid){
                                break;
                            }
                        }
                        pointers[subindex]=new StructPlus();
                        if(leftChild==1){
                            pointers[parentIndex].setLeft(pointers[subindex]);
                        }else{
                            pointers[parentIndex].setRight(pointers[subindex]);
                        }
                        pointers[subindex].setNode(id, 0, value);
                    }
                    subindex++;
                }
                totalNodes+=subindex;
                node_size_tree[i]=subindex;
                nodes[i]=new Vpred[subindex];
                Vpred.createNodes(root, 0, nodes[i]);
                root=null;
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
//        将nodes二维数组变成一维数组
        Vpred []all_nodes;
        all_nodes=Vpred.changeTwoToOne(nodes, totalNodes,node_size_tree);
        int divisibleNumberOfInstances=0;
        nodes=null;
//        检查读取模型文件是否正确
//        for(int i=0;i<all_nodes.length;i++){
//            all_nodes[i].printtree();
//        }
//        ------------------------------read model file end----------------------------------------
//        ------------------------------read feature file begin------------------------------------
        int numberOfInstances=0;
        int numberOfFeatures=0;
        float []features=null;
        try {
            fileReader=new FileReader(featureFilePath);
            bufferedReader=new BufferedReader(fileReader);
            String []split=bufferedReader.readLine().split(" ");
            numberOfInstances=Integer.parseInt(split[0]);
            numberOfFeatures=Integer.parseInt(split[1]);
            divisibleNumberOfInstances=numberOfInstances;
            divisibleNumberOfInstances+=Vpred.V-(numberOfInstances%Vpred.V);
            features=new float[divisibleNumberOfInstances*numberOfFeatures];
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
                    features[instaces_index++]=Float.parseFloat(split[i].split(":")[1]);
                }
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Vpred_Run.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Vpred_Run.class.getName()).log(Level.SEVERE, null, ex);
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
//                System.out.print(features[i*numberOfFeatures+j]+"  ");
//            }
//            System.out.println("");
//        }
//        ------------------------------read feature file end----------------------------------------
//        ------------------------------compute score begin------------------------------------------
        int []leaf=new int[Vpred.V];
        double []score=new double[divisibleNumberOfInstances];
        long time_begin=System.nanoTime();
        for(int r=0;r<rounds_num;r++){
            score=new double[divisibleNumberOfInstances];
            for(int i=0;i<numberOfInstances;i+=Vpred.V){
                for(int t=0;t<trees_size;t++){
                    Vpred.findLeaf(deeps_tree[t], leaf, numberOfFeatures, i, all_nodes, features, node_size_tree[t]);
                    for(int j=0;j<Vpred.V;j++){
                        score[i+j]+=all_nodes[node_size_tree[t]+leaf[j]].getThreshold();
                    }
                }
            }
        }
        long time_end=System.nanoTime();
//        ------------------------------compute score end--------------------------------------------
        long time_total=time_end-time_begin;
        System.out.println(rounds_num+" rounds Tatal Time: "+time_total+" ns");
        System.out.println("per rounds Tatal Time: "+(time_total/rounds_num)+" ns");
        System.out.println("per instance total time:"+(time_total/rounds_num/numberOfInstances) +"ns");
//        for(int i=0;i<numberOfInstances;i++){
//            System.out.println(score[i]);
//        }
    }
    
}
