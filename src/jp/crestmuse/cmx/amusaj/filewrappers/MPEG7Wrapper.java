package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.*;
import org.w3c.dom.*;

public class MPEG7Wrapper extends CMXFileWrapper {

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final String 
  MPEG7_NAMESPACE_URI = "urn:mpeg:mpeg7:schema:2001";

  private Description[] dlist = null;

  private static final Pattern HOP_SIZE_PATTERN
   = Pattern.compile("PT(\\d+)N(\\d+)F");

//  private DescriptionUnit dunit = null;

  public Description[] getDescriptionList() {
    if (dlist == null) {
      setNamespaceContext();
      NodeList nl = selectNodeList("/mpeg7:Mpeg7/mpeg7:Description");
      int size = nl.getLength();
      dlist = new Description[size];
      for (int i = 0; i < size; i++)
        dlist[i] = new Description(nl.item(i));
    }
    return dlist;
  }

/*
  // UNDER CONSTRUCTION
  public DescriptionUnit getDescriptionUnit() {
    if (dunit == null)
      dunit = new DescriptionUnit(selectSingleNode("/Mpeg7/DescriptionUnit"));
    return dunit;
  }
*/

  public class Description extends NodeInterface {
    private MultimediaContent[] contentlist = null;
    private Description(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "Description";
    }
    public MultimediaContent[] getMultimediaContentList() {
      if (contentlist == null) {
        NodeList nl = selectNodeList(node(), "mpeg7:MultimediaContent");
        int size = nl.getLength();
        contentlist = new MultimediaContent[size];
        for (int i = 0; i < size; i++)
          contentlist[i] = new MultimediaContent(nl.item(i));
      }
      return contentlist;
    }
  }

  public class MultimediaContent extends NodeInterface {
    private Audio audio = null;
    private MultimediaContent(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "MultimediaContent";
    }
    public Audio getAudio() {
      if (audio == null)
        audio = new Audio(selectSingleNode(node(), "mpeg7:Audio"));
      return audio;
    }
  }

  public class Audio extends NodeInterface {
    AudioD[] audioD = null;
    private Audio(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "Audio";
    }
    public AudioD[] getAudioDList() {
      if (audioD == null) {
        NodeList nl = selectNodeList(node(), "mpeg7:AudioDescriptor");
        int size = nl.getLength();
        audioD = new AudioD[size];
        for (int i = 0; i < size; i++)
          audioD[i] = new AudioD(nl.item(i));
      }
      return audioD;
    }
  }

  public class AudioD extends NodeInterface {
    ScalableSeries series = null;
    private AudioD(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "AudioDescriptor";
    }
    public String type() {
      return getAttributeNS("type", 
                             "http://www.w3.org/2001/XMLSchema-instance");
    }
    public ScalableSeries series() {
      if (series == null) {
        Node child = getFirstChild();
        String name = child.getNodeName(); 
        if (name.equals("SeriesOfScalar"))
          series = new SeriesOfScalar(child);
        else if (name.equals("SeriesOfVector"))
          series = new SeriesOfVector(child);
      }
      return series;
    }
  }

  public class Vector extends NodeInterface {
     private Vector(Node node) {
       super(node);
     }
    protected String getSupportedNodeName() {
      return "Vector";
    }
  }

  public class SeriesOfVector extends ScalableSeries{
    private SeriesOfVector(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "SeriesOfVector";
    }
    public int vectorSize() {
      return getAttributeInt("vectorSize");
    }
    public TimeSeriesCompatible getTimeSeries(String elem) {
      String text = getText(getChildByTagName(elem));
      int dim = vectorSize();
      int frames = totalNumOfSamples() / dim;
      return new MyVectorTimeSeries(text, dim, frames, timeunitMS());
    }
    public TimeSeriesCompatible[] getTimeSeriesList() {
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      TimeSeriesCompatible[] ts = new TimeSeriesCompatible[size];
      for (int i = 0; i < size; i++) {
        String text = getText(nl.item(i));
        int dim = vectorSize();
        int frames = totalNumOfSamples() / dim;
        ts[i] = new MyVectorTimeSeries(text, dim, frames, timeunitMS());
      }
      return ts;
    }

  }

   public class Scalar extends NodeInterface {
     private Scalar(Node node) {
       super(node);
     }
     protected String getSupportedNodeName() {
       return "Scalar";
     }
     public float value() {
       return Float.parseFloat(getText());
     }
   }

