package eu.kyotoproject.main;

import eu.kyotoproject.kaf.KafEvent;
import eu.kyotoproject.kaf.KafParticipant;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.util.FileProcessor;
import eu.kyotoproject.util.FixEventCoreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by piek on 20/05/16.
 */
public class SrlSourceTagger {


    static public void main (String[] args) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        String pathToFile = "";
        String pathToSourceLemmas = "";
        String extension = "";
        Vector<String> predicates = null;

        pathToFile = "/Users/piek/Desktop/NWR-INC/dasym/test1/test.naf";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--input") && args.length>(i+1)) {
                pathToFile = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--events") && args.length>(i+1)) {
                pathToSourceLemmas = args[i+1];
                predicates = FixEventCoreferences.readFileToVector(pathToSourceLemmas);
            }
        }
        if (pathToFile.equalsIgnoreCase("stream")) {
            kafSaxParser.parseFile(System.in);
            sourceTag(kafSaxParser, predicates);
            kafSaxParser.writeNafToStream(System.out);
        }
        else {
            File file = new File(pathToFile);
            if (file.isDirectory()) {
                ArrayList<File> files = FileProcessor.makeRecursiveFileArrayList(pathToFile, extension);
                for (int i = 0; i < files.size(); i++) {
                    File nextFile = files.get(i);
                    kafSaxParser.parseFile(nextFile);
                    sourceTag(kafSaxParser, predicates);
                    try {
                        OutputStream fos = new FileOutputStream(nextFile);
                        kafSaxParser.writeNafToStream(fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                kafSaxParser.parseFile(file);
                sourceTag(kafSaxParser, predicates);
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

    static void sourceTag(KafSaxParser kafSaxParser, Vector<String> predicates) {
        for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
            KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
            ArrayList<String> spandIds = kafEvent.getSpanIds();
            String lemma = kafSaxParser.getLemma(spandIds);
            if (predicates.contains(lemma)) {
               // System.out.println("lemma = " + lemma);
                KafSense kafSense = new KafSense();
                kafSense.setResource("FrameNet");
                kafSense.setSensecode("Communication");
                kafSense.setSource("vua-source-tagger");
                kafEvent.addExternalReferences(kafSense);
                KafSense roleSense = new KafSense();
                roleSense.setSensecode("Communication@Message");
                roleSense.setResource("Communication@Message");
                roleSense.setSource("Communication@Message");
                for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                    KafParticipant kafParticipant = kafEvent.getParticipants().get(j);
                    kafParticipant.setTokenStrings(kafSaxParser);
                    if (kafParticipant.getRole().equalsIgnoreCase("#")) {
                        kafParticipant.addExternalReferences(roleSense);
                    }
                    else if (kafParticipant.getRole().equalsIgnoreCase("Arg1") || kafParticipant.getRole().equalsIgnoreCase("A1")) {
                        kafParticipant.addExternalReferences(roleSense);
                    }
                    else if (!kafParticipant.getRole().equalsIgnoreCase("Arg0") && !kafParticipant.getRole().equalsIgnoreCase("A0")) {
                        if (kafParticipant.getSpanIds().size()>3) {
                           // System.out.println("kafParticipant.getRole() = " + kafParticipant.getRole());
                            // System.out.println("kafParticipant.getSpanIds() = " + kafParticipant.getSpanIds());
                           // System.out.println("kafParticipant.getTokenString() = " + kafParticipant.getTokenString());
                            kafParticipant.addExternalReferences(roleSense);
                        }
                    }
                }
            }
        }
    }
}
