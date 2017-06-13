/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.quickrank;

import cn.edu.kmust.dataset.Dataset;
import cn.edu.kmust.tree.Ensemble;

/**
 *
 * @author Administrator
 */
public class Quickrank_Run {
    
    /**
     * 保存所有的决策树的信息
     */
    private final Ensemble ensemble= new Ensemble();
    /**
     * 保存所有数据集的信息
     */
    private final Dataset dataset =new Dataset();
    /**
     * 训练的模型的位置及名称
     */
    private String modelFileName="";
    /**
     * 数据集的位置及名称
     */
    private String datasetFileName="";
    
    private int rounds_num=10;
    
    
    
    public static void main(String []args){
        String modelFilename="model/LAMBDAMART-model.xml";
        String datasetFileName="dataset/msn-5k/test.txt";
        Quickrank_Run quickrank_Run=new Quickrank_Run(modelFilename,datasetFileName);
//        quickrank_Run.score_dataset_SDSD();
//        quickrank_Run.score_dataset2();
//        quickrank_Run.score_dataset1();
    }

    private Quickrank_Run(String modelFilename, String datasetFileName) {
        this.modelFileName=modelFilename;
        this.datasetFileName=datasetFileName;
    }
    
    public void score_dataset_SDSD(){
        ensemble.readModelFile(modelFileName);
        dataset.read_datascource(datasetFileName, "testing");
        double []scores=new double[dataset.getMax_instances()];
        int s=5000;
        int d=5000;
//        int []score_block={10,50,100,500,1000,5000};
//        int []document_block={10,50,100,500,1000,5000};
//        for(int s:score_block){
//            for(int d:document_block){
                scores=new double[dataset.getMax_instances()];
                Ensemble.setScore_block(s);
                Ensemble.setDocument_block(d);
                long time_begin = System.nanoTime();
                for(int i=0;i<rounds_num;i++){
                    ensemble.score_instance_SDSD(dataset, scores);
                }
                long time_end= System.nanoTime();
                long time_total=time_end-time_begin;
//                System.out.println(rounds_num+" rounds Tatal Time: "+time_total+" ns");
//                System.out.println("per rounds Tatal Time: "+(time_total/rounds_num)+" ns");
                System.out.println("score_block:"+s+"; document_block:"+d);
                System.out.println("per instance total time:"+(time_total/rounds_num/dataset.getMax_instances()) +"ns");
//            }
//        }
    }
    
    public void score_dataset1(){
        ensemble.readModelFile(modelFileName);
        dataset.read_datascource(datasetFileName, "testing");
        double []scores=new double[dataset.getMax_instances()];
        long time_begin = System.nanoTime();
        for(int i=0;i<rounds_num;i++){
            ensemble.score_dataset1(dataset, scores);
        }
        long time_end= System.nanoTime();
        long time_total=time_end-time_begin;
        System.out.println(rounds_num+" rounds Tatal Time: "+time_total+" ns");
        System.out.println("per rounds Tatal Time: "+(time_total/rounds_num)+" ns");
        System.out.println("per instance total time:"+(time_total/rounds_num/dataset.getMax_instances()) +"ns");
//        for(int i=0;i<dataset.getMax_instances();i++){
//            System.out.println(scores[i]);
//        }
    }
    
    public void score_dataset2(){
        ensemble.readModelFile(modelFileName);
        dataset.read_datascource(datasetFileName, "testing");
        double []scores=new double[dataset.getMax_instances()];
        long time_begin = System.nanoTime();
        for(int i=0;i<rounds_num;i++){
            ensemble.score_dataset2(dataset, scores);
        }
        long time_end= System.nanoTime();
        long time_total=time_end-time_begin;
        System.out.println(rounds_num+" rounds Tatal Time: "+time_total+" ns");
        System.out.println("per rounds Tatal Time: "+(time_total/rounds_num)+" ns");
        System.out.println("per instance total time:"+(time_total/rounds_num/dataset.getMax_instances()) +"ns");
//        for(int i=0;i<dataset.getMax_instances();i++){
//            System.out.println(scores[i]);
//        }
    }
    
}
