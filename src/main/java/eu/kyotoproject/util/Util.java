package eu.kyotoproject.util;

import java.io.File;
import java.util.ArrayList;

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
}
