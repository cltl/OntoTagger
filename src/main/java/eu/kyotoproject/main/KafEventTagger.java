package eu.kyotoproject.main;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.util.Resources;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class KafEventTagger {

    static public void main (String[] args) {
        Resources resources = new Resources();
        String pathToKafFile = "";
        String pathToMatrixFile = "";
        String version = "";
        String pos = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--pos")) && (args.length>(i+1))) {
                pos = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--predicate-matrix")) && (args.length>(i+1))) {
                pathToMatrixFile = args[i+1];
                resources.processMatrixFileWithWordnetSynset(pathToMatrixFile);
            }
            else if ((arg.equalsIgnoreCase("--version")) && (args.length>(i+1))) {
                version = args[i+1];
            }
        }
        KafSaxParser kafSaxParser = new KafSaxParser();
        processKafFileWordnetNetSynsets(kafSaxParser, pathToKafFile, resources, version, pos);
        kafSaxParser.writeKafToStream(System.out);
    }

    static public void processKafFileVerbNet (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String version, String pos) {
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    (resources.verbNetPredicateMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<ArrayList<String>> mappings = resources.verbNetPredicateMap.get(kafTerm.getLemma());
                for (int j = 0; j < mappings.size(); j++) {
                    ArrayList<String> mapping = mappings.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(version);
                    kafSense.setSensecode(mapping.get(0));
                    for (int k = 1; k < mapping.size(); k++) {
                        String s = mapping.get(k);
                        String resource = s.substring(0, 2);
                        KafSense child = new KafSense();
                        child.setResource(resource);
                        child.setSensecode(s);
                        kafSense.addChildren(child);
                    }
                    kafTerm.addSenseTag(kafSense);
                }

            }
        }
    }

    static public void processKafFileWordnetNetSenseKeys (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String version, String pos) {
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    (resources.wordNetLemmaSenseMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<String> senses = resources.wordNetLemmaSenseMap.get(kafTerm.getLemma());
                for (int j = 0; j < senses.size(); j++) {
                    String senseKey = senses.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(version);
                    kafSense.setSensecode(senseKey);
                    if (resources.wordNetPredicateMap.containsKey(senseKey)) {
                        ArrayList<String> mapping = resources.wordNetPredicateMap.get(senseKey);
                        for (int k = 1; k < mapping.size(); k++) {
                            String s = mapping.get(k);
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

    static public void processKafFileWordnetNetSynsets (KafSaxParser kafSaxParser, String pathToKafFile, Resources resources, String version, String pos) {
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if ((pos.isEmpty() || (kafTerm.getPos().toLowerCase().startsWith(pos))) &&
                    (resources.wordNetLemmaSenseMap.containsKey(kafTerm.getLemma()))) {
                ArrayList<String> senses = resources.wordNetLemmaSenseMap.get(kafTerm.getLemma());
                for (int j = 0; j < senses.size(); j++) {
                    String synsetId = senses.get(j);
                    KafSense kafSense = new KafSense();
                    kafSense.setResource(version);
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
                        ArrayList<String> mapping = resources.wordNetPredicateMap.get(synsetId);
                        for (int k = 1; k < mapping.size(); k++) {
                            String s = mapping.get(k);
                            String resource = s.substring(0, 2);
                            KafSense child = new KafSense();
                            child.setResource(resource);
                            child.setSensecode(s);
                            kafSense.addChildren(child);
                        }
                    }
                    if (!matchingSense) {
                        kafTerm.addSenseTag(kafSense);
                    }
                }

            }
        }
    }




}
