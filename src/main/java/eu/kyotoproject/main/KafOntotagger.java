package eu.kyotoproject.main;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.LP;
import eu.kyotoproject.util.Resources;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class KafOntotagger {
    static final String layer = "terms";
    static final String name = "vua-synset-ontotagger";
    static final String version = "1.0";

    static public void main (String[] args) {
        Resources resources = new Resources();
        String pathToKafFile = "";
        String pathToSynsetOntologyFile = "";
        String pathToSynsetBaseConceptFile = "";
        String pathToOntologyOntologyFile = "";
        String pathToRelationsFile = "";
        String format = "naf";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--naf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--format")) && (args.length>(i+1))) {
                format = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--synset-ontology")) && (args.length>(i+1))) {
                pathToSynsetOntologyFile = args[i+1];
                resources.processSynsetOntologyFile(pathToSynsetOntologyFile);
            }
            else if ((arg.equalsIgnoreCase("--synset-baseconcept")) && (args.length>(i+1))) {
                pathToSynsetBaseConceptFile = args[i+1];
                resources.processSynsetBaseConceptFile(pathToSynsetBaseConceptFile);
            }
            else if ((arg.equalsIgnoreCase("--ontology-ontology")) && (args.length>(i+1))) {
                pathToOntologyOntologyFile = args[i+1];
                resources.processOntologyOntologyFile(pathToOntologyOntologyFile);
            }
            else if ((arg.equalsIgnoreCase("--relations")) && (args.length>(i+1))) {
                pathToRelationsFile = args[i+1];
                resources.processRelationsFile(pathToRelationsFile);
            }
        }
        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;

        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            for (int j = 0; j < kafTerm.getSenseTags().size(); j++) {
                KafSense kafSense = kafTerm.getSenseTags().get(j);
                if (resources.synsetBaseconceptMap.containsKey(kafSense.getSensecode())) {
                    ArrayList<String> targets = resources.synsetBaseconceptMap.get(kafSense.getSensecode());
                    for (int k = 0; k < targets.size(); k++) {
                        String s = targets.get(k);
                        String [] fields = s.split("\t");
                        if (fields.length==2) {
                            KafSense externalRef = new KafSense();
                            externalRef.setRefType("base-concept");
                            externalRef.setSensecode(fields[1]);
                            if (!fields[0].isEmpty()) {
                                externalRef.setRefType(fields[0]);
                            }
                            kafSense.addChildren(externalRef);
                        }
                    }
                }

                if (resources.synsetOntologyMap.containsKey(kafSense.getSensecode())) {
                    ArrayList<String> targets = resources.synsetOntologyMap.get(kafSense.getSensecode());
                    for (int k = 0; k < targets.size(); k++) {
                        String s = targets.get(k);
                        String [] fields = s.split("\t");
                        if (fields.length==2) {
                            KafSense externalRef = new KafSense();
                            externalRef.setSensecode(fields[1]);
                            ArrayList<String> coveredClasses = new ArrayList<String>();
                            coveredClasses.add(externalRef.getSensecode());
                            if (!fields[0].isEmpty()) {
                                externalRef.setRefType(fields[0]);
                            }
                            if (resources.relationArrayList.contains(externalRef.getRefType()) || resources.relationArrayList.size()==0) {
                                if (resources.ontologyOntologyMap.containsKey(externalRef.getSensecode())) {
                                    resources.extendExternalReference(coveredClasses, externalRef);
                                }
                                kafSense.addChildren(externalRef);
                            }
                        }
                    }
                }
            }
        }
        strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate);
        kafSaxParser.getKafMetaData().addLayer(name, lp);
        if (format.equalsIgnoreCase("naf")) {
            kafSaxParser.writeNafToStream(System.out);
        }
        else if (format.equalsIgnoreCase("kaf")) {
            kafSaxParser.writeKafToStream(System.out);
        }
    }




}
