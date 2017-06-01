/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.dataset;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Dataset {
    
    /**
    文档对的个数
    */
    private int max_instances=0;
    /**
     * 特征值的个数
     */
    private int num_features=0;
    private final List<Integer> offset_ = new ArrayList<>();
    private final List<Integer> Label_ = new ArrayList<>();
    private final List<List<Double>> data_ =new ArrayList<>();
    private int num_queries=0;
    private int last_instance_id_=-1;
    private int num_instances_;
    
    public void read_datascource(String fileName){
        FileReader fileReader =null;
        BufferedReader bufferedReader =null;
        try {
            fileReader=new FileReader(fileName);
            bufferedReader=new BufferedReader(fileReader);
            String readLine;
            while((readLine=bufferedReader.readLine())!=null){
                //存放标签
                Integer Label;
                //存放特征值
                List<Double> feature =new ArrayList<>();
                //存放qid的值
                Integer qid;
                readLine=readLine.trim();
                //表示空行
                if(readLine.length()==0)
                    continue;
                //#表示注释行
                if(readLine.startsWith("#"))
                    continue;
                //删除最后的#之后的字符
                int idx=readLine.lastIndexOf("#");
                if(idx!=-1){
                    readLine=readLine.substring(0, idx).trim();
                }
                try{
                    String[] fs=readLine.split(" ");
                    Label =Integer.parseInt(fs[0]);
                    if(getKey(fs[1]).equals("qid")){
                        qid=Integer.parseInt(getValue(fs[1]));
                    }else{
                        throw new RuntimeException("没有包含qid的选项");
                    }
                    for(int i=2;i<fs.length;i++){
                        feature.add(Double.parseDouble(getValue(fs[i])));
                    }
                    int current_features=Integer.parseInt(getKey(fs[fs.length-1]));
                    if(current_features>num_features){
                        num_features=current_features;
                    }
                }catch(Exception e){
                    System.out.println("readLine is error line. line is : "+readLine);
                    continue;
                }
                if(last_instance_id_!=qid){
                    offset_.add(max_instances);
                    last_instance_id_=qid;
                    num_queries++;
                }
                max_instances++;
                Label_.add(Label);
                data_.add(feature);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Dataset.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Dataset.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(fileReader!=null){
                    fileReader.close();
                    fileReader=null;
                }
                if(bufferedReader!=null){
                    bufferedReader.close();
                    bufferedReader=null;
                }
            } catch (IOException ex) {
                Logger.getLogger(Dataset.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private String getValue(String pair){
        return pair.split(":")[1];
    }
    
    private String getKey(String pair){
        return pair.split(":")[0];
    }
}
