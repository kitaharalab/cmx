package jp.crestmuse.cmx.amusaj.filewrappers;
import org.w3c.dom.*;

public class FPDXMLWrapper extends AmusaXMLWrapper<TimeSeriesCompatible> {
  public static final String TOP_TAG = "fpd";
  public static final String DATA_TAG = "f0pdf";

  protected TimeSeriesCompatible createDataNodeInterface(Node node) {
    return new TimeSeriesNodeInterface(node) {
        protected String getSupportedNodeName() {
          return DATA_TAG;
        }
      };
  }

//  public AmusaDataSet<TimeSeriesCompatible> createDataSet() {
//    return new AmusaDataSet<TimeSeriesCompatible>(this);
//  }

  public void addDataElement(TimeSeriesCompatible ts) {
    TimeSeriesNodeInterface.addTimeSeriesToWrapper(ts, DATA_TAG, this);
  }
}