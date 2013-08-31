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
    public HashMap<String, ArrayList<String>> wordNetLemmaSenseMap = new HashMap<String,ArrayList<String>>();
    public HashMap<String, ArrayList<String>> wordNetPredicateMap = new HashMap<String,ArrayList<String>>();
    public HashMap<String, ArrayList<ArrayList<String>>> verbNetPredicateMap = new HashMap<String,ArrayList<ArrayList<String>>>();
    public HashMap<String, ArrayList<String>> synsetBaseconceptMap = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> synsetOntologyMap = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> ontologyOntologyMap = new HashMap<String, ArrayList<String>>();
    public ArrayList<String> relationArrayList = new ArrayList<String>();

    public Resources () {
        wordNetLemmaSenseMap = new HashMap<String,ArrayList<String>>();
        wordNetPredicateMap = new HashMap<String,ArrayList<String>>();
        verbNetPredicateMap = new HashMap<String,ArrayList<ArrayList<String>>>();
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

    public void processMatrixFileWithWordnetKey (String file) {
        try {
            /*
            vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:02 vn:Agent fn:Expressing_publicly fn:NULL fn:Communicator pb:articulate.01 pb:0
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:02 vn:Topic fn:Expressing_publicly fn:NULL fn:Content pb:articulate.01 pb:1
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:02 vn:Recipient fn:Expressing_publicly fn:NULL fn:Addressee pb:articulate.01 pb:2
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:01 vn:Agent fn:Expressing_publicly fn:NULL fn:Communicator pb:articulate.01 pb:0
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:01 vn:Topic fn:Expressing_publicly fn:NULL fn:Content pb:articulate.01 pb:1
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:01 vn:Recipient fn:Expressing_publicly fn:NULL fn:Addressee pb:articulate.01 pb:2

             */
            //vn:comprehend-87.2 vn:87.2 vn:comprehend-87.2-1 vn:87.2-1 vn:apprehend wn:apprehend%2:31:00 vn:Experiencer fn:Grasp fn:NULL fn:NULL pb:NULL pb:NULL
            //vn:comprehend-87.2 vn:87.2 vn:comprehend-87.2-1 vn:87.2-1 vn:apprehend wn:apprehend%2:31:00 vn:Attribute fn:Grasp fn:NULL fn:NULL pb:NULL pb:NULL
            //vn:comprehend-87.2 vn:87.2 vn:comprehend-87.2-1 vn:87.2-1 vn:apprehend wn:apprehend%2:31:00 vn:Stimulus fn:Grasp fn:NULL fn:NULL pb:NULL pb:NULL

            String [] headers = null;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String senseKey = "";
            String lemma = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
/*                    if (inputLine.indexOf("wn:die%2:30:00")==-1) {
                        continue;
                    }*/
                    String[] fields = inputLine.split(" ");
                    //System.out.println("fields = " + fields);
                    senseKey = "";
                    lemma= "";
                    if (fields.length>4) {
                        //// takes wn sense key as the key
                        senseKey = fields[5];
                        lemma = senseKey.substring(3);
                        int idx = lemma.indexOf("%");
                        if (idx!=-1) {
                            lemma = lemma.substring(0, idx);
                        }
                        if (wordNetLemmaSenseMap.containsKey(lemma)) {
                            ArrayList<String> senseKeys = wordNetLemmaSenseMap.get(lemma);
                            if (!senseKeys.contains(senseKey)) {
                                senseKeys.add(senseKey);
                                wordNetLemmaSenseMap.put(lemma, senseKeys);
                            }
                        }
                        else {
                            ArrayList<String> senseKeys =new ArrayList<String>();
                            senseKeys.add(senseKey);
                            wordNetLemmaSenseMap.put(lemma, senseKeys);
                        }
                        ArrayList<String> sourceFields = new ArrayList<String>();
                        for (int i = 0; i < fields.length; i++) {
                            String field = fields[i];
                            if (field.toLowerCase().indexOf("null")==-1) {
                                sourceFields.add(field);
                            }
                        }
                        if (sourceFields.size()>0) {
                            if (wordNetPredicateMap.containsKey(senseKey)) {
                                ArrayList<String> targets = wordNetPredicateMap.get(senseKey);
                                for (int i = 0; i < sourceFields.size(); i++) {
                                    String s = sourceFields.get(i);
                                    if (!targets.contains(s)) {
                                        targets.add(s);
                                    }
                                }
                                wordNetPredicateMap.put(senseKey, targets);
                            }
                            else {
                                wordNetPredicateMap.put(senseKey, sourceFields);
                            }
                        }
                    }
                }
            }
/*            Set keySet = wordNetLemmaSenseMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String str = key+"#";
                ArrayList<String> sense = wordNetLemmaSenseMap.get(key);
                for (int i = 0; i < sense.size(); i++) {
                    String s = sense.get(i);
                    str+=s+";";
                }
                System.out.println(str);

            }*/
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void processMatrixFileWithVerbNetKey (String file) {
        try {
            /*
            vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:02 vn:Agent fn:Expressing_publicly fn:NULL fn:Communicator pb:articulate.01 pb:0
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:02 vn:Topic fn:Expressing_publicly fn:NULL fn:Content pb:articulate.01 pb:1
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:02 vn:Recipient fn:Expressing_publicly fn:NULL fn:Addressee pb:articulate.01 pb:2
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:01 vn:Agent fn:Expressing_publicly fn:NULL fn:Communicator pb:articulate.01 pb:0
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:01 vn:Topic fn:Expressing_publicly fn:NULL fn:Content pb:articulate.01 pb:1
vn:say-37.7 vn:37.7 vn:say-37.7-1 vn:37.7-1 vn:articulate wn:articulate%2:32:01 vn:Recipient fn:Expressing_publicly fn:NULL fn:Addressee pb:articulate.01 pb:2

             */
            //vn:comprehend-87.2 vn:87.2 vn:comprehend-87.2-1 vn:87.2-1 vn:apprehend wn:apprehend%2:31:00 vn:Experiencer fn:Grasp fn:NULL fn:NULL pb:NULL pb:NULL
            //vn:comprehend-87.2 vn:87.2 vn:comprehend-87.2-1 vn:87.2-1 vn:apprehend wn:apprehend%2:31:00 vn:Attribute fn:Grasp fn:NULL fn:NULL pb:NULL pb:NULL
            //vn:comprehend-87.2 vn:87.2 vn:comprehend-87.2-1 vn:87.2-1 vn:apprehend wn:apprehend%2:31:00 vn:Stimulus fn:Grasp fn:NULL fn:NULL pb:NULL pb:NULL

            String [] headers = null;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String source = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    String[] fields = inputLine.split(" ");
                    //System.out.println("fields = " + fields);
                    source = "";
                    if (fields.length>4) {
//// Takes vn class lemma as the key

                        source = fields[0].substring(3);
                        int idx = source.lastIndexOf("-");
                        if (idx>-1) {
                            source = source.substring(0, idx);
                        }

                        //// takes wn sense key as the key
                        source = fields[5];
                        ArrayList<String> sourceFields = new ArrayList<String>();
                        for (int i = 0; i < fields.length; i++) {
                            String field = fields[i];
                            if (field.toLowerCase().indexOf("null")==-1) {
                                sourceFields.add(field);
                            }
                        }
                        if (sourceFields.size()>0) {
                            if (verbNetPredicateMap.containsKey(source)) {
                                ArrayList<ArrayList<String>> targets = verbNetPredicateMap.get(source);
                                targets.add(sourceFields);
                                verbNetPredicateMap.put(source, targets);
                            }
                            else {
                                ArrayList<ArrayList<String>> targets = new ArrayList<ArrayList<String>>();
                                targets.add(sourceFields);
                                verbNetPredicateMap.put(source, targets);
                            }
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
