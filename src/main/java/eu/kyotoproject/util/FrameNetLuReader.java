package eu.kyotoproject.util;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 01/06/15.
 */
public class FrameNetLuReader extends DefaultHandler {
    /**
     * <lu hasAnnotation="true" frameID="1148" frameName="Attributed_information" status="Finished_Initial" name="according to.prep" ID="10677"/>
     * <lu hasAnnotation="true" frameID="298" frameName="Text" status="Finished_Initial" name="account.n" ID="5455"/>
     */

    public HashMap<String, ArrayList<String>> lexicalUnitFrameMap;
    private String value = "";
    public boolean KEEPPOSTAG = false;

    public boolean parseFile(InputSource var1) {
        try {
            this.init();
            SAXParserFactory var2 = SAXParserFactory.newInstance();
            var2.setValidating(false);
            SAXParser var3 = var2.newSAXParser();
            var3.parse(var1, this);
            return true;
        } catch (FactoryConfigurationError var4) {
            var4.printStackTrace();
        } catch (ParserConfigurationException var5) {
            var5.printStackTrace();
        } catch (SAXException var6) {
            var6.printStackTrace();
        } catch (IOException var7) {
            ;
        }

        return false;
    }

    public boolean parseFile(File var1) {
        try {
            FileReader var2 = new FileReader(var1);
            InputSource var3 = new InputSource(var2);
            boolean var4 = this.parseFile(var3);
            var2.close();
            return var4;
        } catch (IOException var5) {
            var5.printStackTrace();
            return false;
        }
    }

    public boolean parseFile(InputStream var1) {
        InputSource var2 = new InputSource(var1);
        boolean var3 = this.parseFile(var2);
        try {
            var1.close();
        } catch (IOException var5) {
            ;
        }

        return var3;
    }

    public void parseFile(String filePath) {
        String myerror = "";
        init();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource(new FileReader(filePath));
            parser.parse(inp, this);
        } catch (SAXParseException err) {
            myerror = "\n** Parsing error" + ", line " + err.getLineNumber()
                    + ", uri " + err.getSystemId();
            myerror += "\n" + err.getMessage();
            //System.out.println("myerror = " + myerror);
        } catch (SAXException e) {
            Exception x = e;
            if (e.getException() != null)
                x = e.getException();
            myerror += "\nSAXException --" + x.getMessage();
            //System.out.println("myerror = " + myerror);
        } catch (Exception eee) {
            eee.printStackTrace();
            myerror += "\nException --" + eee.getMessage();
           // System.out.println("myerror = " + myerror);
        }
    }//--c




    public void init() {
        lexicalUnitFrameMap = new HashMap<String, ArrayList<String>>();
    }


    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("lu")) {
            String word = "";
            String frame = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("frameName")) {
                    frame = attributes.getValue(i).trim();
                } else if (name.equalsIgnoreCase("name")) {
                    word = attributes.getValue(i).trim();
                    if (!KEEPPOSTAG) {
                        int idx = word.lastIndexOf(".");
                        if (idx > -1) {
                            word = word.substring(0, idx);
                        }
                    }
                }
            }
            if (!frame.isEmpty() && !word.isEmpty()) {
                if (lexicalUnitFrameMap.containsKey(word)) {
                    ArrayList<String> frames = lexicalUnitFrameMap.get(word);
                    frames.add(frame);
                    lexicalUnitFrameMap.put(word, frames);
                } else {
                    ArrayList<String> frames = new ArrayList<String>();
                    frames.add(frame);
                    lexicalUnitFrameMap.put(word, frames);
                }
            }
        }

        value = "";
    }//--startElement

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }

    public ArrayList<String> getFramesForWord(String word) {
        ArrayList<String> frames = new ArrayList<String>();
        if (this.lexicalUnitFrameMap.containsKey(word)) {
            frames = lexicalUnitFrameMap.get(word);
        } else if (word.length() > 3) {
            word = word.substring(0, word.length() - 1);
            if (lexicalUnitFrameMap.containsKey(word)) {
                frames = lexicalUnitFrameMap.get(word);
            } else {
                word = word.substring(0, word.length() - 1);
                if (lexicalUnitFrameMap.containsKey(word)) {
                    frames = lexicalUnitFrameMap.get(word);
                } else if (word.length() > 4) {
                    word = word.substring(0, word.length() - 1);
                    if (lexicalUnitFrameMap.containsKey(word)) {
                        frames = lexicalUnitFrameMap.get(word);
                    }
                }
            }
        }
        return frames;
    }
}
