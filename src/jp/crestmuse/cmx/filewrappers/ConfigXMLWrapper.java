package jp.crestmuse.cmx.filewrappers;
import java.util.*;
import org.w3c.dom.*;

public class ConfigXMLWrapper extends CMXFileWrapper {
  public Map<String,ParamSet> map = new HashMap<String,ParamSet>();
  public String getParam(String category, final String subcategory, 
                         String key) {
    String c = category + ":" + subcategory;
    if (map.containsKey(c)) {
      return map.get(c).getHeaderElement(key);
    } else {
      Node node = selectSingleNode("/config/"+category+"/category[@name='"+subcategory+"']");
      ParamSet header = new ParamSet(node);
//      AbstractHeaderNodeInterface header = 
//        new AbstractHeaderNodeInterface(node) {
//          protected String getSupportedNodeName() {
//            return subcategory;
//          }
//        };
      map.put(c, header);
      return header.getHeaderElement(key);
    }
  }

  private class ParamSet extends AbstractHeaderNodeInterface {
    protected ParamSet(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "category";
    }
  }

  public int getParamInt(String category, String subcategory, String key) {
    return Integer.parseInt(getParam(category, subcategory, key));
  }

  public double getParamDouble(String category, String subcategory, 
                               String key) {
    return Double.parseDouble(getParam(category, subcategory, key));
  }
                            
  
  
/*
  public String getParam(String category, String subcategory, String key) {
    return NodeInterface.getText
      (selectSingleNode("/config/"+category+"/"+subcategory+"/"+key));
  }

  public int getParamInt(String category, String subcategory, String key) {
    return NodeInterface.getTextInt
      (selectSingleNode("/config/"+category+"/"+subcategory+"/"+key));
  }

  public double getParamDouble
  (String category, String subcategory, String key) {
    return NodeInterface.getTextDouble
      (selectSingleNode("/config/"+category+"/"+subcategory+"/"+key));
  }
*/
}