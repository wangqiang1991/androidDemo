package com.hande.goochao.utils;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by yanshen on 2015/11/5.
 */
public class ZipUtils {

    /**
     * 合并多个Zip
     * @param zips
     * @param descZip
     */
    public static void merge(String[] zips, String descZip){
        ZipOutputStream outputStream = null;
        try {
            outputStream = new ZipOutputStream(new FileOutputStream(descZip));
            merge(zips, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 合并多个zip
     * @param zips
     * @param outputStream
     */
    public static void merge(String[] zips, ZipOutputStream outputStream){
        try {
            int index = 0;
            for (String zip : zips) {
                add(zip, outputStream, index++);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加一个zip到目标zip中
     * @param zip
     * @param outputStream
     * @throws FileNotFoundException
     */
    private static void add(String zip, ZipOutputStream outputStream, int index) throws FileNotFoundException{
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        ZipEntry zipEntry;
        try {
            zipEntry = inputStream.getNextEntry();
            ZipEntry newZipEntry = new ZipEntry(getFileName(zipEntry, index));
            outputStream.putNextEntry(newZipEntry);
            IOUtils.copy(inputStream, outputStream);
            inputStream.closeEntry();
            outputStream.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getFileName(ZipEntry zipEntry, int index){
        String name = zipEntry.getName();
        return index + name.substring(name.lastIndexOf("."));
    }

}
