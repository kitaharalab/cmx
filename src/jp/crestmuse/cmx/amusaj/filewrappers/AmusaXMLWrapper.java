package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import java.util.*;
import org.w3c.dom.*;

/********************************************************************
 *音楽情景分析API「AMUSA-J」におけるファイルラッパの基底クラスです. 
 *AMUSA-Jでは, トップレベルタグ(タグ名は任意)の中に, 1つのheadタグと, 
 *1つ以上のデータタグ(タグ名は任意)が並ぶという構造を前提とします. 
 *各サブクラスにおいて, トップレベルタグ名はTOP_TAG, データタグ名は
 *DATA_TAGで定義しなければなりません. 
 *******************************************************************/
public abstract class AmusaXMLWrapper<D extends AmusaDataCompatible>
  extends CMXFileWrapper implements AmusaDataSetCompatible<D> {
  
  private Header header = null;
  private List<D> datalist = null;

  private String toptag = null;
  private String datatag = null;

  private static final String HEADER_TAG = "header";

  String toptag() {
    if (toptag == null) {
      try {
        toptag = (String)getClass().getField("TOP_TAG").get(this);
      } catch (NoSuchFieldException e) {
        throw new AmusaXMLException(e.toString());
      } catch (IllegalAccessException e) {
        throw new AmusaXMLException(e.toString());
      }
    }
    return toptag;
  }

  String datatag() {
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

  protected abstract void addDataElement(D d);

  public AmusaDataSet<D> createDataSet() {
    return new AmusaDataSet<D>(this);
  }

  private Header getHeader() {
    if (header == null)
      header = new Header(selectSingleNode("/" + toptag() + "/" + HEADER_TAG));
    return header;
  }

  public String[] getHeaderNameList() {
    return getHeader().getHeaderNameList();
  }

  public String getHeader(String key) {
    return getHeader().getHeaderElement(key);
  }

  public int getHeaderInt(String key) {
    return getHeader().getHeaderElementInt(key);
  }

  public double getHeaderDouble(String key) {
    return getHeader().getHeaderElementDouble(key);
  }

  public boolean containsHeaderKey(String key) {
    return getHeader().containsHeaderKey(key);
  }

  public void setHeader(String key, String value) {
    throw new UnsupportedOperationException();
  }

  public void setHeader(String key, int value) {
    throw new UnsupportedOperationException();
  }

  public void setHeader(String key, double value) {
    throw new UnsupportedOperationException();
  }

  public void add(D d) {
    throw new UnsupportedOperationException();
  }

  public List<D> getDataList() {
    if (datalist == null) {
      NodeList nl = selectNodeList("/" + toptag() + "/" + datatag());
      int size = nl.getLength();
      datalist = new ArrayList<D>();
      for (int i = 0; i < size; i++)
        datalist.add(createDataNodeInterface(nl.item(i)));
    }
    return datalist;
  }

}
                          
    