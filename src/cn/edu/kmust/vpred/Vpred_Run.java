/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.vpred;

import cn.edu.kmust.structplus.StructPlus;
import cn.edu.kmust.structplus.StructPlus_Run;
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
    /**
     * 用于将得分保存到文件中
     */
    private String savedFilePath;
    /**
     * 设置Vpred的向量化操作多少个文档，当V=1的时候就是Pred的方法
     */
    private int V=16;
    
    public static void main(String []args){
        if(args.length<4){
            System.out.println("Usage: java -cp ./L2R_JAVA.jar cn.edu.kmust.vpred.Vpred_Run <Params>.");
            System.out.println("Params:");
            System.out.println("\t-model <File>\tThe trained model file to load.");
            System.out.println("\t-test <File>\tYou want to evaluate the trained model on this data.");
            System.out.println("\t-maxLeaves <Number>\tYhe max number of leaf of this trained model.[Default = 8 leaves]");
            System.out.println("\t-rounds <Number>\tThe round number of evaluate the dataset.[Default = 10 rounds]");
            System.out.println("\t-V <Number>\tThe vectorization level(default V = 16)");
            System.out.println("\t-save <FIle>\tThe score of dataset save to where?");
            System.exit(-1);
        }
        Vpred_Run vpred_Run=new Vpred_Run();
//        vpred_Run.featureFilePath="dataset/msn-5k/v_test1.txt";
//        vpred_Run.modelFilePath="model/Vpred-ensemble-model.txt";
        for(int i=0;i<args.length;i++){
            if(args[i].compareTo("-model")==0){
                vpred_Run.modelFilePath=args[++i];
            }else if(args[i].compareTo("-test")==0){
                vpred_Run.featureFilePath=args[++i];
            }else if(args[i].compareTo("-maxLeaves")==0){
                vpred_Run.maxNumberOfLeaves=Integer.parseInt(args[++i]);
            }else if(args[i].compareTo("-rounds")==0){
                vpred_Run.rounds_num=Integer.parseInt(args[++i]);
            }else if(args[i].compareTo("-save")==0){
                vpred_Run.savedFilePath=args[++i];
            }else if(args[i].compareTo("-V")==0){
                vpred_Run.V=Integer.parseInt(args[++i]);
            }
        }
        vpred_Run.getScore_1();
//        System.out.println(Boolean.compare(false, false));
    }
    
    public void getScore_1(){
        FileReader fileReader=null;
        BufferedReader bufferedReader=null;
//        StructPlus [][]trees=null;
        //节点的总个数,在这个模型中，节点个数之和。（每一棵树的节点个数加在一起）
        long totalNodes=0;
        //每一个树中节点的个数,他是一个一维数组，大小就是树的个数（trees_size）
        //也可以作为all_nodes中的遍历单位。
        int []node_size_tree=null;
        //每一个树的深度，他是一个一维数组，大小就是树的个数（trees_size）
        int []deeps_tree=null;
        //用于表示这个ensmeble中的每一个节点，他是一个二维数组，第一维用于表示他是第几棵树，大小就是trees_size
        //第二维用于表示在当前树下第几个节点。
        Vpred [][]nodes=null;
        //载入模型中树的总个数。（一共多少棵树）
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
            node_size_tree=new int[trees_size];
            deeps_tree=new int[trees_size];
            nodes=new Vpred[trees_size][];
//            最大的节点个数（因为一棵二叉树中节点的个数不会超过  2*叶节点个数）
            int maxTreeSize=maxNumberOfLeaves<<1;
            //用于读取每一棵树中的数据（树的深度，每一个节点的下标值，阈值，还有输出值，左右孩子等等信息）
            //一共遍历trees_size次。因为有trees_size棵树，每一次读取一棵树的信息，并放入到相应的变量中
            for(int i=0;i<trees_size;i++){
                //用于表示pointers中节点的下标（也可用来表示一棵树中节点的个数）
                int subindex=0;
                //读取树的深度
                deeps_tree[i]=Integer.parseInt(bufferedReader.readLine());
                //用于保存每个树中的节点信息
                StructPlus []pointers=new StructPlus[maxTreeSize];
                //表示树的根节点信息
                StructPlus root=null;
                //读取的一行数据信息
                String line;
                while(!(line=bufferedReader.readLine()).equals("end")){
                    //表示节点的标识
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
                        //当前节点要比较的特征下标
                        int fid;
                        //父节点的ID值，也就是标识单位。（就是利用这个值与某一个节点的ID值相比较，从而确定是不是某一个节点是不是当前节点的父节点）
                        long pid;
                        //当前节点要比较的阈值
                        float threshold;
                        //用于表示是不是做左孩子。如果是1的话，表示是左孩子，如果是0的话，表示是猴孩子
                        int leftChild;
                        //表示父节点在pointers中的下标
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
                    }else if(split[0].compareTo("leaf")==0){
                        long pid;
                        int leftChild;
                        //叶子节点的输出值，也就是这棵树的输出值
                        float value;
                        pid=Long.parseLong(split[2]);
                        leftChild=Integer.parseInt(split[3]);
                        value=Float.parseFloat(split[4]);
                        //父节点在pointers中的下标
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
                //一棵树读取完毕
                totalNodes+=subindex;
                node_size_tree[i]=subindex;
                //用于表示第二维数组的大小，也就是再某一棵树下节点的个数
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
        //模型文件读取完毕。
        
//        将nodes二维数组变成一维数组，将节点进行行展开，一棵树中的节点之后紧接着芳龄一颗树中的节点
        Vpred []all_nodes;
        all_nodes=Vpred.changeTwoToOne(nodes, totalNodes,node_size_tree);
        nodes=null;
//        检查读取模型文件是否正确
//        for(int i=0;i<all_nodes.length;i++){
//            all_nodes[i].printtree();
//        }
//        ------------------------------read model file end----------------------------------------
//        ------------------------------read feature file begin------------------------------------
        //是V的整数倍的最好实例个数。
        int divisibleNumberOfInstances=0;
        //一共有多少的文档，也就是实例。
        int numberOfInstances=0;
        //每一个文档的特征值的个数
        int numberOfFeatures=0;
        //用于存放每个文档的特征值，形成一个一维数组。一篇文档的所有特征值紧接着另一篇文档的多有特征值
        float []features=null;
        try {
            fileReader=new FileReader(featureFilePath);
            bufferedReader=new BufferedReader(fileReader);
            //得到实例的个数和特征值的个数的数组，下标为0表示实例的个数，下标为1表示特征值的个数
            String []split=bufferedReader.readLine().split(" ");
            numberOfInstances=Integer.parseInt(split[0]);
            numberOfFeatures=Integer.parseInt(split[1]);
            //将这个变量设置成大于等于numberOfInstances的最小V的倍数。
            divisibleNumberOfInstances=numberOfInstances;
            if(numberOfInstances%V!=0){
                divisibleNumberOfInstances+=V-(numberOfInstances%V);
            }
            //设置feature的大小，也就是最大文档个数*特征值的个数。
            features=new float[divisibleNumberOfInstances*numberOfFeatures];
            //从文件中读取的一行数据信息，也就是特征值的信息
            String line;
            //用于表示features中的下标
            int features_index=0;
            while((line=bufferedReader.readLine())!=null){
                line=line.trim();
                if(line.length()==0){
                    continue;
                }
                //表示注释，从而跳过
                if(line.startsWith("#")){
                    continue;
                }
                //表示一个文档的解释说明部分
                if(line.contains("#")){
                    int idx=line.lastIndexOf("#");
                    line=line.substring(0, idx).trim();
                }
                split=line.split(" ");
                for(int i=2;i<split.length;i++){
                    features[features_index++]=Float.parseFloat(split[i].split(":")[1]);
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
        //用于保存V篇文档在某一棵中的叶子节点的下标（在某一个树的偏移位置，而不是在all_nodes一维数组中的偏移位置，以一棵树的根节点为0开始）。
        int []leaf=new int[V];
        //用于记录各个文档的得分值，一共有divisibleNumberOfInstances个。
        double []score=null;
        long time_begin=System.nanoTime();
        //一共遍历多少轮
        for(int r=0;r<rounds_num;r++){
            //每一轮重新赋值
            score=new double[divisibleNumberOfInstances];
            //遍历所有的文档，每一次遍历V个实例，所以每次i加V，i表示每一次计算V篇文档的第一篇文档的下标号
            for(int i=0;i<numberOfInstances;i+=V){
                //计算V篇文档在所有树中的叶子节点的下标号，一棵树一棵树的计算
                for(int t=0;t<trees_size;t++){
                    switch (V) {
                        case 1:
                            Vpred.findLeaf_1(deeps_tree[t], leaf, numberOfFeatures, i, all_nodes, features, node_size_tree[t]);
                            break;
                        case 2:
                            Vpred.findLeaf_2(deeps_tree[t], leaf, numberOfFeatures, i, all_nodes, features, node_size_tree[t]);
                            break;
                        case 4:
                            Vpred.findLeaf_4(deeps_tree[t], leaf, numberOfFeatures, i, all_nodes, features, node_size_tree[t]);
                            break;
                        case 8:
                            Vpred.findLeaf_8(deeps_tree[t], leaf, numberOfFeatures, i, all_nodes, features, node_size_tree[t]);
                            break;
                        case 16:
                            Vpred.findLeaf_16(deeps_tree[t], leaf, numberOfFeatures, i, all_nodes, features, node_size_tree[t]);
                        case 32:
                            Vpred.findLeaf_32(deeps_tree[t], leaf, numberOfFeatures, i, all_nodes, features, node_size_tree[t]);
                        default:
                            break;
                    }
                    
                    for(int j=0;j<V;j++){
                        score[i+j]+=all_nodes[node_size_tree[t]+leaf[j]].getThreshold();
                    }
                }
            }
        }
        long time_end=System.nanoTime();
//        ------------------------------compute score end--------------------------------------------
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
