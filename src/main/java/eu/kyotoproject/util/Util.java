package eu.kyotoproject.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 5/20/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {

    static public ArrayList<String> makeRecursiveFileListAll(String inputPath, String extension) {
        ArrayList<String> acceptedFileList = new ArrayList<String>();
        ArrayList<String>  nestedFileList = new ArrayList<String>();
        File[] theFileList = null;
        File lF = new File(inputPath);
        if ((lF.canRead()) && lF.isDirectory()) {
            theFileList = lF.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                String newFilePath = theFileList[i].getAbsolutePath();
                //   System.out.println("newFilePath = " + newFilePath);
                if (theFileList[i].isDirectory()) {
                    nestedFileList = makeRecursiveFileListAll(newFilePath, extension);
                    for (int j = 0; j < nestedFileList.size(); j++) {
                        String s = nestedFileList.get(j);
                        if (s.endsWith(extension)) {
                            acceptedFileList.add(s);
                        }
                    }
                } else {
                    if (newFilePath.endsWith(extension)) {
                        acceptedFileList.add(newFilePath);
                    }
                }
            }
        }
        return acceptedFileList;
    }

    static public ArrayList<String> makeFlatFileList(String inputPath, String extension) {
        ArrayList<String> acceptedFileList = new ArrayList<String>();
        ArrayList<String>  nestedFileList = new ArrayList<String>();
        File[] theFileList = null;
        File lF = new File(inputPath);
        if ((lF.canRead()) && lF.isDirectory()) {
            theFileList = lF.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                if (theFileList[i].getName().endsWith(extension)) {
                    acceptedFileList.add(theFileList[i].getAbsolutePath());
                }
            }
        }
        return acceptedFileList;
    }

    static public HashMap<String, ArrayList<String>> ReadFileToStringHashMap(String separator, String fileName) {
        HashMap<String, ArrayList<String>> lineHashMap = new HashMap<String, ArrayList<String>>();
        if (new File (fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        String[] fields = inputLine.split(separator);
                        String key = fields[0].trim();
                        ArrayList<String> labels = new ArrayList<String>();
                        for (int i = 1; i < fields.length; i++) {
                            String field = fields[i].trim();
                            labels.add(field);
                        }
                        lineHashMap.put(key, labels);
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lineHashMap;
    }

    static public ArrayList<String> ReadFileToStringArrayList(String fileName) {
        ArrayList<String> list = new ArrayList<String>();
        if (new File (fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        String [] fields = inputLine.split("\t");
                        list.add(fields[0].toLowerCase()); //// we ignore any labels of the first column
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
