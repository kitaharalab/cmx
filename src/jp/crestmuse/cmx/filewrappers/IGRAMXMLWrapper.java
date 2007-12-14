package jp.crestmuse.cmx.filewrappers;
import jp.crestmuse.cmx.math.*;
import org.w3c.dom.*;

public class IGRAMXMLWrapper extends CMXFileWrapper {
  public static final String TOP_TAG = "igram";
  private GenericHeader header = null;
  private IGRAMData[] data = null;

  public GenericHeader getHeader() {
    if (header == null)
      header = new GenericHeader(selectSingleNode("/igram/header"));
    return header;
  }

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

  public IGRAMData getIGRAMData(String inst) {
    IGRAMData[] data = getIGRAMDataList();
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