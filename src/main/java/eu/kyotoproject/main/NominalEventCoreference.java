package eu.kyotoproject.main;

import eu.kyotoproject.kaf.*;
import eu.kyotoproject.util.FrameNetLuReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class NominalEventCoreference {
	private static String pathToLemmaClasses = "";
    static final String layer = "srl";
    static final String name = "vua-nominal-events";
    static final String version = "1.0";

    static public void main (String[] args) {
    	String pathToKafFile = "";
       // pathToKafFile = "/Users/piek/Desktop/tweede-kamer/NAF-Analysis/ABN-AMRO/602597.xml.19k2ubmrl.xml";
        String pathToFrameNetLuFile = "";
      //  pathToFrameNetLuFile = "/Tools/nwr-dutch-pipeline/vua-ontotagger-v1.0/resources/nl-luIndex.xml";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ((arg.equalsIgnoreCase("--naf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if (arg.equals("--framenet-lu") && args.length>i+1) {
                pathToFrameNetLuFile = args[i+1];
            }
        }
        FrameNetLuReader frameNetLuReader = new FrameNetLuReader();
        frameNetLuReader.parseFile(pathToFrameNetLuFile);

        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;
        KafSaxParser kafSaxParser = new KafSaxParser();
        if (pathToKafFile.isEmpty()) {
            //kafSaxParser.encoding = "UTF-8";
            kafSaxParser.parseFile(System.in);
        }
        else {
            kafSaxParser.parseFile(pathToKafFile);
        }
        ArrayList<KafTerm> nominalEvents = new ArrayList<KafTerm>();
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            String [] compoundElements = kafTerm.getLemma().split("_");
            if (compoundElements.length>1 && kafTerm.getSenseTags().size()==0) {
                //// we have a compound term without concepts
                //// we check framenet to find frames for the lemma head
                String headPhrase = compoundElements[compoundElements.length-1];
                if (frameNetLuReader.lexicalUnitFrameMap.containsKey(headPhrase)) {
                    ArrayList<String> frames = frameNetLuReader.lexicalUnitFrameMap.get(headPhrase);
                   // System.out.println("frames.toString() = " + frames.toString());
                    for (int j = 0; j < frames.size(); j++) {
                        String frame = frames.get(j);
                        KafSense kafSense = new KafSense();
                        kafSense.setSensecode(frame);
                        kafSense.setResource("FrameNet");
                        kafTerm.addSenseTag(kafSense);
                    }
                    nominalEvents.add(kafTerm);
                }
            }
            else if (kafTerm.getPosIni().equalsIgnoreCase("n") && kafTerm.getSenseTags().size()==0) {
                if (frameNetLuReader.lexicalUnitFrameMap.containsKey(kafTerm.getLemma())) {
                    ArrayList<String> frames = frameNetLuReader.lexicalUnitFrameMap.get(kafTerm.getLemma());
                  //  System.out.println("frames.toString() = " + frames.toString());
                    for (int j = 0; j < frames.size(); j++) {
                        String frame = frames.get(j);
                        KafSense kafSense = new KafSense();
                        kafSense.setSensecode(frame);
                        kafSense.setResource("FrameNet");
                        kafTerm.addSenseTag(kafSense);
                    }
                    nominalEvents.add(kafTerm);
                }
            }
            else if (kafTerm.getPosIni().equalsIgnoreCase("n") && kafTerm.getSenseTags().size()>0) {
                //// check the external references for event info
                for (int j = 0; j < kafTerm.getSenseTags().size(); j++) {
                    KafSense kafSense = kafTerm.getSenseTags().get(j);
                    if (kafSense.getResource().equalsIgnoreCase("eso")) {
                        nominalEvents.add(kafTerm);
                        break;
                    }
                    else if (kafSense.getResource().equalsIgnoreCase("fn")) {
                            nominalEvents.add(kafTerm);
                            break;
                    }
                    else if (kafSense.getResource().equalsIgnoreCase("mcr") && kafSense.getSensecode().indexOf("Dynamic")>-1) {
                            nominalEvents.add(kafTerm);
                            break;
                    }
                }

            }
        }
        ArrayList<String> eventSpans = new ArrayList<String>();
        for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
            KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
            for (int j = 0; j < kafEvent.getSpanIds().size(); j++) {
                String span = kafEvent.getSpanIds().get(j);
                if (!eventSpans.contains(span)) {
                    eventSpans.add(span);
                }
            }
        }
        int nPredicates = kafSaxParser.kafEventArrayList.size();
       // System.out.println("eventSpans = " + eventSpans.toString());
        for (int i = 0; i < nominalEvents.size(); i++) {
            KafTerm kafTerm = nominalEvents.get(i);
            if (!eventSpans.contains(kafTerm.getTid())) {
                //// new event
                nPredicates++;
               // System.out.println("kafTerm.getId() = " + kafTerm.getTid());
                KafEvent kafEvent = new KafEvent();
                ArrayList<CorefTarget> corefTargets = new ArrayList<CorefTarget>();
                CorefTarget corefTarget = new CorefTarget();
                corefTarget.setId(kafTerm.getTid());
                corefTargets.add(corefTarget);
                kafEvent.setSpans(corefTargets);
                kafEvent.setExternalReferences(kafTerm.getSenseTags());
                String pId = "pr"+ nPredicates;
                kafEvent.setId(pId);
                kafSaxParser.kafEventArrayList.add(kafEvent);
            }
        }
        strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
        kafSaxParser.getKafMetaData().addLayer(layer, lp);
        if (pathToKafFile.isEmpty()) {
            kafSaxParser.writeNafToStream(System.out);
        }
        else {
            try {
                OutputStream fos = new FileOutputStream(pathToKafFile+".frames");
                kafSaxParser.writeNafToStream(fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static boolean hasSpan(ArrayList<String> spans1, ArrayList<String> spans2) {
        for (int i = 0; i < spans1.size(); i++) {
            String span = spans1.get(i);
            if (spans2.contains(span)) {
                return true;
            }
        }
        return false;
    }

}
