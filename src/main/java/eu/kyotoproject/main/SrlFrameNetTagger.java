package eu.kyotoproject.main;

import eu.kyotoproject.kaf.*;
import eu.kyotoproject.rdf.SenseFrameRoles;
import eu.kyotoproject.util.GetDominantMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
        String fns = "";
        String ilins = "";
        String [] rnss = null;
        String pathToKafFile = "";
        Double confidenceThreshold = -1.0;
        Integer frameThreshold = -1;
        String format = "";

/*
        fns = "fn:";
        ilins = "mcr:ili";
        String [] rnss = {"fn-role:", "pb-role:", "fn-pb-role:"};
        pathToKafFile = "/Tools/nwr-dutch-pipeline/vua-ontotagger-v1.0/example/test.srl.lexicalunits.pm.naf";
        // pathToKafFile = "/Tools/ontotagger-v1.0/naf-example/spinoza-voorbeeld-ukb.ont.xml";
        // pathToKafFile = "/Tools/ontotagger-v1.0/naf-example/89007714_06.tok.alpino.ner.ukb.pm.ht.srl.naf";
        confidenceThreshold = new Double(0.25);
        frameThreshold = new Integer(70);
        format = "naf";
*/
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
            else if ((arg.equalsIgnoreCase("--ili-ns")) && (args.length>(i+1))) {
                ilins = args[i+1];
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
        processSrlLayer(kafSaxParser, pathToKafFile, fns, rnss,ilins,  confidenceThreshold.doubleValue(), frameThreshold.intValue());

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
/*            try {
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

    static String getResourceFromSenseCode (KafSense kafSense) {
        String resource = kafSense.getResource();
        int idx = kafSense.getSensecode().indexOf(":");
      //  System.out.println("kafSense = " + kafSense.getSensecode());
        if (idx>-1) {
            String ns = kafSense.getSensecode().substring(0, idx);
          //  System.out.println("ns = " + ns);
            if (ns.toLowerCase().equals("fn")) {
                resource = "FrameNet";
            }
            else if (ns.toLowerCase().equals("pb")) {
                resource = "ProbBank";
            }
            else if (ns.toLowerCase().equals("fn-role")) {
                resource = "FrameNet";
            }
            else if (ns.toLowerCase().equals("fn-pb-role")) {
                resource = "FrameNet";
            }
            else if (ns.toLowerCase().equals("pb-role")) {
                resource = "PropBank";
            }
            else if (ns.toLowerCase().equals("vn-role")) {
                resource = "VerbNet";
            }
            else if (ns.toLowerCase().equals("nb")) {
                resource = "NomBank";
            }
            else if (ns.toLowerCase().equals("vn")) {
                resource = "VerbNet";
            }
            else if (ns.toLowerCase().equals("eso")) {
                resource = "ESO";
            }
            else if (ns.toLowerCase().equals("ili")) {
                resource = "WordNet";
            }
            else if (kafSense.getSensecode().toLowerCase().startsWith("mcr:ili")) {
                resource = "WordNet";
            }
        }
        return resource;
    }

    static String removeNameSpaceFromSenseCode (KafSense kafSense) {
        String reference = removeNameSpaceFromSenseString(kafSense.getSensecode());
        return reference;
    }

    static String removeNameSpaceFromSenseString (String kafSense) {
        String reference = kafSense;
        int idx = kafSense.indexOf(":");
        if (idx>-1) {
            reference = kafSense.substring(idx+1);
        }
        return reference;
    }

    static void fixExternalReference (KafSense kafSense) {
        kafSense.setResource(getResourceFromSenseCode(kafSense));
        kafSense.setSensecode(removeNameSpaceFromSenseCode(kafSense));
    }

    static public void processSrlLayer (KafSaxParser kafSaxParser, 
                                        String pathToKafFile,
                                        String fns,
                                        String [] rnss,
                                        String ilins,
                                        double confidenceThreshold,
                                        int framethreshold) {
        if (pathToKafFile.isEmpty()) {
            kafSaxParser.parseFile(System.in);
        }
        else {
            kafSaxParser.parseFile(pathToKafFile);
        }
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent event = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < event.getSpanIds().size(); j++) {
                String termId = event.getSpanIds().get(j);
                /// we are assuming that predicates have a span size of one term!!!!
                KafTerm kafTerm = kafSaxParser.getTerm(termId);
                if (kafTerm!=null) {
                    HashMap<String, ArrayList<SenseFrameRoles>> frameMap = GetDominantMapping.getFrameMap(kafTerm, confidenceThreshold, fns, rnss, ilins);
                    if (frameMap.size()>0) {
                       // System.out.println("frameMap.size() = " + frameMap.size());
                        double topscore = GetDominantMapping.getTopScore(frameMap);
                        Set keySet = frameMap.keySet();
                        Iterator<String> keys = keySet.iterator();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            // System.out.println("frame = " + key);
                            double score = 0;
                            ArrayList<SenseFrameRoles> data = frameMap.get(key);
                            if (data.size()> 0) {
                                /// we did get FN references and data so we use these for the output
                                for (int f = 0; f < data.size(); f++) {
                                    SenseFrameRoles senseFrameRoles = data.get(f);
                                    score += senseFrameRoles.getSense().getConfidence();
                                }
                                if ((100 * (score / topscore)) > framethreshold) {
                                    KafSense frame = new KafSense();
                                    frame.setSensecode(key);
                                    fixExternalReference(frame);
                                    frame.setConfidence(score / topscore);
                                    frame.setConfidence(data.size());
                                    frame.setConfidence(score);
                                    event.addExternalReferences(frame);
                                    for (int k = 0; k < data.size(); k++) {
                                        SenseFrameRoles senseFrameRoles = data.get(k);
                                        KafSense sense = new KafSense();
                                        sense.setSensecode(senseFrameRoles.getSense().getSensecode());
                                        sense.setConfidence(senseFrameRoles.getSense().getConfidence());
                                        sense.setResource(senseFrameRoles.getSense().getResource());
                                        fixExternalReference(sense);
                                        event.addExternalReferences(sense);
                                        if (!senseFrameRoles.getIli().isEmpty()) {
                                            KafSense ili = new KafSense();
                                            ili.setSensecode(senseFrameRoles.getIli());
                                            fixExternalReference(ili);
                                            event.addExternalReferences(ili);
                                        }
                                        for (int m = 0; m < senseFrameRoles.getEsoClasses().size(); m++) {
                                            String s = senseFrameRoles.getEsoClasses().get(m);
                                            KafSense kafSense = new KafSense();
                                            kafSense.setSensecode(s);
                                            fixExternalReference(kafSense);
                                            event.addExternalReferences(kafSense);
                                        }
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
                                                    fnRole = removeNameSpaceFromSenseString(fnRole);
                                                    KafSense kafSense = new KafSense();
                                                    kafSense.setSensecode(key+"@"+fnRole);
                                                    fixExternalReference(kafSense);
                                                    kafParticipant.addExternalReferences(kafSense);
                                                }
                                            }
                                            /// need to find a way to combine them with ESO classes
                                            for (int m = 0; m < senseFrameRoles.getEsoRoles().size(); m++) {
                                                String s = senseFrameRoles.getEsoRoles().get(m);
                                                KafSense kafSense = new KafSense();
                                                kafSense.setSensecode(s);
                                                fixExternalReference(kafSense);
                                                kafParticipant.addExternalReferences(kafSense);
                                            }
                                        }
                                        role = normalizePropBankRole(role);
                                        kafParticipant.setRole(role);
                                    }
                                    //// now check to participants of the events to add the roles
                                    //System.out.println("score = " + score);
                                    //System.out.println(data.toString());
                                }
                            }
                        }
                    }
                    else {
                        ///// there is no framenetmapping, so we get the top senses of the predicate
                        for (int k = 0; k < kafTerm.getSenseTags().size(); k++) {
                            KafSense kafSense = kafTerm.getSenseTags().get(j);
                            KafSense refSense = new KafSense();
                            refSense.setResource(kafSense.getResource());
                            refSense.setConfidence(kafSense.getConfidence());
                            refSense.setSensecode(kafSense.getSensecode());
                            event.addExternalReferences(refSense);
                            if (kafSense.getChildren().size()>0) {
                                //// we take the first sense which has the highest score
                                KafSense child = kafSense.getChildren().get(0);
                               // event.addExternalReferences(child);
                                for (int c = 0; c < child.getChildren().size(); c++) {
                                    KafSense grandChild = child.getChildren().get(j);
                                    //<externalRef reference="mcr:ili-30-02604760-v" resource="mcr"/>
                                    if (grandChild.getSensecode().startsWith(ilins)) {
                                        fixExternalReference(grandChild);
                                        event.addExternalReferences(grandChild);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    static String matchPropBankFrameNetRole (String fnPbRole, String pbRole) {
        ///fn-pb-role:Entity#2
        ///fn-pb-role:Content#1
        if (pbRole.toLowerCase().startsWith("arg") |
                (pbRole.startsWith("A"))) {
            int idx = fnPbRole.lastIndexOf("#");
            if (idx > -1) {
                String suffix = fnPbRole.substring(idx+1);
                if (pbRole.endsWith(suffix)) {
                    return fnPbRole.substring(0, idx);
                }
            }
            else {
            }
        }
        return "";
    }

    static String normalizePropBankRole (String pbRole) {
        String newRole = pbRole;
        if (pbRole.toLowerCase().startsWith("arg")) {
            newRole = "A"+pbRole.substring(3);
        }
        return newRole;
    }
}