   public abstract class ScalableSeries extends NodeInterface {
     private int timeunitMS = -1;
     private ScalableSeries(Node node) {
       super(node);
     }
     public int totalNumOfSamples() {
       return getAttributeInt("totalNumOfSamples");
     }
     public String hopSize() {
       return getAttribute("hopSize");
     }
     public int timeunitMS() {
       if (timeunitMS == -1) {
         Matcher m = HOP_SIZE_PATTERN.matcher(hopSize());
         if (m.matches()) 
           timeunitMS = 1000 * Integer.parseInt(m.group(1))
             / Integer.parseInt(m.group(2));
         else
           throw new IllegalStateException("hopSize error");
       }
       return timeunitMS;
     }
     public abstract TimeSeriesCompatible getTimeSeries(String elem);
     public abstract TimeSeriesCompatible[] getTimeSeriesList();
   }

  public class SeriesOfScalar extends ScalableSeries {
    private SeriesOfScalar(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "SeriesOfScalar";
    }
    public TimeSeriesCompatible getTimeSeries(String elem) {
      String text = getText(getChildByTagNameNS(elem, MPEG7_NAMESPACE_URI));
      return new MyScalarTimeSeries(text, totalNumOfSamples(), timeunitMS());
    }
    public TimeSeriesCompatible[] getTimeSeriesList() {
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      TimeSeriesCompatible[] ts = new TimeSeriesCompatible[size];
      for (int i = 0; i < size; i++)
        ts[i] = new MyScalarTimeSeries(getText(nl.item(i)), 
                                       totalNumOfSamples(), timeunitMS());
      return ts;
    }
  }
  
  private abstract class MyTimeSeries implements TimeSeriesCompatible {
    int dim;
    int nFrames;
    private int timeunit;
    java.util.Queue<DoubleArray> queue;
    private QueueWrapper<DoubleArray> qwrap;
    private MyTimeSeries(String text, int dim, int frames, int timeunit) {
      this.dim = dim;
      this.nFrames = frames;
      this.timeunit = timeunit;
      queue = new LinkedList<DoubleArray>();
      qwrap = new QueueWrapper(queue, nFrames);
    }
    public QueueReader<DoubleArray> getQueueReader() {
      return qwrap.createReader();
    }
    public int dim() {
      return dim;
    }
    public int frames() {
      return nFrames;
    }
    public int timeunit() {
      return timeunit;
    }
    public void add(DoubleArray array) {
      throw new UnsupportedOperationException();
    }
    public void setAttribute(String key, String value) {
      throw new UnsupportedOperationException();
    }
    public void setAttribute(String key, int value) {
      throw new UnsupportedOperationException();
    }
    public void setAttribute(String key, double value) {
      throw new UnsupportedOperationException();
    }
    public String getAttribute(String key) {
      throw new UnsupportedOperationException();
    }
    public int getAttributeInt(String key) {
      throw new UnsupportedOperationException();
    }
    public double getAttributeDouble(String key) {
      throw new UnsupportedOperationException();
    }
    public Iterator<Map.Entry<String,String>> getAttributeIterator() {
      throw new UnsupportedOperationException();
    }
  }

   private class MyScalarTimeSeries extends MyTimeSeries {
     private MyScalarTimeSeries(String text, int frames, int timeunit) {
       super(text, 1, frames, timeunit);
       String[] ss = text.trim().split(" ");
       if (ss.length != frames)
         throw new IllegalStateException("inconsistent data size");
       for (int n = 0; n < nFrames; n++) {
         DoubleArray array = factory.createArray(dim);
         array.set(0, Double.parseDouble(ss[n]));
         queue.add(array);
       }
     }
   }
  
  private class MyVectorTimeSeries extends MyTimeSeries {
    private MyVectorTimeSeries(String text, int dim, int frames, 
                               int timeunit) {
      super(text, dim, frames, timeunit);
      String[] ss = text.split("\n");
      if (ss.length != frames)
        throw new IllegalStateException("inconsistent data size");
      for (int n = 0; n < nFrames; n++) {
        String[] sss = ss[n].trim().split(" ");
        if (sss.length != dim)
          throw new IllegalStateException("inconsistent data size");
        DoubleArray array = factory.createArray(dim);
        for (int i = 0; i < dim; i++)
          array.set(i, Double.parseDouble(sss[i]));
        queue.add(array);
      }
    }
  }
}
    
         