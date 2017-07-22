/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class FileUtils {
    
    /**
     * 用于将content内容写入到fileName文件中
     * @param fileName 文件的名称
     * @param content 写入的内容
     */
    public static void writeFile(String fileName,List<String> content){
        BufferedWriter out=null;
        try {
            out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
            for(String line:content){
                out.write(line);
                out.newLine();
            }
            out.flush();
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Error in "+FileUtils.class.getName()+"::writeFile():");
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error in "+FileUtils.class.getName()+"::writeFile():");
        }finally{
            try {
                if(out!=null){
                    out.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
