package eu.kyotoproject.util;

import eu.kyotoproject.kaf.KafSense;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 5/20/13
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Resources {

    public HashMap<String, ArrayList<String>> synsetBaseconceptMap = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> synsetOntologyMap = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> ontologyOntologyMap = new HashMap<String, ArrayList<String>>();
    public ArrayList<String> relationArrayList = new ArrayList<String>();

    public Resources () {
        synsetBaseconceptMap = new HashMap<String, ArrayList<String>>();
        synsetOntologyMap = new HashMap<String, ArrayList<String>>();
        ontologyOntologyMap = new HashMap<String, ArrayList<String>>();
        relationArrayList = new ArrayList<String>();
    }

    public void extendExternalReference (ArrayList<String> coveredClasses, KafSense externalRef) {
        ArrayList<String> targets = ontologyOntologyMap.get(externalRef.getSensecode());
        for (int i = 0; i < targets.size(); i++) {
            String s = targets.get(i);
            String [] fields = s.split("\t");
            if (fields.length==2) {
                if ((!coveredClasses.contains(fields[1]))){
                    if (relationArrayList.contains(fields[0]) || relationArrayList.size()==0) {
                        KafSense childSense = new KafSense();
                        childSense.setSensecode(fields[1]);
                        childSense.setStatus("implied");
                        coveredClasses.add(childSense.getSensecode());
                        // System.out.println("coveredClasses = " + coveredClasses.size());
                        //System.out.println("childSense = " + childSense.getSensecode());
                        if (!fields[0].isEmpty()) {
                            childSense.setRefType(fields[0]);
                        }
                        if (ontologyOntologyMap.containsKey(childSense.getSensecode())) {
                            extendExternalReference(coveredClasses, childSense);
                        }
                        externalRef.addChildren(childSense);
                    }
                }
            }
        }
    }

    public void processSynsetBaseConceptFile (String file) {
        processMappingFile(file, synsetBaseconceptMap);
    }

    public void processOntologyOntologyFile (String file) {
        processMappingFile(file, ontologyOntologyMap);
    }

    public void processSynsetOntologyFile (String file) {
        processMappingFile(file, synsetOntologyMap);
    }

    public void processMappingFile (String file, HashMap<String, ArrayList<String>> map) {
        try {

            //eng-30-15294607-n sc_subClassOf Kyoto#time_period__period_of_time__period-eng-3.0-15113229-n
            String [] headers = null;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String source = "";
            String target = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    String[] fields = inputLine.split(" ");
                    //System.out.println("fields = " + fields);
                    source = "";
                    target = "";
                    if (fields.length==2) {
                        source = fields[0];
                        target = "\t"+fields[1];
                    }
                    else if (fields.length>2) {
                        source = fields[0];
                        target = fields[1]+"\t"+fields[2];
                    }
                    if (!source.isEmpty() && !target.isEmpty()) {
                        if (map.containsKey(source)) {
                            ArrayList<String> targets = map.get(source);
                            if (!targets.contains(target)) {
                                targets.add(target);
                                map.put(source, targets);
                            }
                        }
                        else {
                            ArrayList<String> targets = new ArrayList<String>();
                            targets.add(target);
                            map.put(source, targets);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void processRelationsFile (String file) {
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
    }

}
