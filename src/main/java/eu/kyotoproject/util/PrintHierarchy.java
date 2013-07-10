package eu.kyotoproject.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/25/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrintHierarchy {

    static ArrayList<String> children = new ArrayList<String>();
    static ArrayList<String> tops = new ArrayList<String>();
    static ArrayList<String> processed = new ArrayList<String>();

    static public void main (String[] args) {
        HashMap<String, ArrayList<String>> parentMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> relationArrayList = new ArrayList<String>();
        String pathToKafFile = "";
        String pathToSynsetOntologyFile = "";
        String pathToSynsetBaseConceptFile = "";
        String pathToOntologyOntologyFile = "";
        String pathToRelationsFile = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--ontology-ontology")) && (args.length>(i+1))) {
                pathToOntologyOntologyFile = args[i+1];
                //   System.out.println("ontologyOntologyMap = " + ontologyOntologyMap.size());
            }
            else if ((arg.equalsIgnoreCase("--relations")) && (args.length>(i+1))) {
                pathToRelationsFile = args[i+1];
                //  System.out.println("relationArrayList = " + relationArrayList.size());
            }
        }
       relationArrayList = processRelationsFile(pathToRelationsFile);
       parentMap = processMappingFile(pathToOntologyOntologyFile,relationArrayList);
       getTopNodes(parentMap);
       String str = printTree(parentMap, tops, 0);
        try {
            FileOutputStream fos = new FileOutputStream(pathToOntologyOntologyFile+".tree");
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    static String printTree (HashMap<String, ArrayList<String>> map, ArrayList<String> tops, int level) {
        String str = "";
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (!processed.contains(top)) {
                processed.add(top);
                for (int j = 0; j < level; j++) {
                   str += "  ";
                }
                str += top+"\n";
              //  System.out.println(str);
                if (map.containsKey(top)) {
                    ArrayList<String> children = map.get(top);
                    str += printTree(map, children, level+1);
                }
            }
        }
        return str;
    }

    static String printTreeCircular (HashMap<String, ArrayList<String>> map, ArrayList<String> tops, int level) {
        String str = "";
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);

                for (int j = 0; j < level; j++) {
                   str += "  ";
                }
                str += top+"\n";
              //  System.out.println(str);
                if (map.containsKey(top)) {
                    ArrayList<String> children = map.get(top);
                    str += printTree(map, children, level+1);
                }

        }
        return str;
    }

    static void getTopNodes (HashMap<String, ArrayList<String>> map) {
        Set keySet = map.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (!children.contains(key)) {
               tops.add(key);
            }
        }
    }



    public static HashMap<String, ArrayList<String>> processMappingFile (String file, ArrayList<String> relations) {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        try {

            //eng-30-15294607-n sc_subClassOf Kyoto#time_period__period_of_time__period-eng-3.0-15113229-n
            String [] headers = null;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String source = "";
            String target = "";
            String relation = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    String[] fields = inputLine.split(" ");
                    //System.out.println("fields = " + fields);
                    source = "";
                    target = "";
                    relation = "";
                    if (fields.length>2) {
                        source = fields[0];
                        relation = fields[1];
                        target = fields[2];
                        if (relations.contains(relation)) {
                            if (!source.isEmpty() && !target.isEmpty()) {
                                if (!source.equals(target)) {
                                    /// we build a parent to child map;
                                    children.add(source);
                                    if (map.containsKey(target)) {
                                        ArrayList<String> sources = map.get(target);
                                        if (!sources.contains(source)) {
                                            sources.add(source);
                                            map.put(target, sources);
                                        }
                                    }
                                    else {
                                        ArrayList<String> sources = new ArrayList<String>();
                                        sources.add(source);
                                        map.put(target, sources);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return map;
    }

    public static ArrayList<String> processRelationsFile (String file) {
        ArrayList<String> relationArrayList = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    relationArrayList.add(inputLine.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return relationArrayList;
    }
}
