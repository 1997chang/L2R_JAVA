/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.tree;

import java.util.List;
import org.dom4j.Element;

/**
 *
 * @author Administrator
 */
public class RegressionTree {
    
    /**
     * 当前节点的左子树对象
     */
    private RegressionTree left =null;
    /**
     * 当前节点的右子树对象
     */
    private RegressionTree right =null;
    /**
     * 叶子节点的输出值
     */
    private double avgLabel=0.0;
    /**
     * 阈值
     */
    private float threshold=0.0f;
    /**
     * 要和哪一个特征进行比较。这里的feature_id是从0开始计算的。所以是文档特征下标的数字-1
     */
    private int feature_id=-1;
    
    /**
     * 根据XML文件中的一个tree节点下的split去构造一棵决策树
     * @param split 就是tree节点下的第一个split或者split下面的split 
     */
    public void createTree(Element split){
        //是一个分支节点
        if(split.element("feature")!=null){
            feature_id=Integer.parseInt(split.element("feature").getTextTrim())-1;
            threshold=Float.parseFloat(split.element("threshold").getTextTrim());
            List childs=split.elements("split");
            for(Object obj:childs){
                Element child =(Element) obj;
                if(child.attributeValue("pos").equals("left")){
                    left = new RegressionTree();
                    left.createTree(child);
                }else{
                    right=new RegressionTree();
                    right.createTree(child);
                }
            }
        }else{
            //是一个叶子节点
            avgLabel=Double.parseDouble(split.element("output").getTextTrim());
        }
    }
    
    /**
     * 遍历一颗树，输出各个值
     */
    public void traversal(){
        if(left==null&&right==null){
            System.out.println("叶子节点的输出值为:"+avgLabel);
        }else{
            System.out.println("当前节点feature为:"+feature_id);
            System.out.println("当前节点的阈值为:"+threshold);
            if(left!=null){
                System.out.println("当前节点的左节点:");
                left.traversal();
            }
            if(right!=null){
                System.out.println("当前节点的右节点:");
                right.traversal();
            }
        }
    }
    
    /**
     * 根据一个文档的特征序列,返回这个文档对应叶子的输出值（在一个树上）
     * @param feature
     * @return 一棵树的输出值
     */
    public double travesal(List<Float> feature){
        if(left==null&&right==null){
            return avgLabel;
        }
        if(feature.get(feature_id)<=threshold){
            return left.travesal(feature);
        }else{
            return right.travesal(feature);
        }
    }
    
    public double score_instance(List<Float> feature){
        double score=feature_id==-1?avgLabel:(feature.get(feature_id)<=threshold?left.score_instance(feature):right.score_instance(feature));
        return score;
    }
    
}
