package eu.kyotoproject.util;

import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.TermComponent;
import eu.kyotoproject.rdf.SenseFrameRoles;

import java.util.*;

/**
 * Created by piek on 9/8/14.
 */
public class GetDominantMapping {

    /**
     *
     *         <externalRef confidence="0.158284" reference="nld-21-d_v-137-v" resource="cdb2.0-nld-all.infv.0.0.no-allwords">
                    <externalRef resource="predicate-matrix1.1">
                        <externalRef reference="fn:Adjusting" resource="fn"/>
                        <externalRef reference="pb:adapt.01" resource="pb"/>
                        <externalRef reference="fn-pb-role:Agent#0" resource="fn-pb-role"/>
                        <externalRef reference="fn-pb-role:Place#2" resource="fn-pb-role"/>
                        <externalRef reference="fn-pb-role:Agent#1" resource="fn-pb-role"/>
                    </externalRef>
                     <externalRef resource="predicate-matrix1.1">
                        <externalRef reference="pb:adjust.01" resource="pb"/>
                     </externalRef>
                     <externalRef resource="predicate-matrix1.1">
                         <externalRef reference="fn:Compliance" resource="fn"/>
                         <externalRef reference="pb:conform.01" resource="pb"/>
                         <externalRef reference="fn-pb-role:Protagonist#0" resource="fn-pb-role"/>
                         <externalRef reference="fn-pb-role:Act#2" resource="fn-pb-role"/>
                         <externalRef reference="fn-pb-role:Protagonist#1" resource="fn-pb-role"/>
                     </externalRef>
                </externalRef>
                 <externalRef confidence="0.152045" reference="nld-21-d_v-7203-v" resource="cdb2.0-nld-all.infv.0.0.no-allwords">
                     <externalRef resource="predicate-matrix1.1">
                         <externalRef reference="fn:Statement" resource="fn"/>
                         <externalRef reference="pb:add.02" resource="pb"/>
                         <externalRef reference="fn-pb-role:Speaker#0" resource="fn-pb-role"/>
                         <externalRef reference="fn-pb-role:Addressee#2" resource="fn-pb-role"/>
                         <externalRef reference="fn-pb-role:Addressee#1" resource="fn-pb-role"/>
                         <externalRef reference="fn-role:Speaker" resource="fn-role"/>
                         <externalRef reference="fn-role:Addressee" resource="fn-role"/>
                         <externalRef reference="fn-role:Topic" resource="fn-role"/>
                     </externalRef>
                 </externalRef>
                 <externalRef confidence="0.0510565" reference="nld-21-d_v-8512-v" resource="cdb2.0-nld-all.infv.0.0.no-allwords"/>
     </externalReferences>
     */

    static public HashMap<String, ArrayList<SenseFrameRoles>> getFrameMap (KafTerm kafTerm, double confidenceThreshold,
                               String fns, String[] rnss, String ilins) {
        HashMap<String, ArrayList<SenseFrameRoles>> frameMap = new HashMap<String, ArrayList<SenseFrameRoles>>();
        //// we collect all the frames and data for all the senses
        for (int j = 0; j < kafTerm.getSenseTags().size(); j++) {
            KafSense kafSense = kafTerm.getSenseTags().get(j);
            if (kafSense.getConfidence()>=confidenceThreshold) {
                GetDominantMapping.getFrames(frameMap, kafSense, fns, rnss, ilins);
            }
        }
        for (int j = 0; j < kafTerm.getComponents().size(); j++) {
            TermComponent termComponent = kafTerm.getComponents().get(j);
            for (int k = 0; k < termComponent.getSenseTags().size(); k++) {
                KafSense kafSense = termComponent.getSenseTags().get(k);
                if (kafSense.getConfidence()>=confidenceThreshold) {
                    GetDominantMapping.getFrames(frameMap, kafSense, fns, rnss, ilins);
                }
            }
        }
        return frameMap;
    }

