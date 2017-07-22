/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.vpred;

import cn.edu.kmust.structplus.StructPlus;

/**
 *
 * @author Administrator
 */
public class Vpred {
    //每一个中间节点要和哪一个特征下标比较
    private int feature_id;
    //如果是分支节点的话，表示的内容是阈值，如果是叶子节点的话，表示的是这棵树的输出值
    private float threshold;
    //用于表示当前节点的左右孩子的下标号
    //0表示左孩子的下标值，1表示右孩子的下标值，
    int []children=new int[2];
    
    public Vpred(){
    }
    
    /**
     * 以RLT的方式进行树的遍历
     * 将StructPlus的数据结构转化为Vpred结构
     * 将一颗树放入到Vpred中的各个节点中，从而进行Vpred遍历
     * 将左右节点使用下标还表示
     * @param root 表示一棵树的根节点（他的数据结构是StructPlus）
     * @param i 表示当前节点的下标号（在nodes中的下标号）
     * @param nodes 用于表示一个树中的所有节点
     * @return 最后一个节点使用的下标号
     */
    public static int createNodes(StructPlus root,int i,Vpred []nodes){
        nodes[i]=new Vpred();
        nodes[i].feature_id=root.getFeature_id();
        nodes[i].threshold=root.getThreshold();
        //如果当前节点的左右孩子都为空的话，就进行自循环
        if(root.getLeft()==null&&root.getRight()==null){
            nodes[i].children[0]=i;
            nodes[i].children[1]=i;
        }else{
            nodes[i].children[0]=i+1;
            //递归遍历左子树，返回值是最后一个节点使用的下标值
            int last=createNodes(root.getLeft(), i+1, nodes);
            nodes[i].children[1]=last+1;
            i=createNodes(root.getRight(), last+1, nodes);
        }
        return i;
    }
    
    /**
     * 将一个二维数组Vpred转化为一个一维数组的Vpred，
     * @param nodes 是一个二维数组的对象（也就是源对象）
     * @param totalNodeSize 一共有多少个节点
     * @param node_size_tree 每一个树中节点的个数====》转变为一维数组的偏置单元，用于记录每一棵树的开始下标（all_nodes）
     * @return 转变成的一维数组的对象
     */
    public static Vpred[] changeTwoToOne(Vpred[][] nodes,long totalNodeSize,int []node_size_tree){
        //转变成的一维数组的对象，一共有totalNodeSize个Vpred节点
        Vpred []all_nodes=new Vpred[(int)totalNodeSize];
        //用于表示一维数组的下标号，同时担任每一个树的开始下标号，放入到node_size_tree中
        int all_index=0;
        //树的个数
        int treeSize=nodes.length;
        //遍历所有的树，从而将二维数组转化为一维数组
        for(int i=0;i<treeSize;i++){
            //某一个树中节点的个数，从而遍历树中的所有节点，然后放入一维数组节点的开始下标
            int nsize=node_size_tree[i];
            //转化为某一棵树在all_nodes中的开始下标位置
            node_size_tree[i]=all_index;
            int telement;
            //遍历所有的节点
            for(telement=0;telement<nsize;telement++){
//                all_nodes[all_index]=nodes[i][telement];
                all_nodes[all_index]=new Vpred();
                all_nodes[all_index].feature_id=nodes[i][telement].feature_id;
                all_nodes[all_index].threshold=nodes[i][telement].getThreshold();
                all_nodes[all_index].children[0]=nodes[i][telement].children[0];
                all_nodes[all_index].children[1]=nodes[i][telement].children[1];
                all_index++;
            }
        }
        return all_nodes;
    }

    /**
     * @return the threshold
     */
    public float getThreshold() {
        return threshold;
    }
    
    public void printtree(){
        System.out.println("当前节点feature为:"+feature_id);
        System.out.println("当前节点的阈值为:"+getThreshold());
        System.out.println("当前节点的左孩子下标是："+children[0]);
        System.out.println("当前节点的右孩子下标是："+children[1]);
    }
        
