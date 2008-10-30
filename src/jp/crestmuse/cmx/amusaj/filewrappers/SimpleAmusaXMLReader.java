package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.math.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class SimpleAmusaXMLReader {
  private static final String PARSER_CLASS 
  = "org.apache.xerces.parsers.SAXParser";

  public static AmusaDataSet readfile(String filename) 
    throws SAXException, IOException {
    return readfile(filename, AmusaDecoder.getInstance());
  }

  public static AmusaDataSet readfile(String filename, AmusaDecoder decoder) 
    throws SAXException, IOException  {
    XMLReader reader = XMLReaderFactory.createXMLReader(PARSER_CLASS);
    AmusaDataSet dataset = new AmusaDataSet();
    ContentHandler h = new AmusaXMLContentHandler(dataset, decoder);
    reader.setContentHandler(h);
    InputSource is = new InputSource(new FileInputStream(new File(filename)));
    reader.parse(is);
    return dataset;
  }

  private static class AmusaXMLContentHandler implements ContentHandler {
    private AmusaDecoder decoder;
    private String format = null;
    private String currentTagName = null;
    private AmusaDataSet dataset;
    private MutableTimeSeries ts;
    private StringBuilder sb;
    private int nFrames;
    private int dim;
    

    private AmusaXMLContentHandler(AmusaDataSet dataset, 
                                   AmusaDecoder decoder) {
      this.dataset = dataset;
      this.decoder = decoder;
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
        dataset.fmt = format = attrs.getValue("format");
      } else if (localName.equals("header")) {
        currentTagName = "header";
      } else if (localName.equals("meta")) {
        if (!"header".equals(currentTagName))
          throw new SAXException("'meta' should be in 'header'.");
        dataset.setHeader(attrs.getValue("name"), attrs.getValue("content"));
      } else if (localName.equals("data")) {
        currentTagName = "data";
        nFrames = Integer.parseInt(attrs.getValue("frames"));
        String sDim = attrs.getValue(dim);
        dim = sDim == null ? -1 : Integer.parseInt(sDim);
        String sTimeUnit = attrs.getValue("timeunit");
        int timeunit = sTimeUnit == null ? -1 : Integer.parseInt(sTimeUnit);
        ts = new MutableTimeSeries(nFrames, timeunit);
//        if (format.equals("array"))
//          ts = new MutableTimeSeries<DoubleArray>(nFrames, timeunit);
//        else if (format.equals("peaks"))
//          ts = new MutableTimeSeries<PeakSet>(nFrames, timeunit);
//        else 
//          throw new SAXException("unknown format");
        int attrlength = attrs.getLength();
        for (int i = 0; i < attrlength; i++) 
          ts.setAttribute(attrs.getLocalName(i), attrs.getValue(i));
        sb = new StringBuilder();
      }
    }

    public void endElement(String namespaceURI, String localName, 
                           String qName) {
      if ("data".equals(currentTagName)) {
        StringTokenizer st = new StringTokenizer(sb.toString());
        for (int i = 0; i < nFrames; i++) {
          try {
            ts.add(decoder.decode(st, format, dim));
          } catch (InterruptedException e) {
          }
        }
        dataset.add(ts);
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

  public static void main(String[] args) {
    try {
      AmusaDataSet ds = SimpleAmusaXMLReader.readfile(args[0]);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}

