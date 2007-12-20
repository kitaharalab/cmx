package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import org.w3c.dom.*;
import java.util.*;

public class IGRAMXMLWrapper extends 
                             AmusaXMLWrapper<TimeSeriesCompatible> {
  public static final String TOP_TAG = "igram";
  public static final String DATA_TAG = "igram-data";

  protected TimeSeriesCompatible createDataNodeInterface(Node node) {
    return new TimeSeriesNodeInterface(node) {
        protected String getSupportedNodeName() {
          return "igram-data";
        }
      };
  }

  public void addDataElement(TimeSeriesCompatible ts) {
    TimeSeriesNodeInterface.addTimeSeriesToWrapper(ts, DATA_TAG, this);
  }

  public TimeSeriesCompatible getIGRAMData(String inst) {
    List<TimeSeriesCompatible> data = getDataList();
    for (TimeSeriesCompatible d : data)
      if (d.getAttribute("inst").equals(inst))
        return d;
    return null;
  }

/*
  public IGRAMData getIGRAMData(String inst) {
    List<TimeSeriesCompatible> data = getDataList();
    for (TimeSeriesComopatible d : data)
      if (((IGRAMData)d).inst().equals(inst))
        return d;
    return null;
  }
*/


/*
  public class IGRAMData extends TimeSeriesNodeInterface {
    String inst;
    private IGRAMData(Node node) {
      super(node);
      inst = getAttribute("inst");
    }
    protected String getSupportedNodeName() {
      return "igram-data";
    }
    public String inst() {
      return inst;
    }
  }
*/
  
                                

}