    /**
     * 用于发现各个文档（V=1）在同一个树下的叶子节点的下标号
     * @param deep 表示树的深度，从而确定循环多少次就会到叶子节点
     * @param leaf 用于表示V篇文档叶子节点的下标值（在某一棵中，而不是在all_nodes的下标中）
     * @param numberOfFeatures 一篇文档的特征数。
     * @param numberOfinstances 用来表示V篇文档中第一篇文档的下标号，他与上一个变量乘积确定features数组中第一篇文档的下标号
     * @param nodes ensemble中的所有节点。nodes[shift]就是要遍历一个树的根节点
     * @param features 所有文档的特征值。
     * @param shift 用于确定nodes第一个节点（某一棵树的根节点）
     */
    public static void findLeaf_1(int deep,int []leaf,int numberOfFeatures,int numberOfinstances,final Vpred []nodes,final float []features,int shift){
        leaf[0]=nodes[shift].children[features[numberOfinstances*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        for(int d=2;d<=deep;d++){
            leaf[0]=nodes[shift+leaf[0]].children[features[numberOfinstances*numberOfFeatures+nodes[shift+leaf[0]].feature_id]>nodes[shift+leaf[0]].threshold ? 1 : 0];
        }
    }
    
    /**
     * 用于发现各个文档（V=2）在同一个树下的叶子节点的下标号
     * @param deep 表示树的深度，从而确定循环多少次就会到叶子节点
     * @param leaf 用于表示V篇文档叶子节点的下标值（在某一棵中，而不是在all_nodes的下标中）
     * @param numberOfFeatures 一篇文档的特征数。
     * @param numberOfinstances 用来表示V篇文档中第一篇文档的下标号，他与上一个变量乘积确定features数组中第一篇文档的下标号
     * @param nodes ensemble中的所有节点。nodes[shift]就是要遍历一个树的根节点
     * @param features 所有文档的特征值。
     * @param shift 用于确定nodes第一个节点（某一棵树的根节点）
     */
    public static void findLeaf_2(int deep,int []leaf,int numberOfFeatures,int numberOfinstances,final Vpred []nodes,final float []features,int shift){
        leaf[0]=nodes[shift].children[features[numberOfinstances*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[1]=nodes[shift].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        for(int d=2;d<=deep;d++){
            leaf[0]=nodes[shift+leaf[0]].children[features[numberOfinstances*numberOfFeatures+nodes[shift+leaf[0]].feature_id]>nodes[shift+leaf[0]].threshold ? 1 : 0];
            leaf[1]=nodes[shift+leaf[1]].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift+leaf[1]].feature_id]>nodes[shift+leaf[1]].threshold ? 1 : 0];
        }
    }
    
    /**
     * 用于发现各个文档（V=4）在同一个树下的叶子节点的下标号
     * @param deep 表示树的深度，从而确定循环多少次就会到叶子节点
     * @param leaf 用于表示V篇文档叶子节点的下标值（在某一棵中，而不是在all_nodes的下标中）
     * @param numberOfFeatures 一篇文档的特征数。
     * @param numberOfinstances 用来表示V篇文档中第一篇文档的下标号，他与上一个变量乘积确定features数组中第一篇文档的下标号
     * @param nodes ensemble中的所有节点。nodes[shift]就是要遍历一个树的根节点
     * @param features 所有文档的特征值。
     * @param shift 用于确定nodes第一个节点（某一棵树的根节点）
     */
    public static void findLeaf_4(int deep,int []leaf,int numberOfFeatures,int numberOfinstances,final Vpred []nodes,final float []features,int shift){
        leaf[0]=nodes[shift].children[features[numberOfinstances*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[1]=nodes[shift].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[2]=nodes[shift].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[3]=nodes[shift].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        for(int d=2;d<=deep;d++){
            leaf[0]=nodes[shift+leaf[0]].children[features[numberOfinstances*numberOfFeatures+nodes[shift+leaf[0]].feature_id]>nodes[shift+leaf[0]].threshold ? 1 : 0];
            leaf[1]=nodes[shift+leaf[1]].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift+leaf[1]].feature_id]>nodes[shift+leaf[1]].threshold ? 1 : 0];
            leaf[2]=nodes[shift+leaf[2]].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift+leaf[2]].feature_id]>nodes[shift+leaf[2]].threshold ? 1 : 0];
            leaf[3]=nodes[shift+leaf[3]].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift+leaf[3]].feature_id]>nodes[shift+leaf[3]].threshold ? 1 : 0];
        }
    }
    
    /**
     * 用于发现各个文档（V=8）在同一个树下的叶子节点的下标号
     * @param deep 表示树的深度，从而确定循环多少次就会到叶子节点
     * @param leaf 用于表示V篇文档叶子节点的下标值（在某一棵中，而不是在all_nodes的下标中）
     * @param numberOfFeatures 一篇文档的特征数。
     * @param numberOfinstances 用来表示V篇文档中第一篇文档的下标号，他与上一个变量乘积确定features数组中第一篇文档的下标号
     * @param nodes ensemble中的所有节点。nodes[shift]就是要遍历一个树的根节点
     * @param features 所有文档的特征值。
     * @param shift 用于确定nodes第一个节点（某一棵树的根节点）
     */
    public static void findLeaf_8(int deep,int []leaf,int numberOfFeatures,int numberOfinstances,final Vpred []nodes,final float []features,int shift){
        leaf[0]=nodes[shift].children[features[numberOfinstances*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[1]=nodes[shift].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[2]=nodes[shift].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[3]=nodes[shift].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[4]=nodes[shift].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[5]=nodes[shift].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[6]=nodes[shift].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[7]=nodes[shift].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        for(int i=2;i<=deep;i++){
            leaf[0]=nodes[shift+leaf[0]].children[features[numberOfinstances*numberOfFeatures+nodes[shift+leaf[0]].feature_id]>nodes[shift+leaf[0]].threshold ? 1 : 0];
            leaf[1]=nodes[shift+leaf[1]].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift+leaf[1]].feature_id]>nodes[shift+leaf[1]].threshold ? 1 : 0];
            leaf[2]=nodes[shift+leaf[2]].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift+leaf[2]].feature_id]>nodes[shift+leaf[2]].threshold ? 1 : 0];
            leaf[3]=nodes[shift+leaf[3]].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift+leaf[3]].feature_id]>nodes[shift+leaf[3]].threshold ? 1 : 0];
            leaf[4]=nodes[shift+leaf[4]].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift+leaf[4]].feature_id]>nodes[shift+leaf[4]].threshold ? 1 : 0];
            leaf[5]=nodes[shift+leaf[5]].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift+leaf[5]].feature_id]>nodes[shift+leaf[5]].threshold ? 1 : 0];
            leaf[6]=nodes[shift+leaf[6]].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift+leaf[6]].feature_id]>nodes[shift+leaf[6]].threshold ? 1 : 0];
            leaf[7]=nodes[shift+leaf[7]].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift+leaf[7]].feature_id]>nodes[shift+leaf[7]].threshold ? 1 : 0];
        }
    }
    
    /**
     * 用于发现各个文档（V=16）在同一个树下的叶子节点的下标号
     * @param deep 表示树的深度，从而确定循环多少次就会到叶子节点
     * @param leaf 用于表示V篇文档叶子节点的下标值（在某一棵中，而不是在all_nodes的下标中）
     * @param numberOfFeatures 一篇文档的特征数。
     * @param numberOfinstances 用来表示V篇文档中第一篇文档的下标号，他与上一个变量乘积确定features数组中第一篇文档的下标号
     * @param nodes ensemble中的所有节点。nodes[shift]就是要遍历一个树的根节点
     * @param features 所有文档的特征值。
     * @param shift 用于确定nodes第一个节点（某一棵树的根节点）
     */
    public static void findLeaf_16(int deep,int []leaf,int numberOfFeatures,int numberOfinstances,final Vpred []nodes,final float []features,int shift){
        leaf[0]=nodes[shift].children[features[numberOfinstances*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[1]=nodes[shift].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[2]=nodes[shift].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[3]=nodes[shift].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[4]=nodes[shift].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[5]=nodes[shift].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[6]=nodes[shift].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[7]=nodes[shift].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[8]=nodes[shift].children[features[(numberOfinstances+8)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[9]=nodes[shift].children[features[(numberOfinstances+9)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[10]=nodes[shift].children[features[(numberOfinstances+10)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[11]=nodes[shift].children[features[(numberOfinstances+11)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[12]=nodes[shift].children[features[(numberOfinstances+12)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[13]=nodes[shift].children[features[(numberOfinstances+13)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[14]=nodes[shift].children[features[(numberOfinstances+14)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[15]=nodes[shift].children[features[(numberOfinstances+15)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        for(int i=2;i<=deep;i++){
            leaf[0]=nodes[shift+leaf[0]].children[features[numberOfinstances*numberOfFeatures+nodes[shift+leaf[0]].feature_id]>nodes[shift+leaf[0]].threshold ? 1 : 0];
            leaf[1]=nodes[shift+leaf[1]].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift+leaf[1]].feature_id]>nodes[shift+leaf[1]].threshold ? 1 : 0];
            leaf[2]=nodes[shift+leaf[2]].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift+leaf[2]].feature_id]>nodes[shift+leaf[2]].threshold ? 1 : 0];
            leaf[3]=nodes[shift+leaf[3]].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift+leaf[3]].feature_id]>nodes[shift+leaf[3]].threshold ? 1 : 0];
            leaf[4]=nodes[shift+leaf[4]].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift+leaf[4]].feature_id]>nodes[shift+leaf[4]].threshold ? 1 : 0];
            leaf[5]=nodes[shift+leaf[5]].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift+leaf[5]].feature_id]>nodes[shift+leaf[5]].threshold ? 1 : 0];
            leaf[6]=nodes[shift+leaf[6]].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift+leaf[6]].feature_id]>nodes[shift+leaf[6]].threshold ? 1 : 0];
            leaf[7]=nodes[shift+leaf[7]].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift+leaf[7]].feature_id]>nodes[shift+leaf[7]].threshold ? 1 : 0];
            leaf[8]=nodes[shift+leaf[8]].children[features[(numberOfinstances+8)*numberOfFeatures+nodes[shift+leaf[8]].feature_id]>nodes[shift+leaf[8]].threshold ? 1 : 0];
            leaf[9]=nodes[shift+leaf[9]].children[features[(numberOfinstances+9)*numberOfFeatures+nodes[shift+leaf[9]].feature_id]>nodes[shift+leaf[9]].threshold ? 1 : 0];
            leaf[10]=nodes[shift+leaf[10]].children[features[(numberOfinstances+10)*numberOfFeatures+nodes[shift+leaf[10]].feature_id]>nodes[shift+leaf[10]].threshold ? 1 : 0];
            leaf[11]=nodes[shift+leaf[11]].children[features[(numberOfinstances+11)*numberOfFeatures+nodes[shift+leaf[11]].feature_id]>nodes[shift+leaf[11]].threshold ? 1 : 0];
            leaf[12]=nodes[shift+leaf[12]].children[features[(numberOfinstances+12)*numberOfFeatures+nodes[shift+leaf[12]].feature_id]>nodes[shift+leaf[12]].threshold ? 1 : 0];
            leaf[13]=nodes[shift+leaf[13]].children[features[(numberOfinstances+13)*numberOfFeatures+nodes[shift+leaf[13]].feature_id]>nodes[shift+leaf[13]].threshold ? 1 : 0];
            leaf[14]=nodes[shift+leaf[14]].children[features[(numberOfinstances+14)*numberOfFeatures+nodes[shift+leaf[14]].feature_id]>nodes[shift+leaf[14]].threshold ? 1 : 0];
            leaf[15]=nodes[shift+leaf[15]].children[features[(numberOfinstances+15)*numberOfFeatures+nodes[shift+leaf[15]].feature_id]>nodes[shift+leaf[15]].threshold ? 1 : 0];
        }
    }
    
    /**
     * 用于发现各个文档（V=32）在同一个树下的叶子节点的下标号
     * @param deep 表示树的深度，从而确定循环多少次就会到叶子节点
     * @param leaf 用于表示V篇文档叶子节点的下标值（在某一棵中，而不是在all_nodes的下标中）
     * @param numberOfFeatures 一篇文档的特征数。
     * @param numberOfinstances 用来表示V篇文档中第一篇文档的下标号，他与上一个变量乘积确定features数组中第一篇文档的下标号
     * @param nodes ensemble中的所有节点。nodes[shift]就是要遍历一个树的根节点
     * @param features 所有文档的特征值。
     * @param shift 用于确定nodes第一个节点（某一棵树的根节点）
     */
    public static void findLeaf_32(int deep,int []leaf,int numberOfFeatures,int numberOfinstances,final Vpred []nodes,final float []features,int shift){
        leaf[0]=nodes[shift].children[features[numberOfinstances*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[1]=nodes[shift].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[2]=nodes[shift].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[3]=nodes[shift].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[4]=nodes[shift].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[5]=nodes[shift].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[6]=nodes[shift].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[7]=nodes[shift].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[8]=nodes[shift].children[features[(numberOfinstances+8)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[9]=nodes[shift].children[features[(numberOfinstances+9)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[10]=nodes[shift].children[features[(numberOfinstances+10)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[11]=nodes[shift].children[features[(numberOfinstances+11)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[12]=nodes[shift].children[features[(numberOfinstances+12)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[13]=nodes[shift].children[features[(numberOfinstances+13)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[14]=nodes[shift].children[features[(numberOfinstances+14)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[15]=nodes[shift].children[features[(numberOfinstances+15)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[16]=nodes[shift].children[features[(numberOfinstances+16)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[17]=nodes[shift].children[features[(numberOfinstances+17)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[18]=nodes[shift].children[features[(numberOfinstances+18)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[19]=nodes[shift].children[features[(numberOfinstances+19)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[20]=nodes[shift].children[features[(numberOfinstances+20)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[21]=nodes[shift].children[features[(numberOfinstances+21)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[22]=nodes[shift].children[features[(numberOfinstances+22)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[23]=nodes[shift].children[features[(numberOfinstances+23)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[24]=nodes[shift].children[features[(numberOfinstances+24)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[25]=nodes[shift].children[features[(numberOfinstances+25)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[26]=nodes[shift].children[features[(numberOfinstances+26)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[27]=nodes[shift].children[features[(numberOfinstances+27)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[28]=nodes[shift].children[features[(numberOfinstances+28)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[29]=nodes[shift].children[features[(numberOfinstances+29)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[30]=nodes[shift].children[features[(numberOfinstances+30)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[31]=nodes[shift].children[features[(numberOfinstances+31)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        for(int i=2;i<=deep;i++){
            leaf[0]=nodes[shift+leaf[0]].children[features[numberOfinstances*numberOfFeatures+nodes[shift+leaf[0]].feature_id]>nodes[shift+leaf[0]].threshold ? 1 : 0];
            leaf[1]=nodes[shift+leaf[1]].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift+leaf[1]].feature_id]>nodes[shift+leaf[1]].threshold ? 1 : 0];
            leaf[2]=nodes[shift+leaf[2]].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift+leaf[2]].feature_id]>nodes[shift+leaf[2]].threshold ? 1 : 0];
            leaf[3]=nodes[shift+leaf[3]].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift+leaf[3]].feature_id]>nodes[shift+leaf[3]].threshold ? 1 : 0];
            leaf[4]=nodes[shift+leaf[4]].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift+leaf[4]].feature_id]>nodes[shift+leaf[4]].threshold ? 1 : 0];
            leaf[5]=nodes[shift+leaf[5]].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift+leaf[5]].feature_id]>nodes[shift+leaf[5]].threshold ? 1 : 0];
            leaf[6]=nodes[shift+leaf[6]].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift+leaf[6]].feature_id]>nodes[shift+leaf[6]].threshold ? 1 : 0];
            leaf[7]=nodes[shift+leaf[7]].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift+leaf[7]].feature_id]>nodes[shift+leaf[7]].threshold ? 1 : 0];
            leaf[8]=nodes[shift+leaf[8]].children[features[(numberOfinstances+8)*numberOfFeatures+nodes[shift+leaf[8]].feature_id]>nodes[shift+leaf[8]].threshold ? 1 : 0];
            leaf[9]=nodes[shift+leaf[9]].children[features[(numberOfinstances+9)*numberOfFeatures+nodes[shift+leaf[9]].feature_id]>nodes[shift+leaf[9]].threshold ? 1 : 0];
            leaf[10]=nodes[shift+leaf[10]].children[features[(numberOfinstances+10)*numberOfFeatures+nodes[shift+leaf[10]].feature_id]>nodes[shift+leaf[10]].threshold ? 1 : 0];
            leaf[11]=nodes[shift+leaf[11]].children[features[(numberOfinstances+11)*numberOfFeatures+nodes[shift+leaf[11]].feature_id]>nodes[shift+leaf[11]].threshold ? 1 : 0];
            leaf[12]=nodes[shift+leaf[12]].children[features[(numberOfinstances+12)*numberOfFeatures+nodes[shift+leaf[12]].feature_id]>nodes[shift+leaf[12]].threshold ? 1 : 0];
            leaf[13]=nodes[shift+leaf[13]].children[features[(numberOfinstances+13)*numberOfFeatures+nodes[shift+leaf[13]].feature_id]>nodes[shift+leaf[13]].threshold ? 1 : 0];
            leaf[14]=nodes[shift+leaf[14]].children[features[(numberOfinstances+14)*numberOfFeatures+nodes[shift+leaf[14]].feature_id]>nodes[shift+leaf[14]].threshold ? 1 : 0];
            leaf[15]=nodes[shift+leaf[15]].children[features[(numberOfinstances+15)*numberOfFeatures+nodes[shift+leaf[15]].feature_id]>nodes[shift+leaf[15]].threshold ? 1 : 0];
            leaf[16]=nodes[shift+leaf[16]].children[features[(numberOfinstances+16)*numberOfFeatures+nodes[shift+leaf[16]].feature_id]>nodes[shift+leaf[16]].threshold ? 1 : 0];
            leaf[17]=nodes[shift+leaf[17]].children[features[(numberOfinstances+17)*numberOfFeatures+nodes[shift+leaf[17]].feature_id]>nodes[shift+leaf[17]].threshold ? 1 : 0];
            leaf[18]=nodes[shift+leaf[18]].children[features[(numberOfinstances+18)*numberOfFeatures+nodes[shift+leaf[18]].feature_id]>nodes[shift+leaf[18]].threshold ? 1 : 0];
            leaf[19]=nodes[shift+leaf[19]].children[features[(numberOfinstances+19)*numberOfFeatures+nodes[shift+leaf[19]].feature_id]>nodes[shift+leaf[19]].threshold ? 1 : 0];
            leaf[20]=nodes[shift+leaf[20]].children[features[(numberOfinstances+20)*numberOfFeatures+nodes[shift+leaf[20]].feature_id]>nodes[shift+leaf[20]].threshold ? 1 : 0];
            leaf[21]=nodes[shift+leaf[21]].children[features[(numberOfinstances+21)*numberOfFeatures+nodes[shift+leaf[21]].feature_id]>nodes[shift+leaf[21]].threshold ? 1 : 0];
            leaf[22]=nodes[shift+leaf[22]].children[features[(numberOfinstances+22)*numberOfFeatures+nodes[shift+leaf[22]].feature_id]>nodes[shift+leaf[22]].threshold ? 1 : 0];
            leaf[23]=nodes[shift+leaf[23]].children[features[(numberOfinstances+23)*numberOfFeatures+nodes[shift+leaf[23]].feature_id]>nodes[shift+leaf[23]].threshold ? 1 : 0];
            leaf[24]=nodes[shift+leaf[24]].children[features[(numberOfinstances+24)*numberOfFeatures+nodes[shift+leaf[24]].feature_id]>nodes[shift+leaf[24]].threshold ? 1 : 0];
            leaf[25]=nodes[shift+leaf[25]].children[features[(numberOfinstances+25)*numberOfFeatures+nodes[shift+leaf[25]].feature_id]>nodes[shift+leaf[25]].threshold ? 1 : 0];
            leaf[26]=nodes[shift+leaf[26]].children[features[(numberOfinstances+26)*numberOfFeatures+nodes[shift+leaf[26]].feature_id]>nodes[shift+leaf[26]].threshold ? 1 : 0];
            leaf[27]=nodes[shift+leaf[27]].children[features[(numberOfinstances+27)*numberOfFeatures+nodes[shift+leaf[27]].feature_id]>nodes[shift+leaf[27]].threshold ? 1 : 0];
            leaf[28]=nodes[shift+leaf[28]].children[features[(numberOfinstances+28)*numberOfFeatures+nodes[shift+leaf[28]].feature_id]>nodes[shift+leaf[28]].threshold ? 1 : 0];
            leaf[29]=nodes[shift+leaf[29]].children[features[(numberOfinstances+29)*numberOfFeatures+nodes[shift+leaf[29]].feature_id]>nodes[shift+leaf[29]].threshold ? 1 : 0];
            leaf[30]=nodes[shift+leaf[30]].children[features[(numberOfinstances+30)*numberOfFeatures+nodes[shift+leaf[30]].feature_id]>nodes[shift+leaf[30]].threshold ? 1 : 0];
            leaf[31]=nodes[shift+leaf[31]].children[features[(numberOfinstances+31)*numberOfFeatures+nodes[shift+leaf[31]].feature_id]>nodes[shift+leaf[31]].threshold ? 1 : 0];
        }
    }
}
