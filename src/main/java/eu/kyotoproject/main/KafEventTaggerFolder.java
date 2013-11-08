package eu.kyotoproject.main;


import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.util.Resources;
import eu.kyotoproject.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 1/29/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class KafEventTaggerFolder {


    static public void main (String[] args) {
        Resources resources = new Resources();
        String pathToKafFolder = "";
        String fileExtension = "";
        String pathToMatrixFile = "";
        String pathToGrammaticalVerbsFile = "";
        String pmVersion = "";
        String key = "";
        String pos = "";
        boolean ili = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--input-folder")) && (args.length>(i+1))) {
                pathToKafFolder = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--extension")) && (args.length>(i+1))) {
                fileExtension = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--pos")) && (args.length>(i+1))) {
                pos = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--predicate-matrix")) && (args.length>(i+1))) {
                pathToMatrixFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--version")) && (args.length>(i+1))) {
                pmVersion = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--grammatical-words")) && (args.length>(i+1))) {
                pathToGrammaticalVerbsFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--key")) && (args.length>(i+1))) {
                key = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--ili"))) {
                ili = true;
            }
        }
        if (ili) {
            resources.processMatrixFileWithWordnetILI(pathToMatrixFile);
        }
        else if (!key.isEmpty()) {
            resources.processMatrixFile(pathToMatrixFile, key);
        }
        else {
            resources.processMatrixFileWithWordnetLemma(pathToMatrixFile);
        }
        if (!pathToGrammaticalVerbsFile.isEmpty()) {
            resources.processGrammaticalWordsFile(pathToGrammaticalVerbsFile);
        }
        System.out.println("resources.wordNetPredicateMap.size() = " + resources.wordNetPredicateMap.size());
        System.out.println("resources.grammaticalWords.size() = " + resources.grammaticalWords.size());
        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<String> kafFiles = Util.makeRecursiveFileListAll(pathToKafFolder, fileExtension);
        for (int f = 0; f < kafFiles.size(); f++) {
            String pathToKafFile =  kafFiles.get(f);
            System.out.println("pathToKafFile = " + pathToKafFile);
            KafEventTagger.processKafFileWordnetNetSynsets(kafSaxParser, pathToKafFile, pmVersion, resources);
            String pathToKafFileOut = pathToKafFile+".event.kaf";
            try {
                FileOutputStream fos = new FileOutputStream(pathToKafFileOut);
                kafSaxParser.writeNafToStream(fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }



}
