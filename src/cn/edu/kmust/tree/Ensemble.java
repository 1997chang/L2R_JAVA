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
    
    private List<RegressionTree> trees = new ArrayList<>();
    private List<Double> weights = new ArrayList<>();
    
    public static void main(String []args){
        Ensemble ensemble=new Ensemble();
        ensemble.readModelFile("LAMBDAMART-model.xml.T100.xml");
        ensemble.readTrees();
    }
    
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
                weights.add(Double.parseDouble(tree.attributeValue("weight")));
                RegressionTree tree_root=new RegressionTree();
                tree_root.createTree(tree.element("split"));
                this.trees.add(tree_root);
            }
        } catch (DocumentException ex) {
            Logger.getLogger(Ensemble.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 用于验证一个XML文件是否正确读取
     */
    public void readTrees(){
        for(int i=0;i<trees.size();i++){
            RegressionTree regressionTree=trees.get(i);
            regressionTree.traversal();
            System.out.println("当前树的权重为:"+weights.get(i));
            System.out.println("************************************************");
        }
    }
}
