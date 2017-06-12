/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.structplus;

/**
 *
 * @author Administrator
 */
public class StructPlus {
    /**
     * 当前节点的右节点
     */
    private StructPlus right=null;
    /**
     * 当前节点的左节点
     */
    private StructPlus left=null;
    /**
     * 分支节点的阈值或者叶子节点的输出值
     */
    private float threshold=0.0f;
    /**
     * 要比较的特征下标
     */
    private int feature_id=-1;
    
    private long id;
    
    /**
     * 返回特征向量的叶子节点
     * @param featureVector 一篇文档的特征数组
     * @return 最后到达的叶子节点
     */
    public StructPlus getLeaf(float[] featureVector){
        //达到叶子节点，就返回
        if(this.getLeft()==null&&this.getRight()==null){
            return this;
        }
        if(featureVector[this.getFeature_id()]<=this.getThreshold()){
            return this.getLeft().getLeaf(featureVector);
        }else{
            return this.getRight().getLeaf(featureVector);
        }
    }
    
    /**
     * 创建一个size大小的Structplus数组
     * @param size 创建的大小
     * @return 
     */
    public static StructPlus[] createNodes(int size){
        return new StructPlus[size];
    }
    
    public void setNode(long id,int featureid,float threshold){
        this.id=id;
        this.feature_id=featureid;
        this.threshold=threshold;
        
    }
    
    public long getId(){
        return id;
    }

    public void setLeft(StructPlus left) {
        this.left=left;
    }
    
    public void setRight(StructPlus right) {
        this.right=right;
    }
    
    private static int countNodes(StructPlus root){
        if(root==null){
            return 0;
        }else{
            return 1+countNodes(root.getLeft())+countNodes(root.getRight());
        }
        
    }
    
    public static StructPlus[] compress(StructPlus []root){
        int validNodes=countNodes(root[0]);
        StructPlus []tree=new StructPlus[validNodes];
        compressNodes(tree, root[0], 0);
        return tree;
    }
    
    /**
     * 返回最后一个已用的下标值
     * @param tree_new 新树的数组
     * @param tree_old 旧树的根节点
     * @param index 当前节点的下标值
     * @return 返回下一个可用的下标值
     */
    private static int compressNodes(StructPlus []tree_new,StructPlus tree_old,int index){
        tree_new[index]=new StructPlus();
        tree_new[index].id=tree_old.id;
        tree_new[index].feature_id=tree_old.getFeature_id();
        tree_new[index].threshold=tree_old.getThreshold();
        if(tree_old.getRight()!=null||tree_old.getLeft()!=null){
            //用于保存当前节点的下标值
            int pindex=index;
            //返回最后一个已用的下标值
            index=compressNodes(tree_new, tree_old.getLeft(), index+1);
            tree_new[pindex].left=tree_new[pindex+1];
            //保存右孩子的下标值
            int right=index+1;
            index=compressNodes(tree_new, tree_old.getRight(), index+1);
            tree_new[pindex].right=tree_new[right];
        }
        return index;
    }

    /**
     * @return the threshold
     */
    public float getThreshold() {
        return threshold;
    }
    
    /**
     * 遍历一颗树，输出各个值,以判断读取文件是否正确
     */
    public void printtree(){
        if(getLeft()==null&&getRight()==null){
            System.out.println("this left value is :"+getThreshold());
        }else{
            System.out.println("当前节点feature为:"+getFeature_id());
            System.out.println("当前节点的阈值为:"+getThreshold());
            if(getLeft()!=null){
                System.out.println("当前节点的左节点:");
                getLeft().printtree();
            }
            if(getRight()!=null){
                System.out.println("当前节点的右节点:");
                getRight().printtree();
            }
        }
    }
    
    /**
     * @return the feature_id
     */
    public int getFeature_id() {
        return feature_id;
    }

    /**
     * @return the right
     */
    public StructPlus getRight() {
        return right;
    }

    /**
     * @return the left
     */
    public StructPlus getLeft() {
        return left;
    }
    
}
