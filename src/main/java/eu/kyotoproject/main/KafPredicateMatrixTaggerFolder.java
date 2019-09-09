package eu.kyotoproject.main;


import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.LP;
import eu.kyotoproject.util.Resources;
import eu.kyotoproject.util.Util;

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
public class KafPredicateMatrixTaggerFolder {


    static final String layer = "terms";
    static final String name = "vua-predicate-matrix-tagger";
    static final String version = "1.0";
    static String testparamters1 = "--input-folder /Users/piek/Desktop/NNIP/2005-01-18/S-1/A --extension .xml --mappings fn:;ili;eso --ili --version 1.2 --predicate-matrix /Code/vu/newsreader/vua-resources/PredicateMatrix_withESO.txt.gz --grammatical-words /Code/vu/newsreader/vua-resources/Grammatical-words.en";
    static String testparamters2 = "--input-folder /Users/piek/Desktop/NNIP/2005-01-18/S-1/A --extension .xml --mappings fn:;ili;eso --ili --version 1.2 --predicate-matrix /Code/vu/newsreader/vua-resources/PredicateMatrix.v1.3.txt.role.odwn.gz --grammatical-words /Code/vu/newsreader/vua-resources/Grammatical-words.en";

    static public void main (String[] args) {
        Resources resources = new Resources();
        String pathToKafFolder = "";
        String fileExtension = "";
        String pathToMatrixFile = "";
        String pathToGrammaticalVerbsFile = "";
        String pmVersion = "";
        String key = "";
        String pos = "";
        String prefix = "";
        String format = "naf";
        String[] selectedMappings = null;

        boolean ili = false;
        if (args.length==0)  args = testparamters2.split(" ");

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
            else if ((arg.equalsIgnoreCase("--ignore-prefix")) && (args.length>(i+1))) {
                prefix = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--format")) && (args.length>(i+1))) {
                format = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--ili"))) {
                ili = true;
            }
            else if ((arg.equalsIgnoreCase("--mappings")) && (args.length>(i+1))) {
                selectedMappings = args[i+1].split(";");
            }
        }
        if (ili) {
            resources.processMatrixFileWithWordnetILI(pathToMatrixFile);
        }
        else if (!key.isEmpty()) {
            resources.processMatrixFile(pathToMatrixFile, key, prefix);
        }
        else {
            resources.processMatrixFileWithWordnetLemma(pathToMatrixFile);
        }
        if (!pathToGrammaticalVerbsFile.isEmpty()) {
            resources.processGrammaticalWordsFile(pathToGrammaticalVerbsFile);
        }
        //System.out.println("resources.wordNetPredicateMap.size() = " + resources.wordNetPredicateMap.size());
        //System.out.println("resources.grammaticalWords.size() = " + resources.grammaticalWords.size());
        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<String> kafFiles = Util.makeRecursiveFileListAll(pathToKafFolder, fileExtension);
        for (int f = 0; f < kafFiles.size(); f++) {
            String pathToKafFile =  kafFiles.get(f);
           // System.out.println("pathToKafFile = " + pathToKafFile);

            String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
            String strEndDate = null;
            kafSaxParser.parseFile(pathToKafFile);
            KafPredicateMatrixTagger.processKafFileWordnetNetSynsets(kafSaxParser, pmVersion, resources, selectedMappings);

            strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
            String host = "";
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
            kafSaxParser.getKafMetaData().addLayer(layer, lp);

            String pathToKafFileOut = pathToKafFile+".naf";
            try {
                OutputStream fos = new FileOutputStream(pathToKafFileOut);
                if (format.equalsIgnoreCase("naf")) {
                    kafSaxParser.writeNafToStream(fos);
                }
                else if (format.equalsIgnoreCase("kaf")) {
                    kafSaxParser.writeKafToStream(fos);
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


    }



}
