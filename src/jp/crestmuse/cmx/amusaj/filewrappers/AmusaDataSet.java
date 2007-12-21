package jp.crestmuse.cmx.amusaj.filewrappers;
import java.util.*;

public class AmusaDataSet<D extends AmusaDataCompatible> 
  implements AmusaDataSetCompatible<D> {
  Map<String,String> header;
  List<D> data;
  private AmusaXMLWrapper wrapper;
  
  AmusaDataSet(AmusaXMLWrapper wrapper) {
    this.wrapper = wrapper;
    header = new HashMap<String,String>();
    data = new ArrayList<D>();
  }

/*********************************************************************
 *Returns the item corresponding to the specified key in the header. 
 *<br>
 *ヘッダ内の指定されたキーに対応する項目を返します. 
 *********************************************************************/
  public String getHeader(String key) {
    return header.get(key);
  }

/*********************************************************************
 *Returns in integer the item corresponding to the specified key 
 *in the header. <br>
 *ヘッダ内の指定されたキーに対応する項目をinteger型で返します. 
 *********************************************************************/
  public int getHeaderInt(String key) {
    return Integer.parseInt(getHeader(key));
  }

/*********************************************************************
 *Returns in double the item corresponding to the specified key 
 *in the header. <br>
 *ヘッダ内の指定されたキーに対応する項目をdouble型で返します. 
 *********************************************************************/
  public double getHeaderDouble(String key) {
    return Double.parseDouble(getHeader(key));
  }

/*********************************************************************
 *Tests if the specified key is contained in the header. <br>
 *指定されたキーがヘッダに含まれているか調べます. 
 *********************************************************************/
  public boolean containsHeaderKey(String key) {
    return header.containsKey(key);
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String key, String value) {
    header.put(key, value);
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String key, int value) {
    header.put(key, String.valueOf(value));
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String key, double value) {
    header.put(key, String.valueOf(value));
  }

  public void setHeaders(Map<String,Object> map) {
    Set<Map.Entry<String,Object>> entrySet = map.entrySet();
    for (Map.Entry<String,Object> e : entrySet)
      header.put(e.getKey(), e.getValue().toString());
  }
       
  public void setHeaders(Map<String,Object> map, String... keys) {
    for (String key : keys) 
      header.put(key, map.get(key).toString());
  }       

/********************************************************************
 *Adds data. <br>
 *データを追加します. 
 ********************************************************************/
  public void add(D d) {
    data.add(d);
  }

  public List<D> getDataList() {
    return data;
  }

  public void addElementsToWrapper() {
    wrapper.addChild("header");
    Set<Map.Entry<String,String>> header = this.header.entrySet();
    for (Map.Entry<String,String> e : header) {
      wrapper.addChild("meta");
      wrapper.setAttribute("name", e.getKey());
      wrapper.setAttribute("content", e.getValue());
      wrapper.returnToParent();
    }
    wrapper.returnToParent();
    for (D d : data) 
      wrapper.addDataElement(d);
  }
}