    static public double getTopScore (HashMap<String, ArrayList<SenseFrameRoles>> frameMap) {
        double topscore = 0;
        Set keySet = frameMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            double score = 0;
            ArrayList<SenseFrameRoles> data = frameMap.get(key);
            for (int j = 0; j < data.size(); j++) {
                SenseFrameRoles senseFrameRoles = data.get(j);
                score += senseFrameRoles.getConfidence();
            }
            if (score>topscore) topscore = score;
        }
        return topscore;
    }


    static public void getFrames (HashMap<String, ArrayList<SenseFrameRoles>> frameMap,
                           KafSense kafSense,
                           String fns, String[] rns, String ilins) {
       // System.out.println("kafSense.getSensecode() = " + kafSense.getSensecode());
        for (int i = 0; i < kafSense.getChildren().size(); i++) {
            KafSense child = kafSense.getChildren().get(i);
            //     <externalRef resource="predicate-matrix1.1">
            //System.out.println("child = " + child.getResource());
            String frame = "";
            SenseFrameRoles senseFrameRoles = new SenseFrameRoles();
            /// we first look for the frame that matches the names space prefix
            String iliReference = "";
            for (int j = 0; j < child.getChildren().size(); j++) {
                KafSense grandChild = child.getChildren().get(j);
                //System.out.println("grandChild.getSensecode() = " + grandChild.getSensecode());
                ///these are the mappings found
                //<externalRef reference="mcr:ili-30-02604760-v" resource="mcr"/>
                if (grandChild.getSensecode().startsWith(ilins)) {
                    int idx = grandChild.getSensecode().indexOf(":");
                    if (idx>-1) {
                        iliReference = grandChild.getSensecode().substring(idx+1);
                    }
                    else {
                        iliReference = grandChild.getSensecode();
                    }
                    break;
                }
            }

            for (int j = 0; j < child.getChildren().size(); j++) {
                KafSense grandChild = child.getChildren().get(j);
                //System.out.println("grandChild.getSensecode() = " + grandChild.getSensecode());
                ///these are the mappings found
                if (grandChild.getSensecode().startsWith(fns)) {
                    senseFrameRoles = new SenseFrameRoles();
                    senseFrameRoles.setSense(kafSense.getSensecode());
                    senseFrameRoles.setConfidence(kafSense.getConfidence());
                    senseFrameRoles.setResource(kafSense.getResource());
                    senseFrameRoles.setFrame(grandChild.getSensecode());
                    senseFrameRoles.setIli(iliReference);
                    frame = senseFrameRoles.getFrame();
                    break;
                }
                else {
                  //  System.out.println("grandChild.getSensecode() = " + grandChild.getSensecode());
                }
            }
            if (!frame.isEmpty()) {
                /// we add all the roles and store it in the map
                for (int j = 0; j < child.getChildren().size(); j++) {
                    KafSense grandChild = child.getChildren().get(j);
                    for (int k = 0; k < rns.length; k++) {
                        String rn = rns[k];
                        if (grandChild.getSensecode().startsWith(rn)) {
                            senseFrameRoles.addRoles(grandChild.getSensecode());
                        }
                    }
                    if (grandChild.getSensecode().startsWith("eso:")) {
                       if (grandChild.getSensecode().toLowerCase().equals(grandChild.getSensecode())) {
                           /// this is a lowercase role
                           senseFrameRoles.addEsoRoles(grandChild.getSensecode());
                       }else {
                           senseFrameRoles.addEsoClasses(grandChild.getSensecode());
                       }
                    }
                }
                if (frameMap.containsKey(frame)) {
                    ArrayList<SenseFrameRoles> data = frameMap.get(frame);
                    data.add(senseFrameRoles);
                    frameMap.put(frame, data);
                }
                else {
                    ArrayList<SenseFrameRoles> data = new ArrayList<SenseFrameRoles>();
                    data.add(senseFrameRoles);
                    frameMap.put(frame, data);
                }
            }
            else {
                ////
                if (!iliReference.isEmpty()) {
                    ArrayList<SenseFrameRoles> data = new ArrayList<SenseFrameRoles>();
                    frameMap.put(iliReference, data);
                }

            }

        }
/*        if (frameMap.containsKey(child.getSensecode())) {
            ArrayList<SenseFrameRoles> data = frameMap.get(child.getSensecode());
            senseFrameRoles.setConfidence(conf);

        }
        else {
        }*/

        //System.out.println("s = " + s);
      //  System.out.println("kafSense.toString() = " + kafSense.toString());
/*
        for (int i = 0; i < kafSense.getChildren().size(); i++) {
            KafSense sense = kafSense.getChildren().get(i);
            countFrames(cnts, sense, ns, top);
        }*/
    }

    static void countFrames (HashMap<String, Integer> cnts, KafSense kafSense, String ns, Integer top) {
        String s = kafSense.getSensecode();
        //System.out.println("s = " + s);
      //  System.out.println("kafSense.toString() = " + kafSense.toString());
        if (s.startsWith(ns)) {
            if (cnts.containsKey(s)) {
                Integer cnt = cnts.get(s);
                cnt++;
                if (cnt>top) top = cnt;
                cnts.put(s,cnt);
            }
            else {
                cnts.put(s, 1);
            }
        }
        for (int i = 0; i < kafSense.getChildren().size(); i++) {
            KafSense sense = kafSense.getChildren().get(i);
            countFrames(cnts, sense, ns, top);
        }
    }

    /**
     *
     * @param term
     */
    static public ArrayList<String> getDominantTags (KafTerm term, String ns, int threshold) {
        ArrayList<String> topTags = new ArrayList<String>();
        Integer top = 1;
        HashMap<String, Integer> cnts = new HashMap<String, Integer>();
        for (int i = 0; i < term.getSenseTags().size(); i++) {
            KafSense kafSense = term.getSenseTags().get(i);
            countFrames(cnts, kafSense, ns, top);
        }
        for (int j = 0; j < term.getComponents().size(); j++) {
            TermComponent termComponent = term.getComponents().get(j);
            for (int k = 0; k < termComponent.getSenseTags().size(); k++) {
                KafSense kafSense = termComponent.getSenseTags().get(k);
                countFrames(cnts, kafSense, ns, top);
            }
        }

        Set keySet = cnts.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Integer cnt = cnts.get(key);
            if (((100*cnt)/top)>threshold) {
                topTags.add(key);
            }
        }
        return topTags;
    }

    static void getRoles (ArrayList<String> roles, KafSense kafSense, String frame, String [] nss) {
        for (int i = 0; i < kafSense.getChildren().size(); i++) {
            KafSense child = kafSense.getChildren().get(i);
            if (child.getSensecode().equals(frame)) {
                /// we now know this set of mappings includes a matching frame
               // System.out.println("frame.getSensecode() = " + child.getSensecode());
                for (int j = 0; j < kafSense.getChildren().size(); j++) {
                    if (j!=i) {
                        KafSense sense = kafSense.getChildren().get(j);
                        for (int n = 0; n < nss.length; n++) {
                            String ns = nss[n];
                           // System.out.println("ns = " + ns);
                           // System.out.println("sense.getSensecode() = " + sense.getSensecode());
                            if (sense.getSensecode().startsWith(ns)) {
                                if (!roles.contains(sense.getSensecode())) {
                                    roles.add(sense.getSensecode());
                                }
                            }
                        }
                    }
                }
            }
            getRoles(roles, child, frame, nss);
        }
    }

    static public ArrayList<String> getFrameRoles (KafTerm term, String frame, String[] nss) {
        ArrayList<String> roles = new ArrayList<String>();
        for (int i = 0; i < term.getSenseTags().size(); i++) {
            KafSense kafSense = term.getSenseTags().get(i);
            getRoles(roles, kafSense, frame, nss);
        }
        for (int j = 0; j < term.getComponents().size(); j++) {
            TermComponent termComponent = term.getComponents().get(j);
            for (int k = 0; k < termComponent.getSenseTags().size(); k++) {
                KafSense kafSense = termComponent.getSenseTags().get(k);
                getRoles(roles, kafSense, frame, nss);
            }
        }
        return roles;
    }
}
