package jp.crestmuse.cmx.filewrappers.amusaj;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import org.w3c.dom.*;

public class IGRAMXMLWrapper extends AmusaXMLWrapper<IGRAMData> {
  public static final String TOP_TAG = "igram";
  public static final String DATA_TAG = "igram-data";
//  private Header header = null;
//  private IGRAMData[] data = null;

/*
  public Header getHeader() {
    if (header == null)
      header = new Header(selectSingleNode("/igram/header"));
    return header;
  }
*/

/*
  public IGRAMData[] getIGRAMDataList() {
    if (data == null) {
      NodeList nl = selectNodeList("/igram/igram-data");
      int size = nl.getLength();
      data = new IGRAMData[size];
      for (int i = 0; i < size ; i++) 
        data[i] = new IGRAMData(nl.item(i));
    }
    return data;
  }
*/

  protected IGRAMData createDataNodeInterface(Node node) {
    return new IGRAMData(node);
  }

  public IGRAMData getIGRAMData(String inst) {
    IGRAMData[] data = getDataList();
    for (IGRAMData d : data)
      if (d.inst().equals(inst))
        return d;
    return null;
  }

  public class IGRAMData extends TimeSeriesNodeInterface {
    String inst;
    private IGRAMData(Node node) {
      super(node, getDoubleArrayFactory());
      inst = getAttribute("inst");
    }
    protected String getSupportedNodeName() {
      return "igram-data";
    }
    public String inst() {
      return inst;
    }
  }

  
                                

}