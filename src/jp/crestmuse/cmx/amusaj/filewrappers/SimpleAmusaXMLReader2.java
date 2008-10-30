package jp.crestmuse.cmx.amusaj.filewrappers;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class SimpleAmusaXMLReader2 {
  private static final String PARSER_CLASS 
  = "org.apache.xerces.parsers.SAXParser";

  public static Result readfile(String filename) 
    throws SAXException, IOException {
    XMLReader reader = XMLReaderFactory.createXMLReader(PARSER_CLASS);
    Result result = new Result();
    ContentHandler h = new AmusaXMLContentHandler(result);
    reader.setContentHandler(h);
    InputSource is = new InputSource(new FileInputStream(new File(filename)));
    reader.parse(is);
    return result;
  }

  public static class Result {
    public String format;
    public Map<String,String> header = new HashMap<String,String>();
    public List<Map<String,String>> attrs = new ArrayList<Map<String,String>>();
    public List<String> data = new ArrayList<String>();
  }

  private static class AmusaXMLContentHandler implements ContentHandler {
    private Result result;
    private String currentTagName = null;
    private StringBuilder sb;

    private AmusaXMLContentHandler(Result result) {
      this.result = result;
    }

    public void setDocumentLocator(Locator l) {

    }

    public void startDocument() {

    }

    public void endDocument() {

    }

    public void startPrefixMapping(String prefix, String uri) {

    }

    public void endPrefixMapping(String prefix) {

    }

    public void startElement(String namespaceURI, String localName, 
                             String qName, Attributes attrs) 
      throws SAXException {
      if (localName.equals("amusaxml")) {
        result.format = attrs.getValue("format");
      } else if (localName.equals("header")) {
        currentTagName = "header";
      } else if (localName.equals("meta")) {
        if (!"header".equals(currentTagName))
          throw new SAXException("'meta' should be in 'header'.");
        result.header.put(attrs.getValue("name"), attrs.getValue("content"));
      } else if (localName.equals("data")) {
        currentTagName = "data";
        sb = new StringBuilder();
        Map m = new HashMap<String,String>();
        for (int i = 0; i < attrs.getLength(); i++)
          m.put(attrs.getLocalName(i), attrs.getValue(i));
        result.attrs.add(m);
      }
    }

    public void endElement(String namespaceURI, String localName, 
                           String qName) {
      if ("data".equals(currentTagName)) {
        result.data.add(sb.toString());
        currentTagName = null;
      }
    }

    public void characters(char ch[], int start, int length) {
      if ("data".equals(currentTagName)) 
        sb.append(ch, start, length);
    }

    public void ignorableWhitespace(char ch[], int start, int length) {

    }

    public void processingInstruction(String target, String data) {

    }

    public void skippedEntity(String name) {

    }
  }
}

