package eu.kyotoproject.prepare;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/15/11
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateOntotagDutch {


    static public HashMap<String, String> readT1 (String filePath) {
/*
eng-30-02055431-n eng-30-01504437-n
eng-30-02055460-a eng-30-00023271-n
eng-30-02055521-v eng-30-01835496-v
 */
           HashMap<String, String> lex = new HashMap<String, String>();
           if (new File(filePath).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine = "";
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    if (inputLine.trim().length()>0) {
                        String [] fields = inputLine.split(" ");
                        if (fields.length==2) {
                            String s0 = fields[0];
                            String s1 = fields[1];
                           lex.put(s0, s1);
                        }
                    }
                }
                fis.close();
                System.out.println("T1 lex.size() = " + lex.size());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
           }
           return lex;
    }

    static public HashMap<String,ArrayList<String>> readT2 (String filePath) {
/*
eng-30-01350699-v sc_subClassOf Kyoto#change-eng-3.0-00191142-n
eng-30-01350699-v sc_subClassOf Kyoto#happening__occurrence__occurrent__natural_event-eng-3.0-07283608-n
eng-30-01350701-n sc_subClassOf Kyoto#eubacteria__eubacterium__true_bacteria-eng-3.0-01355326-n
eng-30-01350855-n sc_hasQuality Kyoto#quality-eng-3.0-04723816-n
eng-30-01350855-n sc_subClassOf Kyoto#eubacteria__eubacterium__true_bacteria-eng-3.0-01355326-n
eng-30-01350876-a sc_qualityOf Kyoto#trait-eng-3.0-04616059-n
eng-30-01350876-a sc_subClassOf Kyoto#quality-eng-3.0-04723816-n
eng-30-01350971-v sc_hasParticipant Kyoto#artifact__artefact-eng-3.0-00021939-n
eng-30-01350971-v sc_hasRole Kyoto#instrument
eng-30-01350971-v sc_subClassOf Kyoto#happening__occurrence__occurrent__natural_event-eng-3.0-07283608-n
eng-30-01350971-v sc_subClassOf Kyoto#touch-eng-3.0-01206218-v
eng-30-01350994-n sc_subClassOf Kyoto#eubacteria__eubacterium__true_bacteria-eng-3.0-01355326-n
eng-30-01351021-a sc_qualityOf Kyoto#discipline__subject__subject_area__subject_field__field__field_of_study__study__bailiwick-eng-3.0-05996646-n
eng-30-01351021-a sc_subClassOf Kyoto#quality-eng-3.0-04723816-n
eng-30-01351170-n sc_subClassOf Kyoto#eubacteria__eubacterium__true_bacteria-eng-3.0-01355326-n
eng-30-01351170-v sc_hasParticipant Kyoto#device-eng-3.0-03183080-n
eng-30-01351170-v sc_hasRole Kyoto#instrument
 */
           HashMap<String,ArrayList<String>> lex = new HashMap<String,ArrayList<String>>();
           if (new File(filePath).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine = "";
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    if (inputLine.trim().length()>0) {
                        String [] fields = inputLine.split(" ");
                        if (fields.length==3) {
                            String s0 = fields[0];
                            String t = fields[1]+" "+fields[2];
                            if (lex.containsKey(s0)) {
                                ArrayList<String> mappings = lex.get(s0);
                                mappings.add(t);
                                lex.put(s0, mappings);
                            }
                            else {
                                ArrayList<String> mappings = new ArrayList<String>();
                                mappings.add(t);
                                lex.put(s0, mappings);
                            }
                        }
                    }
                }
                fis.close();
                System.out.println("T2 lex.size() = " + lex.size());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
           }
           return lex;
    }

    static public HashMap<String, ArrayList<String>> readDutchLex (String filePath) {
        //misdruk  d_n-12675-n ENG-30-01263018-n ENG-30-05244934-n ENG-30-05790572-n ENG-30-06591342-n ENG-30-13560417-n ENG-30-13763888-n
           HashMap<String, ArrayList<String>> lex = new HashMap<String, ArrayList<String>>();
           if (new File(filePath).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine = "";
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    if (inputLine.trim().length()>0) {
                        String [] fields = inputLine.split(" ");
                        if (fields.length>2) {
                            String nlSynset = fields[1];
                            ArrayList<String> equiMap = new ArrayList<String>();
                            for (int i = 0; i < fields.length; i++) {
                                String field = fields[i];
                                if (field.startsWith("ENG")) {
                                    equiMap.add(field);
                                }
                            }
                            if (equiMap.size()>0) {
                               lex.put(nlSynset, equiMap);
                            }
                        }
                    }
                }
                fis.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
           }
           return lex;
    }

    static public void main (String[] args) {
        /// Takes an English wordnet based file with ontotagged data and the cornetto synset lexicon for UKB with equivalent synsets and creates
        /// ontotag files for Dutch

        //-t1
        // "/Projects/Kyoto/WP6 Knowledge integration/OntoTagTables/generic/T1.v3.2.txt"
        // "/Projects/Kyoto/WP6 Knowledge integration/OntoTagTables/nl/dwn13_nld_eng.lex"

        //-t2
        // "/Projects/Kyoto/WP6 Knowledge integration/OntoTagTables/generic/T2.v3.2.txt"
        // "/Projects/Kyoto/WP6 Knowledge integration/OntoTagTables/nl/dwn13_nld_eng.lex"

        String option = args[0];
        String fileT = args[1];
        String dutchLexFile = args[2];
        if (option.equals("-t1")) {
            HashMap<String, String> t1 = readT1(fileT);
            if (new File(dutchLexFile).exists() ) {
                 try {
                     /*
c  d_n-10569-n ENG-30-03060294-n ENG-30-06902696-n ENG-30-13636648-n ENG-30-13714491-n ENG-30-13750415-n ENG-30-14633206-n d_n-10873-n ENG-30-06868309-n d_n-38650-n d_n-33035-n
a  d_n-26714-n ENG-30-05400860-n ENG-30-13637376-n ENG-30-13658027-n d_n-21099-n d_n-29117-n ENG-30-06868986-n
                      */
                     FileInputStream fis = new FileInputStream(dutchLexFile);
                     FileOutputStream fos = new FileOutputStream(fileT+".nl");
                     InputStreamReader isr = new InputStreamReader(fis);
                     BufferedReader in = new BufferedReader(isr);
                     String inputLine = "";
                     while (in.ready()&&(inputLine = in.readLine()) != null) {
                         if (inputLine.trim().length()>0) {
                             String [] fields = inputLine.split(" ");
                             if (fields.length>2) {
                                 String nlSynset = "";
                                 for (int i = 1; i < fields.length; i++) {
                                     String field = fields[i].toLowerCase();
                                     if (field.startsWith("eng-30")) {
                                         if (nlSynset.length()>0) {
                                         //    System.out.println("nlSynset = " + nlSynset);
                                             if (t1.containsKey(field)) {
                                                String s1 = t1.get(field); //// we get the base concept associated with the english synset
                                                String str = nlSynset+" "+s1+"\n";
                                             //    System.out.println("str = " + str);
                                                fos.write(str.getBytes());
                                             }
                                         }
                                     }
                                     else {
                                        nlSynset = fields[i];
                                     }
                                 }
                             }
                         }
                     }
                     fis.close();
                     fos.close();
                 }
                 catch (IOException e) {
                     e.printStackTrace();
                 }
            }
        }
        else if (option.equals("-t2")) {
            HashMap<String, ArrayList<String>> t2 = readT2(fileT);
            if (new File(dutchLexFile).exists() ) {
                 try {
                     FileInputStream fis = new FileInputStream(dutchLexFile);
                     FileOutputStream fos = new FileOutputStream(fileT+".nl");
                     InputStreamReader isr = new InputStreamReader(fis);
                     BufferedReader in = new BufferedReader(isr);
                     String inputLine = "";
                     while (in.ready()&&(inputLine = in.readLine()) != null) {
                         if (inputLine.trim().length()>0) {
                             String [] fields = inputLine.split(" ");
                             if (fields.length>2) {
                                 String nlSynset = "";
                             //    System.out.println("nlSynset = " + nlSynset);
                                 for (int i = 1; i < fields.length; i++) {
                                     String field = fields[i].toLowerCase();
                                     if (field.startsWith("eng-")) {
                                         if (nlSynset.length()>0) {
                                             if (t2.containsKey(field)) {
                                                 ArrayList<String> mappings = t2.get(field);
                                                 for (int j = 0; j < mappings.size(); j++) {
                                                     String s = mappings.get(j);
                                                     String str = nlSynset+" "+s+"\n";
                                                  //    System.out.println("str = " + str);
                                                     fos.write(str.getBytes());
                                                 }
                                             }
                                         }
                                     }
                                     else {
                                        nlSynset = fields[i];
                                     }
                                 }
                             }
                         }
                     }
                     fis.close();
                     fos.close();
                 }
                 catch (IOException e) {
                     e.printStackTrace();
                 }
            }
        }
    }


}
