package jp.crestmuse.cmx.amusaj.filewrappers;
import org.w3c.dom.*;

public class SPDXMLWrapper extends AmusaXMLWrapper<PeaksCompatible> {
  public static final String TOP_TAG = "spd";
  public static final String DATA_TAG = "peaks";

  protected PeaksCompatible createDataNodeInterface(Node node) {
    return new Peaks(node);
  }

  public AmusaDataSet<PeaksCompatible> createDataSet() {
    return new AmusaDataSet<PeaksCompatible>(this);
  }

  public void addDataElement(PeaksCompatible peaks) {
    Peaks.addPeaksToWrapper(peaks, DATA_TAG, this);
  }
}