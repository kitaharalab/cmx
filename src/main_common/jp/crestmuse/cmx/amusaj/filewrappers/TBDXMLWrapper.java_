package jp.crestmuse.cmx.amusaj.filewrappers;
import org.w3c.dom.*;

public class TBDXMLWrapper extends AmusaXMLWrapper<TimeSeriesCompatible> {
  public static final String TOP_TAG = "tbd";
  public static final String DATA_TAG = "features";

  protected TimeSeriesCompatible createDataNodeInterface(Node node) {
    return new TimeSeriesNodeInterface(node) {
        protected String getSupportedNodeName() {
          return DATA_TAG;
        }
      };
  }

  public void addDataElement(TimeSeriesCompatible ts) {
    TimeSeriesNodeInterface.addTimeSeriesToWrapper(ts, DATA_TAG, this);
  }

}
