package jp.crestmuse.cmx.filewrappers.amusaj;
import org.w3c.dom.*;

public class SPDXMLWrapper extends AmusaXMLWrapper<Peaks> {
  public static final String TOP_TAG = "spd";
  public static final String DATA_TAG = "peaks";

  protected Peaks createDataNodeInterface(Node node) {
    return new Peaks(node);
  }
}