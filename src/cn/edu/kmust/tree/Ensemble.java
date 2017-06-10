/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author Administrator
 */
public class Ensemble {
    
//    保存所有的决策树列表
    private final List<RegressionTree> trees = new ArrayList<>();
//    所有树的权重值
    private final List<Double> weights = new ArrayList<>();
    
    /**
     * 根据一个XML文件得到一个ensemble森林
     * @param modelFile XML文件
     */
    public void readModelFile(String modelFile){
        SAXReader reader=new SAXReader();
        try {
            Document document=reader.read(new File(modelFile));
            Element root=document.getRootElement();
            List trees_xml=root.selectNodes("//tree");
            for(Object obj:trees_xml){
                Element tree = (Element) obj;
                getWeights().add(Double.parseDouble(tree.attributeValue("weight")));
                RegressionTree tree_root=new RegressionTree();
                tree_root.createTree(tree.element("split"));
                this.getTrees().add(tree_root);
            }
        } catch (DocumentException ex) {
            Logger.getLogger(Ensemble.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 用于验证一个XML文件是否正确读取
     */
    public void readTrees(){
        for(int i=0;i<getTrees().size();i++){
            RegressionTree regressionTree=getTrees().get(i);
            regressionTree.traversal();
            System.out.println("当前树的权重为:"+getWeights().get(i));
            System.out.println("************************************************");
        }
    }
    
    /**
     * 返回一个文档集在森林中的得分
     * @param features 文档集的特征值集合
     * @return 各个文档的最后得分
     */
    public double[] getScores(List<List<Float>> features){
        double []result=new double[features.size()];
        for(int i=0;i<getTrees().size();i++){
            RegressionTree regressionTree=getTrees().get(i);
            for(int j=0;j<features.size();j++){
                result[j]+=regressionTree.travesal(features.get(j))*getWeights().get(i);
            }
        }
        return result;
    }
    
        /**
     * @return the trees
     */
    public List<RegressionTree> getTrees() {
        return trees;
    }

    /**
     * @return the weights
     */
    public List<Double> getWeights() {
        return weights;
    }
}
