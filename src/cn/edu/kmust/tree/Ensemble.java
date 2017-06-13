/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.tree;

import cn.edu.kmust.dataset.Dataset;
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
    
    private static int document_block=50;
    private static int score_block=50;
    private int size=0;
    
    /**
     * 根据一个XML文件得到一个ensemble森林
     * @param modelFile XML文件
     */
    public void readModelFile(String modelFile){
        System.out.println("# Loading model from file "+modelFile);
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
        size=trees.size();
        System.out.println("# pubulish load model from file "+modelFile);
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
        int treesize=getTrees().size();
        int documentsize=features.size();
        for(int i=0;i<documentsize;i++){
            for(int j=0;j<treesize;j++){
                RegressionTree regressionTree=getTrees().get(j);
                result[i]+=regressionTree.travesal(features.get(i))*getWeights().get(j);
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
    
    public void score_dataset1(Dataset dataset,double []scores){
        List<Float> features=dataset.getData();
        int featuresize=dataset.getFeatureSize();
        List<Float> subfeature;
        int instance_size=dataset.getMax_instances();
        for(int i=0;i<instance_size;i++){
            subfeature=features.subList(i*featuresize, (i+1)*featuresize);
            scores[i]=score_document(subfeature);
        }
    }
    
    public void score_dataset2(Dataset dataset,double []scores){
        int instance_size=dataset.getMax_instances();
        for(int i=0;i<instance_size;i++){
            List<Float> features=dataset.getData_i(i);
            scores[i]=score_document(features);
        }
    }
    
    public double score_document(List<Float> features){
        double sum=0.0;
        for(int i=0;i<trees.size();i++){
            sum+=weights.get(i)*trees.get(i).score_instance(features);
        }
        return sum;
    }
    
    public void score_instance_SDSD(Dataset dataset,double []scores){
        int query_size=dataset.getNum_queries();
        List<Float> features;
        int offset=0;
        for(int pid_index=0;pid_index<query_size;pid_index++){
            int instance_pre_query=dataset.offset(pid_index+1)-dataset.offset(pid_index);
            int score_block_num=size/score_block;
            int last_score_num=size%score_block;
            int document_block_num=instance_pre_query/document_block;
            int last_document_num=instance_pre_query%document_block;
            int j=0;
            for(;j<score_block_num;j++){
                int i=0;
                for(;i<document_block_num;i++){
                    for(int jj=0;jj<score_block;jj++){
                        for(int ii=0;ii<document_block;ii++){
                            features=dataset.getData_i(offset+i*document_block+ii);
                            scores[offset+i*document_block+ii]+=trees.get(j*score_block+jj).score_instance(features)*weights.get(j*score_block+jj);
                        }
                    }
                }
                for(int jj=0;jj<score_block;jj++){
                    for(int ii=0;ii<last_document_num;ii++){
                        features=dataset.getData_i(offset+i*document_block+ii);
                        scores[offset+i*document_block+ii]+=trees.get(j*score_block+jj).score_instance(features)*weights.get(j*score_block+jj);
                    }
                }
            }
            int i=0;
            for(;i<document_block_num;i++){
                for(int jj=0;jj<last_score_num;jj++){
                    for(int ii=0;ii<document_block;ii++){
                        features=dataset.getData_i(offset+i*document_block+ii);
                        scores[offset+i*document_block+ii]+=trees.get(j*score_block+jj).score_instance(features)*weights.get(j*score_block+jj);
                    }
                }
            }
            for(int jj=0;jj<last_score_num;jj++){
                for(int ii=0;ii<last_document_num;ii++){
                    features=dataset.getData_i(offset+i*document_block+ii);
                    scores[offset+i*document_block+ii]+=trees.get(j*score_block+jj).score_instance(features)*weights.get(j*score_block+jj);
                }
            }
            offset+=instance_pre_query;
        }
    }

    /**
     * @param aDocument_block the document_block to set
     */
    public static void setDocument_block(int aDocument_block) {
        document_block = aDocument_block;
    }

    /**
     * @param aScore_block the score_block to set
     */
    public static void setScore_block(int aScore_block) {
        score_block = aScore_block;
    }
}
