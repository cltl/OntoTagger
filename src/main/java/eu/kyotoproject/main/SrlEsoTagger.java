package eu.kyotoproject.main;

import eu.kyotoproject.kaf.KafEvent;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.LP;
import eu.kyotoproject.util.FileProcessor;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 20/05/16.
 */
public class SrlEsoTagger {

    static final String layer = "srl";
    static final String name = "vua-srl-eso-tagger";
    static final String version = "1.0";

    static public void main(String[] args) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        String pathToFile = "";
        String pathToEvents = "";
        String extension = "";
        HashMap<String, String> esoMap = new HashMap<String, String>();

        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;

        pathToFile = "/Users/piek/Desktop/NWR-INC/dasym/test1/test.naf";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--input") && args.length > (i + 1)) {
                pathToFile = args[i + 1];
            } else if (arg.equalsIgnoreCase("--extension") && args.length > (i + 1)) {
                extension = args[i + 1];
            } else if (arg.equalsIgnoreCase("--events") && args.length > (i + 1)) {
                pathToEvents = args[i + 1];
                esoMap = readFileToMap(pathToEvents);
            }
        }
        if (pathToFile.equalsIgnoreCase("stream")) {
            kafSaxParser.parseFile(System.in);
            esoTag(kafSaxParser, esoMap);

            strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
            String host = "";
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            LP lp = new LP(name, version, strBeginDate, strBeginDate, strEndDate, host);
            kafSaxParser.getKafMetaData().addLayer(layer, lp);

            kafSaxParser.writeNafToStream(System.out);
        } else {
            File file = new File(pathToFile);
            if (file.isDirectory()) {
                ArrayList<File> files = FileProcessor.makeRecursiveFileArrayList(pathToFile, extension);
                for (int i = 0; i < files.size(); i++) {
                    File nextFile = files.get(i);
                    kafSaxParser.parseFile(nextFile);
                    esoTag(kafSaxParser, esoMap);

                    strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
                    String host = "";
                    try {
                        host = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    LP lp = new LP(name, version, strBeginDate, strBeginDate, strEndDate, host);
                    kafSaxParser.getKafMetaData().addLayer(layer, lp);

                    try {
                        OutputStream fos = new FileOutputStream(nextFile);
                        kafSaxParser.writeNafToStream(fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                kafSaxParser.parseFile(file);
                esoTag(kafSaxParser, esoMap);

                strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
                String host = "";
                try {
                    host = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                LP lp = new LP(name, version, strBeginDate, strBeginDate, strEndDate, host);
                kafSaxParser.getKafMetaData().addLayer(layer, lp);

                try {
                    OutputStream fos = new FileOutputStream(file);
                    kafSaxParser.writeNafToStream(fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void esoTag(KafSaxParser kafSaxParser, HashMap<String, String> esoMap) {
        for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
            KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
            ArrayList<String> spandIds = kafEvent.getSpanIds();
            String lemma = kafSaxParser.getLemma(spandIds);
            if (esoMap.containsKey(lemma)) {
                kafEvent.setExternalReferences(new ArrayList<KafSense>()); /// re-initialize to remove wrong interpretations
                String type = esoMap.get(lemma);
                KafSense kafSense = new KafSense();
                kafSense.setResource("eso");
                kafSense.setSensecode(type);
                kafSense.setSource("vua-source-tagger");
                kafEvent.addExternalReferences(kafSense);
                //System.out.println("lemma = " + lemma);
                //System.out.println("type = " + type);
            }
        }
    }


    public static HashMap<String, String> readFileToMap(String file) {
        HashMap<String, String> map = new HashMap<String, String>();
        if ((new File(file)).exists()) {
            try {
                FileInputStream var2 = new FileInputStream(file);
                InputStreamReader var3 = new InputStreamReader(var2);
                BufferedReader var4 = new BufferedReader(var3);

                String inputLine = "";
                while (var4.ready() && (inputLine = var4.readLine()) != null) {
                    if (inputLine.trim().length() > 0) {
                       String [] fields = inputLine.split("\t");
                        if (fields.length==3) {
                            String lemma = fields[0];
                            String eso = fields[2];
                            map.put(lemma, eso);
                        }
                    }
                }
                var4.close();
            } catch (IOException var6) {
                var6.printStackTrace();
            }
        } else {
            System.out.println("Cannot load the file = " + file);
        }
        return map;
    }

}
