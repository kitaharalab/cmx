package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Utils.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.*;
import org.w3c.dom.*;

public class MPEG7Wrapper extends CMXFileWrapper {

  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();
  private static final DoubleMatrixFactory mfactory = 
    DoubleMatrixFactory.getFactory();
  private static final String 
  MPEG7_NAMESPACE_URI = "urn:mpeg:mpeg7:schema:2001";
  private static final String
  XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

  private Description[] dlist = null;

  private static final Pattern HOP_SIZE_PATTERN
   = Pattern.compile("PT(\\d+)N(\\d+)F");

//  private DescriptionUnit dunit = null;

  private static String getNodeType(Node node) {
    return ((Element)node).getAttributeNS(XSI_NAMESPACE_URI, "type");
  }

  public Description[] getDescriptionList() {
    if (dlist == null) {
      setNamespaceContext();
      NodeList nl = selectNodeList("/mpeg7:Mpeg7/mpeg7:Description");
      int size = nl.getLength();
      dlist = new Description[size];
      for (int i = 0; i < size; i++) {
        Node node = nl.item(i);
        String type = getNodeType(node);
        if (type.equals("ContentEntityType"))
          dlist[i] = new ContentEntity(node);
        else if (type.equals("ModelDescriptionType"))
          dlist[i] = new ModelDescription(node);
        else
          dlist[i] = new Description(node);
      }
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

  private abstract class MPEG7NodeInterface extends NodeInterface {
    private MPEG7NodeInterface(Node node) {
      super(node);
    }
    public String type() {
      return getAttributeNS("type", XSI_NAMESPACE_URI);
    }
  }

  public class Description extends MPEG7NodeInterface {
    private Description(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "Description";
    }
  }    

  public class ContentEntity extends Description {
    private MultimediaContent[] contentlist = null;
    private ContentEntity(Node node) {
      super(node);
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

  public class MultimediaContent extends MPEG7NodeInterface {
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

  public class Audio extends MPEG7NodeInterface {
    AudioD[] audioD = null;
    AudioDS[] audioDS = null;
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
        for (int i = 0; i < size; i++) {
          Node node = nl.item(i);
          String type = getNodeType(node);
          if (type.equals("AudioHarmonicityType"))
            audioD[i] = new AudioHarmonicity(node);
          else if (type.equals("AudioSignalQuality"))
            audioD[i] = new AudioSignalQuality(node);
          else
            audioD[i] = new AudioLLD(node);
        }
      }
      return audioD;
    }
    public AudioDS[] getAudioDSList() {
      if (audioDS == null) {
        NodeList nl = selectNodeList(node(), "mpeg7:AudioDescriptionScheme");
        int size = nl.getLength();
        audioDS = new AudioDS[size];
        for (int i = 0; i < size; i++) {
          Node node = nl.item(i);
          String type = getNodeType(node);
          if (type.contains("Timbre"))
            audioDS[i] = new InstrumentTimbre(node);
          else if (type.contains("AudioSignature"))
            audioDS[i] = new AudioSignature(node);
          else
            audioDS[i] = new AudioDS(node);
        }
      }
      return audioDS;
    }
  }

  public class AudioDS extends MPEG7NodeInterface {
    private AudioDS(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "AudioDescriptionScheme";
    }
  }

  public class InstrumentTimbre extends AudioDS {
    private AudioLLD[] lldlist = null;
    private InstrumentTimbre(Node node) {
      super(node);
    }
    public AudioLLD[] getAudioLLDList() {
      if (lldlist == null) {
        NodeList nl = getChildNodes();
        int size = nl.getLength();
        lldlist = new AudioLLD[size];
        for (int i = 0; i < size; i++) {
          Node node = nl.item(i);
          final String nodename = node.getNodeName();
          lldlist[i] = new AudioLLD(node) {
              protected String getSupportedNodeName() {
                return nodename;
              }
            };
        }
      }
      return lldlist;
    }
  }

  public class AudioSignature extends AudioDS {
    private AudioLLD flatness;
    private AudioSignature(Node node) {
      super(node);
      flatness = new AudioLLD(selectSingleNode(node(), "mpeg7:Flatness")) {
          protected String getSupportedNodeName() {
            return "Flatness";
          }
        };
    }
  }
        
  public abstract class AudioD extends MPEG7NodeInterface {
    private AudioD(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "AudioDescriptor";
    }
  }

  public class AudioHarmonicity extends AudioD {
    private AudioLLD harmonicRatio, upperLimitOfHarmonicity;
    private AudioHarmonicity(Node node) {
      super(node);
      harmonicRatio = new AudioLLD(selectSingleNode(node(), 
                                                    "mpeg7:HarmonicRatio")) {
          protected String getSupportedNodeName() {
            return "HarmonicRatio";
          }
        };
      upperLimitOfHarmonicity
        = new AudioLLD(selectSingleNode(node(), 
                                        "mpeg7:UpperLimitOfHarmonicity")) {
            protected String getSupportedNodeName() {
              return "UpperLimitOfHarmonicity";
            }
          };
    }
    public AudioLLD harmonicRatio() {
      return harmonicRatio;
    }
    public AudioLLD upperLimitOfHarmonicity() {
      return upperLimitOfHarmonicity;
    }
  }

  public class AudioSignalQuality extends AudioD {
    // under construction
    private AudioSignalQuality(Node node) {
      super(node);
    }
  }

  public class AudioLLD extends AudioD {
    private ScalableSeries series = null;
    private AudioLLD(Node node) {
      super(node);
    }
    public double scalar() throws NullPointerException {
      return getChildTextDouble("Scalar");
    }
    public ScalableSeries series() {
      if (series == null) {
        Node child = getFirstChild();
        String name = child.getNodeName();
        if (name.equals("SeriesOfScalar"))
          series = new SeriesOfScalar(child, this);
        else if (name.equals("SeriesOfVector"))
          series = new SeriesOfVector(child, this);
      }
      return series;
    }
  }

  public class SeriesOfVector extends ScalableSeries {
    private SeriesOfVector(Node node, MPEG7NodeInterface parent) {
      super(node, parent);
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
      return new MyVectorTimeSeries(text, dim, frames, timeunitMS(), parent);
    }
    public TimeSeriesCompatible[] getTimeSeriesList() {
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      TimeSeriesCompatible[] ts = new TimeSeriesCompatible[size];
      for (int i = 0; i < size; i++) {
        String text = getText(nl.item(i));
        int dim = vectorSize();
        int frames = totalNumOfSamples() / dim;
        ts[i] = new MyVectorTimeSeries(text, dim, frames, timeunitMS(), 
                                       parent);
      }
      return ts;
    }
  }

   public abstract class ScalableSeries extends MPEG7NodeInterface {
     private int timeunitMS = -1;
     MPEG7NodeInterface parent;
     private ScalableSeries(Node node, MPEG7NodeInterface parent) {
       super(node);
       this.parent = parent;
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
    private SeriesOfScalar(Node node, MPEG7NodeInterface parent) {
      super(node, parent);
    }
    protected String getSupportedNodeName() {
      return "SeriesOfScalar";
    }
    public TimeSeriesCompatible getTimeSeries(String elem) {
      String text = getText(getChildByTagNameNS(elem, MPEG7_NAMESPACE_URI));
      return new MyScalarTimeSeries(text, totalNumOfSamples(), timeunitMS(),
                                    parent);
    }
    public TimeSeriesCompatible[] getTimeSeriesList() {
      NodeList nl = getChildNodes();
      int size = nl.getLength();
      TimeSeriesCompatible[] ts = new TimeSeriesCompatible[size];
      for (int i = 0; i < size; i++)
        ts[i] = new MyScalarTimeSeries(getText(nl.item(i)), 
                                       totalNumOfSamples(), timeunitMS(), 
                                       parent);
      return ts;
    }
  }

/*
  public class Vector extends NodeInterface {
     private Vector(Node node) {
       super(node);
     }
    protected String getSupportedNodeName() {
      return "Vector";
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
*/

  
  private abstract class MyTimeSeries 
    implements TimeSeriesCompatible<DoubleArray> {
    int dim;
    int nFrames;
    private int timeunit;
    java.util.Queue<DoubleArray> queue;
    private MPEG7NodeInterface parent;
    private QueueWrapper<DoubleArray> qwrap;
    private MyTimeSeries(String text, int dim, int frames, int timeunit, 
                         MPEG7NodeInterface parent) {
      this.dim = dim;
      this.nFrames = frames;
      this.timeunit = timeunit;
      queue = new LinkedList<DoubleArray>();
      qwrap = new QueueWrapper(queue, nFrames);
      this.parent = parent;
    }
    public QueueReader<DoubleArray> getQueueReader() {
      return qwrap.createReader();
    }
//    public void finalizeQueueReader() {
//      qwrap.finalizeReader();
//    }
    public int dim() {
      return dim;
    }
    public int frames() {
      return nFrames;
    }
    public int timeunit() {
      return timeunit;
    }
    public int bytesize() {
      return 4 * dim();
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
      return parent.getAttribute(key);
    }
    public int getAttributeInt(String key) {
      return parent.getAttributeInt(key);
    }
    public double getAttributeDouble(String key) {
      return parent.getAttributeDouble(key);
    }
    public Iterator<Map.Entry<String,String>> getAttributeIterator() {
      throw new UnsupportedOperationException();
    }
  }

   private class MyScalarTimeSeries extends MyTimeSeries {
     private MyScalarTimeSeries(String text, int frames, int timeunit, 
                                MPEG7NodeInterface parent) {
       super(text, 1, frames, timeunit, parent);
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
                               int timeunit, MPEG7NodeInterface parent) {
      super(text, dim, frames, timeunit, parent);
      String[] ss = text.split("\n");
      if (ss.length != frames)
        throw new IllegalStateException("inconsistent data size");
      for (int n = 0; n < nFrames; n++) {
        DoubleArray array = parseArray(ss[n], " ");
//        String[] sss = ss[n].trim().split(" ");
        if (array.length() != dim)
          throw new IllegalStateException("inconsistent data size");
//        DoubleArray array = factory.createArray(dim);
//        for (int i = 0; i < dim; i++)
//          array.set(i, Double.parseDouble(sss[i]));
        queue.add(array);
      }
    }
  }

  public class ModelDescription extends Description {
    Model[] modellist = null;
    private ModelDescription(Node node) {
      super(node);
    }
    public Model[] getModelList() {
      if (modellist == null) {
        NodeList nl = selectNodeList(node(), "mpeg7:Model");
        int size = nl.getLength();
        modellist = new Model[size];
        for (int i = 0; i < size; i++) {
          Node node = nl.item(i);
          String type = getNodeType(node);
          if (type.equals("SoundModelType"))
            modellist[i] = new SoundModel(nl.item(i));
          else
            modellist[i] = new Model(nl.item(i));
        }
      }
      return modellist;
    }
  }

  /** Under construction */
  public class Model extends MPEG7NodeInterface {
    private Model(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "Model";
    }
  }

  public class SoundModel extends Model {
    private State[] states = null;
    private ContinuousDistribution[] dists = null;
    private SoundModel(Node node) {
      super(node);
    }
    public DoubleArray initial() {
      return parseArray(getChildText("Initial"));
    }
    public DoubleMatrix transitions() {
      return parseMatrix(getChildText("Transitions"));
    }
    public State[] getStateList() {
      if (states == null) {
        NodeList nl = selectNodeList(node(), "mpeg7:State");
        int size = nl.getLength();
        states = new State[size];
        for (int i = 0; i < size; i++)
          states[i] = new State(nl.item(i));
      }
      return states;
    }
    public ContinuousDistribution[] getObservationDistributionList() {
      if (dists == null) {
        NodeList nl = selectNodeList(node(), "mpeg7:ObservationDistribution");
        int size = nl.getLength();
        dists = new ContinuousDistribution[size];
        for (int i = 0; i < size; i++) {
          Node node = nl.item(i);
          String type = getNodeType(node);
          if (type.equals("GaussianDistributionType"))
            dists[i] = new GaussianDistribution(node);
          else
            dists[i] = new ContinuousDistribution(node);
        }
      }
      return dists;
    }
    public String soundClassLabel() {
      return getText(selectSingleNode(node(), 
                                      "mpeg7:SoundClassLabel/mpeg7:Name"));
    }
  }

  public class State extends Model {
    private State(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "State";
    }
  }

  public class ContinuousDistribution extends Model {
    private ContinuousDistribution(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "ObservationDistribution";
    }
  }

  public class GaussianDistribution extends ContinuousDistribution {
    private GaussianDistribution(Node node) {
      super(node);
    }
    DoubleArray mean() {
      return parseArray(getChildText("Mean"));
    }
    DoubleMatrix covarianceInverse() {
      return parseMatrix(getChildText("CovarianceInverse"));
    }
  }
}
  