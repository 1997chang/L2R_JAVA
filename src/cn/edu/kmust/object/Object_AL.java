/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.object;

/**
 *
 * @author Administrator
 */
public class Object_AL {
    private Object_AL left;
    private Object_AL right;
    private int feature_id;
    private float threshold;
    private long id;
    
    public Object_AL(long id,int feature_id,float threshold){
        this.id=id;
        this.feature_id=feature_id;
        this.threshold=threshold;
        this.left=null;
        this.right=null;
    }

    public long getId() {
        return id;
    }

    public void setLeft(Object_AL left) {
        this.left = left;
    }

    public void setRight(Object_AL right) {
        this.right = right;
    }
    
    /**
     * 根据某一篇文档的特征值，得到一棵树中的叶子节点
     * @param features
     * @return 
     */
    public Object_AL getLeaf(float []features){
        if(this.left==null&&this.right==null){
            return this;
        }
        if(features[this.feature_id]<=this.threshold){
            return this.left.getLeaf(features);
        }else{
            return this.right.getLeaf(features);
        }
    }

    public float getThreshold() {
        return threshold;
    }
}
