package jp.crestmuse.cmx.amusaj.filewrappers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jp.crestmuse.cmx.amusaj.sp.SPTerminator;
import jp.crestmuse.cmx.amusaj.sp.TimeSeriesCompatible;
import jp.crestmuse.cmx.filewrappers.AbstractHeaderNodeInterface;
import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.NodeInterface;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.misc.QueueWrapper;

/********************************************************************
 *音楽情景分析API「AMUSA-J」におけるファイルラッパの基底クラスです. 
 *AMUSA-Jでは, トップレベルタグ(タグ名は任意)の中に, 1つのheadタグと, 
 *1つ以上のデータタグ(タグ名は任意)が並ぶという構造を前提とします. 
 *各サブクラスにおいて, トップレベルタグ名はTOP_TAG, データタグ名は
 *DATA_TAGで定義しなければなりません. 
 *******************************************************************/
public class AmusaXMLWrapper extends CMXFileWrapper 
  implements AmusaDataSetCompatible<TimeSeriesCompatible> {
  
//  private Map<String,Header> headers = new HashMap<String,Header>();
  private Header header = null;
  private List<TimeSeriesCompatible> datalist = null;

//  private String toptag = null;
//  private String datatag = null;
  private String format = null;

  public static final String TOP_TAG = "amusaxml";
  private static final String HEADER_TAG = "header";
  private static final String DATA_TAG = "data";

  private AmusaDecoder decoder = AmusaDecoder.getInstance();

/*
  String toptag() {
    if (toptag == null) {
      try {
        toptag = (String)getClass().getField("TOP_TAG").get(this);
      } catch (NoSuchFieldException e) {
        throw new AmusaXMLException(e.toString());
      } catch (IllegalAccessException e) {
        throw new AmusaXMLException(e.toString());
      }
    }
    return toptag;
  }

  String datatag() {
    if (datatag == null) {
      try {
        datatag = (String)getClass().getField("DATA_TAG").get(this);
      } catch (NoSuchFieldException e) {
        throw new AmusaXMLException(e.toString());
      } catch (IllegalAccessException e) {
        throw new AmusaXMLException(e.toString());
      }
    }
    return datatag;
  }
*/

//  protected abstract D createDataNodeInterface(Node node);

//  protected abstract void addDataElement(D d);

//  public AmusaDataSet<D> createDataSet() {
//    return new AmusaDataSet<D>(this);
//  }

  public void changeDecoder(AmusaDecoder decoder) {
    this.decoder = decoder;
  }

  protected void analyze() {
    NamedNodeMap map = getDocument().getDocumentElement().getAttributes();
    format = map.getNamedItem("format").getNodeValue();
    header = new Header(selectSingleNode("/" + TOP_TAG + "/" + HEADER_TAG));
//    NodeList nl = selectNodeList("/" + TOP_TAG + "/" + HEADER_TAG + "/" 
//                                 + "category");
//    for (int i = 0; i < nl.getLength(); i++)
//      headers.put(((Element)nl.item(i)).getAttribute("name"), 
//                  new Header(nl.item(i)));
  }

  public String getFormat() {
    return format;
  }

//  private Header getHeader() {
//  if (header == null)
//    header = new Header(selectSingleNode("/" + TOP_TAG + "/" + HEADER_TAG));
//    return header;
//  }

/*
  public String[] getHeaderNameList() {
    return header.getHeaderNameList();
  }
*/

  public String getHeader(String category, String key) {
    return header.getHeaderElement(category + ":" + key);
//    return headers.get(category).getHeaderElement(key);
  }

  public int getHeaderInt(String category, String key) {
    return Integer.parseInt(getHeader(category, key));
//    return header.getHeaderElementInt(key);
  }

  public double getHeaderDouble(String category, String key) {
    return Double.parseDouble(getHeader(category, key));
//    return header.getHeaderElementDouble(key);
  }

  public boolean containsHeaderKey(String category, String key) {
    return header.containsHeaderKey(category + ":" + key);
//    if (headers.containsKey(category))
//      return headers.get(category).containsHeaderKey(key);
//    else
//      return false;
  }

  public void setHeader(String category, String key, String value) {
    throw new UnsupportedOperationException();
  }

  public void setHeader(String category, String key, int value) {
    throw new UnsupportedOperationException();
  }

  public void setHeader(String category, String key, double value) {
    throw new UnsupportedOperationException();
  }

  public void add(TimeSeriesCompatible d) {
    throw new UnsupportedOperationException();
  }

  public List<TimeSeriesCompatible> getDataList() {
    if (datalist == null) {
      NodeList nl = selectNodeList("/" + TOP_TAG + "/" + DATA_TAG);
      int size = nl.getLength();
      datalist = new ArrayList<TimeSeriesCompatible>();
      for (int i = 0; i < size; i++)
        datalist.add(new Data(nl.item(i)));
//      String format = getFormat();
//      if (format.equals("array"))
//        for (int i = 0; i < size; i++)
//          datalist.add(new DoubleArrayTimeSeries(nl.item(i)));
//      else if (format.equals("peaks"))
//        for (int i = 0; i < size; i++)
//          datalist.add(new Peaks(nl.item(i)));
//      else
//        throw new IllegalStateException("Format '" + format + "' is not supported.");
//      for (int i = 0; i < size; i++)
//        datalist.add(createDataNodeInterface(nl.item(i)));
    }
    return datalist;
  }

private class Header extends AbstractHeaderNodeInterface {
  Header(Node node) {
    super(node);
  }

  protected String getSupportedNodeName() {
    return "category";
//    return "header";
  }
}

  private class Data extends NodeInterface implements TimeSeriesCompatible {

    private int dim = -1;
    private int nFrames;
    private int timeunit = -1;
    private java.util.Queue queue;
    private QueueWrapper qwrap;

    protected Data(Node node) {
      super(node);
      if (hasAttribute("dim")) dim = getAttributeInt("dim");
      if (hasAttribute("timeunit")) timeunit = getAttributeInt("timeunit");
      nFrames = getAttributeInt("frames");
      queue = new LinkedList();
      qwrap = new QueueWrapper(queue);
      StringTokenizer st = new StringTokenizer(getText());
      for (int i = 0; i < nFrames; i++)
	  queue.add(decoder.decode(st, format, dim));
      queue.add(SPTerminator.getInstance());
//      interpretTextElement(getText(), queue);
    }

//    protected abstract void 
//    interpretTextElement(String text, java.util.Queue<E> queue);
    
    protected final String getSupportedNodeName() {
      return "data";
    }

    public final QueueReader getQueueReader() {
      return qwrap.createReader();
    }

    public final int dim() {
      return dim;
    }

    public final int frames() {
      return nFrames;
    }

    public final int timeunit() {
      return timeunit;
    }

    public final boolean isComplete() {
      return true;
    }

    public final void add(Object e) {
      throw new UnsupportedOperationException();
    }

    public final void setAttribute(String key, String value) {
      throw new UnsupportedOperationException();
    }

    public final void setAttribute(String key, int value) {
      throw new UnsupportedOperationException();
    }

    public final void setAttribute(String key, double value) {
      throw new UnsupportedOperationException();
    }

    public final Iterator<Map.Entry<String,String>> getAttributeIterator() {
      return new AttrIterator(node().getAttributes());
    }
  }

/*
  public class DoubleArrayTimeSeries extends Data<DoubleArray> {
    protected DoubleArrayTimeSeries(Node node) {
      super(node);
    }

    protected void interpretTextElement(String text, 
                                        java.util.Queue<DoubleArray> queue) {
      StringTokenizer st = new StringTokenizer(text);
      for (int n = 0; n < frames(); n++) {
        DoubleArray array = factory.createArray(dim());
        for (int i = 0; i < dim(); i++)
          array.set(i, Double.parseDouble(st.nextToken()));
        queue.add(array);
      }
    }
  }
*/

/*
  public class Peaks extends Data<PeakSet> {
    protected Peaks(Node node) {
      super(node);
    }

    protected void interpretTextElement(String text,
                                        java.util.Queue<PeakSet> queue) {
      StringTokenizer st = new StringTokenizer(text);
      for (int n = 0; n < frames(); n++) {
        int nPeaks = Integer.parseInt(st.nextToken());
        PeakSet peakset = new PeakSet(nPeaks);
        for (int i = 0; i < nPeaks; i++)
          peakset.setPeak(i, Double.parseDouble(st.nextToken()),
                          Double.parseDouble(st.nextToken()), 
                          Double.parseDouble(st.nextToken()), 
                          Double.parseDouble(st.nextToken()), 
                          Double.parseDouble(st.nextToken()));
        queue.add(peakset);
      }
    }
  }
*/      

}
                          
    