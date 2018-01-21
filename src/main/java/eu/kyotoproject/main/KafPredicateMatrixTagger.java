package eu.kyotoproject.main;

import eu.kyotoproject.kaf.*;
import eu.kyotoproject.util.Resources;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: piek
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class KafPredicateMatrixTagger {

    static final String layer = "terms";
    static final String name = "vua-predicate-matrix-tagger";
    static final String version = "1.0";

    static public void main (String[] args) {
        Resources resources = new Resources();
      //  String pathToKafFile = "/Tools/ontotagger-v1.0/naf-example/spinoza-voorbeeld-ukb.xml";
       // String pathToKafFile = "/Users/piek/Desktop/NWR/NWR-SRL/wikinews-nl/files/14369_Airbus_offers_funding_to_search_for_black_boxes_from_Air_France_disaster.ukb.kaf";
        String pathToKafFile = "";
        //pathToKafFile = "/Tools/nwr-dutch-pipeline/vua-ontotagger-v1.0/nl.demo.naf";
       // pathToKafFile = "/Users/piek/Desktop/NWR/NWR-SRL/wikinews-nl/files/14369_Airbus_offers_funding_to_search_for_black_boxes_from_Air_France_disaster.ukb.kaf";
        String pathToMatrixFile = "";
        //pathToMatrixFile = "/Tools/nwr-dutch-pipeline/vua-ontotagger-v1.0/resources/PredicateMatrix.v1.3.txt.role.odwn";
        String pathToGrammaticalVerbsFile = "";
        //pathToGrammaticalVerbsFile = "/Tools/ontotagger-v1.0/resources/grammaticals/Grammatical-words.nl";
        String pmVersion = "";
        pmVersion = "1.1";
        boolean ili = false;
        String pos = "";
        String prefix = "";
        String key = "";
        //key = "odwn-eq";
        String format = "naf";
        String[] selectedMappings = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--naf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--pos")) && (args.length>(i+1))) {
                pos = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--predicate-matrix")) && (args.length>(i+1))) {
                pathToMatrixFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--grammatical-words")) && (args.length>(i+1))) {
                pathToGrammaticalVerbsFile = args[i+1];
            }

            else if ((arg.equalsIgnoreCase("--version")) && (args.length>(i+1))) {
                pmVersion = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--key")) && (args.length>(i+1))) {
                key = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--ignore-prefix")) && (args.length>(i+1))) {
                prefix = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--mappings")) && (args.length>(i+1))) {
                selectedMappings = args[i+1].split(";");
            }
            else if ((arg.equalsIgnoreCase("--ili"))) {
                ili = true;
            }
        }
        if (ili) {
            resources.processMatrixFileWithWordnetILI(pathToMatrixFile);
        }
        else if (!key.isEmpty()) {
            resources.processMatrixFile(pathToMatrixFile, key, prefix);
           // System.out.println("resources = " + resources.wordNetPredicateMap.size());
        }
        else {
            resources.processMatrixFileWithWordnetLemma(pathToMatrixFile);
        }

        if (!pathToGrammaticalVerbsFile.isEmpty()) {
            resources.processGrammaticalWordsFile(pathToGrammaticalVerbsFile);
        }
        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;

        KafSaxParser kafSaxParser = new KafSaxParser();
        if (pathToKafFile.isEmpty()) {
            kafSaxParser.parseFile(System.in);
        }
        else {

            if (pathToKafFile.toLowerCase().endsWith(".gz")) {
                try {
                    InputStream fileStream = new FileInputStream(pathToKafFile);
                    InputStream gzipStream = new GZIPInputStream(fileStream);
                    kafSaxParser.parseFile(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (pathToKafFile.toLowerCase().endsWith(".bz2")) {
                try {
                    InputStream fileStream = new FileInputStream(pathToKafFile);
                    InputStream gzipStream = new CBZip2InputStream(fileStream);
                    kafSaxParser.parseFile(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                kafSaxParser.parseFile(pathToKafFile);
            }
        }
        processKafFileWordnetNetSynsets(kafSaxParser, pmVersion, resources, selectedMappings);

        strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
        kafSaxParser.getKafMetaData().addLayer(layer, lp);


        if (format.equalsIgnoreCase("naf")) {
            kafSaxParser.writeNafToStream(System.out);
            /*
            try {
                OutputStream fos = new FileOutputStream("/Tools/ontotagger-v1.0/naf-example/89007714_06.ont.srl.naf");
                kafSaxParser.writeNafToStream(fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
        else if (format.equalsIgnoreCase("kaf")) {
            kafSaxParser.writeKafToStream(System.out);
        }
    }

    static public void processKafFileVerbNet (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String pmVersion, String pos, String [] selectedMappings) {
        kafSaxParser.parseFile(pathToKafFile);
         for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    !kafTerm.getLemma().isEmpty() &&
                    (resources.verbNetPredicateMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<ArrayList<String>> mappings = resources.verbNetPredicateMap.get(kafTerm.getLemma());
                for (int j = 0; j < mappings.size(); j++) {
                    ArrayList<String> mapping = mappings.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(pmVersion);
                    kafSense.setSensecode(mapping.get(0));//// we assume that the first mapping represents the sensCode
                    for (int k = 0; k < mapping.size(); k++) {
                        String s = mapping.get(k);
                        if (checkMappings(selectedMappings, s)) {
                            String resource = s.substring(0, 2);
                            KafSense child = new KafSense();
                            child.setResource(resource);
                            child.setSensecode(s);
                            kafSense.addChildren(child);
                        }
                    }
                    kafTerm.addSenseTag(kafSense);
                }

            }
        }
    }

    static public void processKafFileWordnetNetSenseKeys (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String pmVersion, String pos, String[] selectedMappings) {
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    !kafTerm.getLemma().isEmpty() &&
                    (resources.wordNetLemmaSenseMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<String> senses = resources.wordNetLemmaSenseMap.get(kafTerm.getLemma());
                for (int j = 0; j < senses.size(); j++) {
                    String senseKey = senses.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(pmVersion);
                    kafSense.setSensecode(senseKey);
                    if (resources.wordNetPredicateMap.containsKey(senseKey)) {
                        ArrayList<ArrayList<String>> mappings = resources.wordNetPredicateMap.get(senseKey);
                        KafSense mChild = new KafSense ();
                        for (int m = 0; m < mappings.size(); m++) {
                            ArrayList<String> mapping =  mappings.get(m);
                            for (int k = 1; k < mapping.size(); k++) {
                                String s = mapping.get(k);
                                if (checkMappings(selectedMappings, s)) {
                                    String resource = s.substring(0, 2);
                                    KafSense child = new KafSense();
                                    child.setResource(resource);
                                    child.setSensecode(s);
                                    mChild.addChildren(child);
                                }
                            }
                        }
                        kafSense.addChildren(mChild);

                    }
                    kafTerm.addSenseTag(kafSense);
                }

            }
        }
    }

    static public void processKafFileWordnetNetLemmas (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String pmVersion, String pos, String[] selectedMappings) {
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    !kafTerm.getLemma().isEmpty() &&
                    (resources.wordNetLemmaSenseMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<String> senses = resources.wordNetLemmaSenseMap.get(kafTerm.getLemma());
                for (int j = 0; j < senses.size(); j++) {
                    String synsetId = senses.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(pmVersion);
                    kafSense.setSensecode(synsetId);
                    boolean matchingSense = false;
                    for (int k = 0; k < kafTerm.getSenseTags().size(); k++) {
                        KafSense givenKafSense = kafTerm.getSenseTags().get(k);
                        if (givenKafSense.getSensecode().equals(synsetId)) {
                           kafSense = givenKafSense;
                           matchingSense = true;
                           break;
                        }
                    }
                    if (resources.wordNetPredicateMap.containsKey(synsetId)) {
                        ArrayList<ArrayList<String>> mappings = resources.wordNetPredicateMap.get(synsetId);
                        KafSense mChild = new KafSense ();
                        for (int m = 0; m < mappings.size(); m++) {
                            ArrayList<String> mapping =  mappings.get(m);
                            for (int k = 1; k < mapping.size(); k++) {
                                String s = mapping.get(k);
                                if (checkMappings(selectedMappings, s)) {
                                    String resource = s.substring(0, 2);
                                    KafSense child = new KafSense();
                                    child.setResource(resource);
                                    child.setSensecode(s);
                                    mChild.addChildren(child);
                                }
                            }
                        }
                        kafSense.addChildren(mChild);
                    }
                    if (!matchingSense) {
                        kafTerm.addSenseTag(kafSense);
                    }
                }

            }
        }
    }


    static boolean checkMappings (String[] mappings, String m) {
        if (mappings==null) {
            return true;
        }
        for (int l = 0; l < mappings.length; l++) {
            String selectedMapping = mappings[l];
            if (m.equalsIgnoreCase(selectedMapping)) {
                return true;
            }
        }
        return false;
    }

    static public void processKafFileWordnetNetSynsets (KafSaxParser kafSaxParser, String pmVersion, Resources resources, String[] selectedMappings) {
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if (resources.grammaticalWords.contains(kafTerm.getLemma())) {
                KafSense child = new KafSense();
                child.setSensecode("grammatical");
                kafTerm.addSenseTag(child);
            }
            else {
                for (int j = 0; j < kafTerm.getSenseTags().size(); j++) {
                    KafSense kafSense = kafTerm.getSenseTags().get(j);
                    mappSense(resources, kafSense, pmVersion, selectedMappings);
                }
                for (int j = 0; j < kafTerm.getComponents().size(); j++) {
                    TermComponent termComponent = kafTerm.getComponents().get(j);
                    for (int k = 0; k < termComponent.getSenseTags().size(); k++) {
                        KafSense kafSense = termComponent.getSenseTags().get(k);
                        mappSense(resources, kafSense, pmVersion, selectedMappings);

                    }
                }
            }
        }
    }

    static public void processKafFileCorefWordnetNetSynsets (KafSaxParser kafSaxParser, String pmVersion, Resources resources, String[] selectedMappings) {
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            for (int j = 0; j < kafCoreferenceSet.getExternalReferences().size(); j++) {
                KafSense kafSense = kafCoreferenceSet.getExternalReferences().get(j);
                mappSense(resources, kafSense, pmVersion, selectedMappings);
            }
        }
    }

    static public void processExtendCorefForWordnetNetSynsets (KafSaxParser kafSaxParser, String pmVersion, Resources resources, String[] selectedMappings) {
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            ArrayList<KafSense> concepts = new ArrayList<KafSense>();
            for (int j = 0; j < kafCoreferenceSet.getExternalReferences().size(); j++) {
                 KafSense kafSense = kafCoreferenceSet.getExternalReferences().get(j);
                ArrayList<KafSense> myconcepts = addSense(resources, kafSense, pmVersion, selectedMappings);
                for (int k = 0; k < myconcepts.size(); k++) {
                    KafSense sense = myconcepts.get(k);
                    boolean match = false;
                    for (int l = 0; l < concepts.size(); l++) {
                        KafSense kafSense1 = concepts.get(l);
                        if (sense.getSensecode().equals(kafSense1.getSensecode())) {
                            match = true; break;
                        }
                    }
                    if (!match) concepts.add(sense);
                }
            }
            for (int j = 0; j < concepts.size(); j++) {
                KafSense kafSense = concepts.get(j);
                kafCoreferenceSet.addExternalReferences(kafSense);
            }
        }
    }


    static void mappSense (Resources resources, KafSense givenKafSense, String pmVersion, String[] selectedMappings) {
        String senseCode = givenKafSense.getSensecode();
        if (!resources.wordNetPredicateMap.containsKey(givenKafSense.getSensecode())) {
            if (senseCode.startsWith("nld-")) {
                int idx = senseCode.indexOf("_"); //nld-21-d_v-3939-v
                if (idx>-1) {
                    senseCode = senseCode.substring(idx-1);  //d_v-3939-v
                }
            }
        }
        if (resources.wordNetPredicateMap.containsKey(senseCode)) {

            ArrayList<ArrayList<String>> mappings = resources.wordNetPredicateMap.get(senseCode);
            for (int m = 0; m < mappings.size(); m++) {
                boolean match = false;
                KafSense mChild = new KafSense ();
                mChild.setResource("predicate-matrix");
                mChild.setSensecode(pmVersion);
                ArrayList<String> mapping =  mappings.get(m);
                for (int k = 1; k < mapping.size(); k++) {
                    String s = mapping.get(k);
                    if (checkMappings(selectedMappings, s)) {
                        match = true;
                        int idx = s.indexOf(":");
                        String resource = "";
                        if (idx > -1) {
                            resource = s.substring(0, idx);
                        }
                        KafSense child = new KafSense();
                        child.setResource(resource);
                        child.setSensecode(s);
                        mChild.addChildren(child);
                    }
                }
                if (match) {
                  //  System.out.println("givenKafSense = " + givenKafSense.getSensecode());
                    givenKafSense.addChildren(mChild);
                }
            }

        }
        else {
          //  System.out.println("cannot find senseCode = " + senseCode);
        }
    }

    static ArrayList<KafSense> addSense (Resources resources, KafSense givenKafSense, String pmVersion, String[] selectedMappings) {
        ArrayList<KafSense> concepts = new ArrayList<KafSense>();

        String senseCode = givenKafSense.getSensecode();
        if (!resources.wordNetPredicateMap.containsKey(givenKafSense.getSensecode())) {
            if (senseCode.startsWith("nld-")) {
                int idx = senseCode.indexOf("_"); //nld-21-d_v-3939-v
                if (idx>-1) {
                    senseCode = senseCode.substring(idx-1);  //d_v-3939-v
                }
            }
        }
        if (resources.wordNetPredicateMap.containsKey(senseCode)) {
            ArrayList<String> coveredMappings = new ArrayList<String>();
            ArrayList<ArrayList<String>> mappings = resources.wordNetPredicateMap.get(senseCode);
            for (int m = 0; m < mappings.size(); m++) {
                ArrayList<String> mapping =  mappings.get(m);
                for (int k = 1; k < mapping.size(); k++) {
                    String s = mapping.get(k);
                    int idx = s.indexOf(":");
                    String resource = s;
                    if (idx > -1) {
                        resource = s.substring(0, idx);
                    }
                    if (checkMappings(selectedMappings, resource) && !coveredMappings.contains(resource)) {
                        coveredMappings.add(resource); /// prevent multiple fields that share prefix, take first
                        KafSense child = new KafSense();
                        child.setResource(resource);
                        child.setSensecode(s);
                        concepts.add(child);
                    }
                }
            }

        }
        else {
          //  System.out.println("cannot find senseCode = " + senseCode);
        }
        return concepts;
    }

}
