package jp.crestmuse.cmx.filewrappers.amusaj;
import jp.crestmuse.cmx.filewrappers.*;
import java.util.*;
import org.w3c.dom.*;

public abstract class AmusaXMLWrapper<D extends NodeInterface>
  extends CMXFileWrapper {
  
  private Header header = null;
  private D[] datalist = null;

  private String toptag = null;
  private String datatag = null;

  private static final String HEADER_TAG = "head";

  private String toptag() {
    if (toptag == null) {
      try {
        toptag = (String)getClass().getField("TAP_TAG").get(this);
      } catch (NoSuchFieldException e) {
        throw new AmusaXMLException(e.toString());
      } catch (IllegalAccessException e) {
        throw new AmusaXMLException(e.toString());
      }
    }
    return toptag;
  }

  private String datatag() {
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

  protected abstract D createDataNodeInterface(Node node);

  public Header getHeader() {
    if (header == null)
      header = new Header(selectSingleNode("/" + toptag() + "/" + HEADER_TAG));
    return header;
  }

  public D[] getDataList() {
    if (datalist == null) {
      NodeList nl = selectNodeList("/" + toptag() + "/" + datatag());
      int size = nl.getLength();
      NodeInterface[] ni = new NodeInterface[size];
      for (int i = 0; i < size; i++)
        ni[i] = createDataNodeInterface(nl.item(i));
      datalist = (D[])ni;
    }
    return datalist;
  }
}
                          
    