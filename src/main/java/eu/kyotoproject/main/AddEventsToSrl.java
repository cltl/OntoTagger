package eu.kyotoproject.main;

import eu.kyotoproject.kaf.*;
import eu.kyotoproject.util.Util;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddEventsToSrl {
	private static String pathToLemmaClasses = "";
    static final String layer = "srl";
    static final String name = "vua-add-events";
    static final String version = "1.0";

    static String testParameters = "--naf-folder /Users/piek/Desktop/Semeval2018/test_data/NAFOUT --extension .naf --event-file /Users/piek/Desktop/SemEval2018/scripts/trial_vocabulary";

    static public void main (String[] args) {
        String pathToKafFile = "";
        String pathToKafFolder = "";
        String extension = "";
        String pathToEventFile = "";
        args = testParameters.split(" ");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--naf-file") && args.length > (i + 1)) {
                pathToKafFile = args[i + 1];
            }
            else if (arg.equalsIgnoreCase("--naf-folder") && args.length > (i + 1))  {
                pathToKafFolder = args[i + 1];
            }
            else if (arg.equalsIgnoreCase("--extension") && args.length > (i + 1))  {
                extension = args[i + 1];
            }
            else if (arg.equalsIgnoreCase("--event-file") && args.length > (i + 1)) {
                pathToEventFile = args[i + 1];
            }
        }

        ArrayList<String> events = Util.ReadFileToStringArrayList(pathToEventFile);
        //System.out.println("events.size() = " + events.size());
        //System.out.println("pathToKafFolder = " + pathToKafFolder);
        //System.out.println("extension = " + extension);

        if (pathToKafFile.isEmpty() && pathToKafFolder.isEmpty()) {
            //System.out.println("Waiting for stream");
            String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
            String strEndDate = null;
            KafSaxParser kafSaxParser = new KafSaxParser();
            kafSaxParser.parseFile(System.in);
            process(kafSaxParser, events);
            strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
            String host = "";
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
            kafSaxParser.getKafMetaData().addLayer(layer, lp);
            kafSaxParser.writeNafToStream(System.out);
        }
        else if (!pathToKafFile.isEmpty()) {
                processNafFile(pathToKafFile, events);
        }
        else if (!pathToKafFolder.isEmpty()) {
                ArrayList<String> files = Util.makeFlatFileList(pathToKafFolder, extension);
                for (int i = 0; i < files.size(); i++) {
                    String filePath = files.get(i);
                    processNafFile(filePath, events);
                }
        }
    }

    static void processNafFile (String pathToKafFile, ArrayList<String> events) {
        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;
        KafSaxParser kafSaxParser = new KafSaxParser();

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
        System.out.println("pathToFile = " + pathToKafFile);
        try {
            process(kafSaxParser, events);
            strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
            String host = "";
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            LP lp = new LP(name, version, strBeginDate, strBeginDate, strEndDate, host);
            kafSaxParser.getKafMetaData().addLayer(layer, lp);
            OutputStream fos = new FileOutputStream(pathToKafFile + ".events");
            kafSaxParser.writeNafToStream(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void process(KafSaxParser kafSaxParser, ArrayList<String> events) {
        ArrayList<KafTerm> newEvents = new ArrayList<KafTerm>();
        for (int i = 0; i < kafSaxParser.getKafTerms().size(); i++) {
            KafTerm kafTerm = kafSaxParser.getKafTerms().get(i);
            if (events.contains(kafTerm.getLemma().toLowerCase())) {
                newEvents.add(kafTerm);
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
        for (int i = 0; i < newEvents.size(); i++) {
            KafTerm kafTerm = newEvents.get(i);
            if (!eventSpans.contains(kafTerm.getTid())) {
                //// new event
                nPredicates++;
               // System.out.println("kafTerm.getId() = " + kafTerm.getTid());
                KafEvent kafEvent = new KafEvent();
                kafEvent.setStatus("new");
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
