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
    
    private RegressionTree left =null;
    private RegressionTree right =null;
    private double avgLabel=0.0;
    private double threshold=0.0;
    private int feature_id=-1;
    
    /**
     * 根据XML文件中的一个tree节点下的split去构造一棵决策树
     * @param split 就是tree节点下的第一个split或者split下面的split 
     */
    public void createTree(Element split){
        //是一个分支节点
        if(split.element("feature")!=null){
            feature_id=Integer.parseInt(split.element("feature").getTextTrim());
            threshold=Double.parseDouble(split.element("threshold").getTextTrim());
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
     * 遍历一颗树
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
    
}
