package eu.kyotoproject.util;

import eu.kyotoproject.kaf.KafSense;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 5/20/13
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Resources {
    public Vector<String> grammaticalWords = new Vector<String>();
    public HashMap<String, ArrayList<String>> wordNetLemmaSenseMap = new HashMap<String,ArrayList<String>>();
    public HashMap<String, ArrayList<ArrayList<String>>> wordNetPredicateMap = new HashMap<String,ArrayList<ArrayList<String>>>();
    public HashMap<String, ArrayList<ArrayList<String>>> verbNetPredicateMap = new HashMap<String,ArrayList<ArrayList<String>>>();
    public HashMap<String, ArrayList<String>> synsetBaseconceptMap = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> synsetOntologyMap = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> ontologyOntologyMap = new HashMap<String, ArrayList<String>>();
    public ArrayList<String> relationArrayList = new ArrayList<String>();

    public Resources () {
        wordNetLemmaSenseMap = new HashMap<String,ArrayList<String>>();
        wordNetPredicateMap = new HashMap<String,ArrayList<ArrayList<String>>>();
        verbNetPredicateMap = new HashMap<String,ArrayList<ArrayList<String>>>();
        synsetBaseconceptMap = new HashMap<String, ArrayList<String>>();
        synsetOntologyMap = new HashMap<String, ArrayList<String>>();
        ontologyOntologyMap = new HashMap<String, ArrayList<String>>();
        relationArrayList = new ArrayList<String>();
    }


    static InputStream getStreamFromFile (String pathToFile) {
        if (pathToFile.toLowerCase().endsWith(".gz")) {
            try {
                InputStream fileStream = new FileInputStream(pathToFile);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                return gzipStream;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (pathToFile.toLowerCase().endsWith(".bz2")) {
            try {
                InputStream fileStream = new FileInputStream(pathToFile);
                InputStream gzipStream = new CBZip2InputStream(fileStream);
                return gzipStream;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                InputStream fileStream = new FileInputStream(pathToFile);
                return fileStream;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    static public void main (String[] args) {
        try {
            String pathToPredicateMatrixFile = "";
            pathToPredicateMatrixFile = "/Tools/ontotagger-v1.0/resources/PredicateMatrix.v1.2/PredicateMatrix_withESO.txt";
            pathToPredicateMatrixFile = "/Code/vu/WordnetTools/resources/PredicateMatrix_withESO.v0.2.txt.role";
            OutputStream fos = new FileOutputStream(pathToPredicateMatrixFile+"wnfn");
            Resources resources = new Resources();
            resources.processMatrixFile(pathToPredicateMatrixFile, "mcr", "");
            Set keySet = resources.wordNetPredicateMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String fnRelations = "";
                ArrayList<String> frames = new ArrayList<String>();
                ArrayList<ArrayList<String>> mappingSets = resources.wordNetPredicateMap.get(key);
                for (int i = 0; i < mappingSets.size(); i++) {
                    ArrayList<String> mapping = mappingSets.get(i);
                    for (int j = 0; j < mapping.size(); j++) {
                        String s = mapping.get(j);
                        if (s.startsWith("fn:")) {
                            if (!frames.contains(s)) {frames.add(s);}

                        }
                    }

                }
                if (frames.size()>0) {
                    /*
                    <Synset id="fn:Vehicle_landing">
                     */
                   String str = "<Synset id=\""+key+"\">\n";
                    for (int i = 0; i < frames.size(); i++) {
                        String frame = frames.get(i);
                         /*
                            <SynsetRelation relType="has_hyperonym" target="eso:Arriving"/>
                             */
                        str += "<SynsetRelation relType=\"has_hyperonym\" target=\""+frame+"\"/>\n";
                    }
                    str += "</Synset>\n";
                    fos.write(str.getBytes());
                }
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void processGrammaticalWordsFile (String file) {
        try {

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    if (!grammaticalWords.contains(inputLine)) {
                       grammaticalWords.add(inputLine);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void processMappingFile (String file, HashMap<String, ArrayList<String>> map) {
        try {

            //eng-30-15294607-n sc_subClassOf Kyoto#time_period__period_of_time__period-eng-3.0-15113229-n
            String [] headers = null;
            //InputStream fis = new FileInputStream(file);
            InputStream fis = getStreamFromFile(file);
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
    ///////// PREDICATE MATRIX FUNCTIONS //////////////////////////////

    public void processMatrixFileWithWordnetLemma(String file) {
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
            //InputStream fis = new FileInputStream(file);
            InputStream fis = getStreamFromFile(file);
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
                        lemma = senseKey;
                        if (senseKey.length()>2) {
                            lemma = senseKey.substring(3);
                            int idx = lemma.indexOf("%");
                            if (idx!=-1) {
                                lemma = lemma.substring(0, idx);
                            }
                        }
                        if (lemma.isEmpty()) {
                            continue;
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
                                ArrayList<ArrayList<String>> targets = wordNetPredicateMap.get(senseKey);
                                if (!hasSourceField(targets, sourceFields)) {
                                    targets.add(sourceFields);
                                    wordNetPredicateMap.put(senseKey, targets);

                                }/*                                for (int i = 0; i < sourceFields.size(); i++) {
                                    String s = sourceFields.get(i);
                                    if (!targets.contains(s)) {
                                        targets.add(s);
                                    }
                                }*/
                                wordNetPredicateMap.put(senseKey, targets);
                            }
                            else {
                                ArrayList<ArrayList<String>> targets = new ArrayList<ArrayList<String>>();
                                targets.add(sourceFields);
                                wordNetPredicateMap.put(senseKey, targets);
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



    public void processMatrixFileWithWordnetILI (String file) {
        try {
           /*
           VN_CLASS VN_CLASS_NUMBER VN_SUBCLASS VN_SUBCLASS_NUMBER VN_LEMA WN_SENSE VN_THEMROLE FN_FRAME FN_LEXENT FN_ROLE PB_ROLESET PB_ARG MCR_ILIOFFSET MCR_DOMAIN MCR_SUMO MC_LEXNAME
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Experiencer fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:0 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Attribute fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:1 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Stimulus fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:NULL mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misinterpret wn:misinterpret%2:31:02 vn:Experiencer fn:NULL fn:NULL fn:NULL pb:misinterpret.01 pb:0 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misinterpret wn:misinterpret%2:31:02 vn:Attribute fn:NULL fn:NULL fn:NULL pb:misinterpret.01 pb:2 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition

            */
            String [] headers = null;
            //InputStream fis = new FileInputStream(file);
            InputStream fis = getStreamFromFile(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String synset = "";
            String senseKey ="";
            String lemma = "";
            int nSynset = 0;
            int noSynset = 0;
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    String[] fields = inputLine.split(" ");
                    synset = "";
                    lemma= "";
                    senseKey = "";
                    ///get synset identifier
                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i];
                        if (field.startsWith("mcr:")) {
                            synset = field.substring(4);
                            if (synset.startsWith("d_") || synset.startsWith("n_")) {
                                ///DWN synset
                                //mcr:d_v-1339-v
                                break;
                            }
                            else if (synset.startsWith("ili")) {
                                //// ili reference
                                synset = "eng"+synset.substring(3);  //mcr:ili-30-00619869-v
                                break;
                            }
                        }
                    }
                   // System.out.println("synset = " + synset);
                    // we get the lemma and the senseKey
                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i];
                        if (field.startsWith("wn:")) {
                            senseKey = field; /// wn:misconstrue%2:31:01
                            //System.out.println("senseKey = " + senseKey);
                            lemma = senseKey.substring(3);
                            int idx = lemma.indexOf("%");
                            if (idx!=-1) {
                                lemma = lemma.substring(0, idx);
                            }
                        }
                    }
                    if (synset.isEmpty()) {
                        /// we could not find a synset reference
                        noSynset++;
                        continue;
                    }
                    if (wordNetLemmaSenseMap.containsKey(lemma)) {
                        ArrayList<String> synsets = wordNetLemmaSenseMap.get(lemma);
                        if (!synsets.contains(synset)) {
                            synsets.add(synset);
                            wordNetLemmaSenseMap.put(lemma, synsets);
                        }
                    }
                    else {
                        ArrayList<String> synsets =new ArrayList<String>();
                        synsets.add(synset);
                        wordNetLemmaSenseMap.put(lemma, synsets);
                    }
                    ArrayList<String> sourceFields = new ArrayList<String>();
                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i];
                        if (field.toLowerCase().indexOf("null")==-1) {
                            sourceFields.add(field);
                        }
                    }
                    if (sourceFields.size()>0) {
                        if (wordNetPredicateMap.containsKey(synset)) {
                            nSynset++;
                            ArrayList<ArrayList<String>> targets = wordNetPredicateMap.get(synset);
                            if (!hasSourceField(targets, sourceFields)) {
                                targets.add(sourceFields);
                                wordNetPredicateMap.put(synset, targets);

                            }                           /* for (int i = 0; i < sourceFields.size(); i++) {
                                String s = sourceFields.get(i);
                                if (!targets.contains(s)) {
                                    targets.add(s);
                                }
                            }*/
                            wordNetPredicateMap.put(synset, targets);
                        }
                        else {
                            nSynset++;
                            ArrayList<ArrayList<String>> targets = new ArrayList<ArrayList<String>>();
                            targets.add(sourceFields);
                            wordNetPredicateMap.put(synset, targets);
                        }
                    }
                    else {
                        //System.out.println(synset+":"+sourceFields.size());
                    }
                }
            }

            System.out.println("nSynset = " + nSynset);
            System.out.println("noSynset = " + noSynset);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void processMatrixFile (String file,
                                   String key,
                                   String prefix) {
        try {
           /*
           VN_CLASS VN_CLASS_NUMBER VN_SUBCLASS VN_SUBCLASS_NUMBER VN_LEMA WN_SENSE VN_THEMROLE FN_FRAME FN_LEXENT FN_ROLE PB_ROLESET PB_ARG MCR_ILIOFFSET MCR_DOMAIN MCR_SUMO MC_LEXNAME
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Experiencer fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:0 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Attribute fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:1 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Stimulus fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:NULL mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misinterpret wn:misinterpret%2:31:02 vn:Experiencer fn:NULL fn:NULL fn:NULL pb:misinterpret.01 pb:0 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misinterpret wn:misinterpret%2:31:02 vn:Attribute fn:NULL fn:NULL fn:NULL pb:misinterpret.01 pb:2 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition

            */
            //InputStream fis = new FileInputStream(file);
            InputStream fis = getStreamFromFile(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String synset = "";
            int nSynset = 0;
            int noSynset = 0;
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    String[] fields = inputLine.split(" ");
                    if (fields.length==1) {
                        fields = inputLine.split("\t");
                    }
                   // System.out.println("fields.length = " + fields.length);
                    synset = "";
                    ///get synset identifier
                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i];
                        if (field.startsWith(key)) {
                           //  System.out.println("field = " + field);
                            int idx = field.lastIndexOf(":");
                            if (idx > -1) {
                                synset = field.substring(idx + 1);
                            } else {
                                synset = field.substring(key.length());
                            }
                            if (prefix.isEmpty())  {
                                // System.out.println("synset = " + synset);
                                if (synset.startsWith("ili")) {
                                    //// ili reference
                                    synset = "eng" + synset.substring(3);  //mcr:ili-30-00619869-v
                                }
                            }
                            else {
                               if (synset.startsWith(prefix)) {
                                   synset = synset.substring(prefix.length());
                               }
                            }
                            break;
                        }
                        else {
                         //   System.out.println("field = " + field);
                        }
                    }

                    if (synset.isEmpty()) {
                        /// we could not find a synset reference
                        noSynset++;
                        continue;
                    }

                    ArrayList<String> sourceFields = new ArrayList<String>();
                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i];
                        if (field.toLowerCase().indexOf("null")==-1) {
                            if (!sourceFields.contains(field))  {
                                sourceFields.add(field);
                            }
                        }
                    }
                    if (sourceFields.size()>0) {
                        if (wordNetPredicateMap.containsKey(synset)) {
                            nSynset++;
                            ArrayList<ArrayList<String>> targets = wordNetPredicateMap.get(synset);
                            if (!hasSourceField(targets, sourceFields)) {
                                targets.add(sourceFields);
                                wordNetPredicateMap.put(synset, targets);

                            }
                           /* for (int i = 0; i < sourceFields.size(); i++) {
                                String s = sourceFields.get(i);
                                if (!targets.contains(s)) {
                                    targets.add(s);
                                }
                            }*/
                        }
                        else {
                            nSynset++;
                            ArrayList<ArrayList<String>> targets = new ArrayList<ArrayList<String>>();
                            targets.add(sourceFields);
                            wordNetPredicateMap.put(synset, targets);
                        }
                    }
                    else {
                        //System.out.println(synset+":"+sourceFields.size());
                    }
                }
            }

            //System.out.println("nSynset = " + nSynset);
            //System.out.println("noSynset = " + noSynset);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    boolean hasSourceField (ArrayList<ArrayList<String>> targets, ArrayList<String> sourceField) {
        for (int i = 0; i < targets.size(); i++) {
            ArrayList<String> strings = targets.get(i);
            if (strings.containsAll(sourceField)) return true;
        }
        return false;
    }

    public void processMatrixFileWithVerbNetKey (String file) {
        try {
            /* version0.1
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

            /*
            VN_CLASS VN_CLASS_NUMBER VN_SUBCLASS VN_SUBCLASS_NUMBER VN_LEMA WN_SENSE VN_THEMROLE FN_FRAME FN_LEXENT FN_ROLE PB_ROLESET PB_ARG MCR_ILIOFFSET MCR_DOMAIN MCR_SUMO MC_LEXNAME
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Experiencer fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:0 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Attribute fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:1 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misconstrue wn:misconstrue%2:31:01 vn:Stimulus fn:NULL fn:NULL fn:NULL pb:misconstrue.01 pb:NULL mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misinterpret wn:misinterpret%2:31:02 vn:Experiencer fn:NULL fn:NULL fn:NULL pb:misinterpret.01 pb:0 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition
vn:comprehend-87.2 vn:87.2 vn:null vn:null vn:misinterpret wn:misinterpret%2:31:02 vn:Attribute fn:NULL fn:NULL fn:NULL pb:misinterpret.01 pb:2 mcr:ili-30-00619869-v mcr:factotum mcr:Communication mcr:cognition

             */

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
                        if (source.isEmpty()) {
                            continue;
                        }
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
                                if (!hasSourceField(targets, sourceFields)) {
                                    targets.add(sourceFields);
                                    verbNetPredicateMap.put(source, targets);

                                }
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
