package eu.kyotoproject.util;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileReader;
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
            System.out.println("myerror = " + myerror);
        } catch (SAXException e) {
            Exception x = e;
            if (e.getException() != null)
                x = e.getException();
            myerror += "\nSAXException --" + x.getMessage();
            System.out.println("myerror = " + myerror);
        } catch (Exception eee) {
            eee.printStackTrace();
            myerror += "\nException --" + eee.getMessage();
            System.out.println("myerror = " + myerror);
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
                    int idx = word.lastIndexOf(".");
                    if (idx > -1) {
                        word = word.substring(0, idx);
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
