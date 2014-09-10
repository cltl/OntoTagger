package eu.kyotoproject.main;

import eu.kyotoproject.kaf.*;
import eu.kyotoproject.rdf.SenseFrameRoles;
import eu.kyotoproject.util.GetDominantMapping;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 9/8/14.
 */
public class SrlFrameNetTagger {

    /**
     *         <externalRef confidence="0.165911" reference="nld-21-d_n-36759-n" resource="cdb2.0-nld-all.infv.0.0.no-allwords">
     <externalRef resource="predicate-matrix1.1">
     <externalRef reference="fn:Fluidic_motion" resource="fn"/>
     <externalRef reference="fn-role:Area" resource="fn-role"/>
     <externalRef reference="fn-role:Fluid" resource="fn-role"/>
     <externalRef reference="fn:flow.v" resource="fn"/>
     <externalRef reference="pb:flow.01" resource="pb"/>
     <externalRef reference="fn-role:Goal" resource="fn-role"/>
     <externalRef reference="fn-pb-role:Fluid#1" resource="fn-pb-role"/>
     <externalRef reference="FN_MAPPING;SYNONYMS" resource=""/>
     </externalRef>
     */

    static final String layer = "terms";
    static final String name = "vua-framenet-srl-tagger";
    static final String version = "1.0";

    static public void main (String[] args) {
        String fns = "fn:";
        String [] rnss = {"fn-role:", "pb-role:", "fn-pb-role:"};
        //String pathToKafFile = "/Tools/ontotagger-v1.0/naf-example/spinoza-voorbeeld-ukb.ont.xml";
        String pathToKafFile = "/Tools/ontotagger-v1.0/naf-example/89007714_06.tok.alpino.ner.ukb.pm.ht.srl.naf";
        Double confidenceThreshold = new Double(0.25);
        Integer frameThreshold = new Integer(70);
        String format = "naf";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--naf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--frame-ns")) && (args.length>(i+1))) {
                fns = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--role-ns")) && (args.length>(i+1))) {
                rnss = args[i+1].split(";");
            }
            else if ((arg.equalsIgnoreCase("--format")) && (args.length>(i+1))) {
                format = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--sense-conf")) && (args.length>(i+1))) {
                try {
                    confidenceThreshold = Double.parseDouble(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if ((arg.equalsIgnoreCase("--frame-conf")) && (args.length>(i+1))) {
                try {
                    frameThreshold = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;

        KafSaxParser kafSaxParser = new KafSaxParser();
        processSrlLayer(kafSaxParser, pathToKafFile,fns, rnss, confidenceThreshold.doubleValue(), frameThreshold.intValue());

        strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate);
        kafSaxParser.getKafMetaData().addLayer(name, lp);
        if (format.equalsIgnoreCase("naf")) {
           // kafSaxParser.writeNafToStream(System.out);
            try {
                OutputStream fos = new FileOutputStream("/Tools/ontotagger-v1.0/naf-example/89007714_06.ont.srl.naf");
                kafSaxParser.writeNafToStream(fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (format.equalsIgnoreCase("kaf")) {
            kafSaxParser.writeKafToStream(System.out);
        }
    }

    static public void processSrlLayer (KafSaxParser kafSaxParser, 
                                        String pathToKafFile,
                                        String fns,
                                        String [] rnss,
                                        double confidenceThreshold,
                                        int framethreshold) {
        kafSaxParser.parseFile(pathToKafFile);
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent event = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < event.getSpanIds().size(); j++) {
                String termId = event.getSpanIds().get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(termId);
                if (kafTerm!=null) {
                    HashMap<String, ArrayList<SenseFrameRoles>> frameMap = GetDominantMapping.getFrameMap(kafTerm, confidenceThreshold, fns, rnss);
                    if (frameMap.size()>0) System.out.println("frameMap.size() = " + frameMap.size());
                    double topscore = GetDominantMapping.getTopScore(frameMap);
                    Set keySet = frameMap.keySet();
                    Iterator<String> keys = keySet.iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                       // System.out.println("frame = " + key);
                        double score = 0;
                        ArrayList<SenseFrameRoles> data = frameMap.get(key);
                        for (int f = 0; f < data.size(); f++) {
                            SenseFrameRoles senseFrameRoles = data.get(f);
                            score += senseFrameRoles.getConfidence();
                        }
                        if ((100*(score/topscore))>framethreshold) {
                            KafSense frame = new KafSense();
                            frame.setSensecode(key);
                            frame.setConfidence(score / topscore);
                            frame.setConfidence(data.size());
                            frame.setConfidence(score);
                            event.addExternalReferences(frame);
                            for (int k = 0; k < data.size(); k++) {
                                SenseFrameRoles senseFrameRoles = data.get(k);
                                KafSense sense = new KafSense();
                                sense.setSensecode(senseFrameRoles.getSense());
                                sense.setConfidence(senseFrameRoles.getConfidence());
                                sense.setResource(senseFrameRoles.getResource());
                                event.addExternalReferences(sense);
                            }
                            for (int k = 0; k < event.getParticipants().size(); k++) {
                                KafParticipant kafParticipant = event.getParticipants().get(k);
                                String role = kafParticipant.getRole();
                                for (int l = 0; l < data.size(); l++) {
                                    SenseFrameRoles senseFrameRoles = data.get(l);
                                    for (int m = 0; m < senseFrameRoles.getRoles().size(); m++) {
                                        String fnPbRole = senseFrameRoles.getRoles().get(m);
                                        String fnRole = matchPropBankFrameNetRole(fnPbRole, role);
                                        if (!fnRole.isEmpty()) {
                                            KafSense kafSense = new KafSense();
                                            kafSense.setSensecode(fnRole);
                                            kafParticipant.addExternalReferences(kafSense);
                                        }
                                    }
                                }
                            }
                            //// now check to participants of the events to add the roles
                            //System.out.println("score = " + score);
                            //System.out.println(data.toString());
                        }
                    }

                }

            }
        }
    }

    static String matchPropBankFrameNetRole (String fnPbRole, String pbRole) {
        if (pbRole.toLowerCase().startsWith("arg")) {
            int idx = fnPbRole.lastIndexOf("#");
            if (idx > -1) {
                String suffix = fnPbRole.substring(idx+1);
/*                System.out.println("suffix = " + suffix);
                System.out.println("pbRole = " + pbRole);
                System.out.println("fnPbRole = " + fnPbRole);*/
                if (pbRole.endsWith(suffix)) {
                    return fnPbRole.substring(0, idx);
                }
            }
        }
        return "";
    }
}
