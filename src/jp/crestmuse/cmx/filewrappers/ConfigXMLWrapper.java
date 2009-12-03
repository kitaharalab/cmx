package jp.crestmuse.cmx.filewrappers;
import java.util.*;
import org.w3c.dom.*;
import jp.crestmuse.cmx.misc.*;

public class ConfigXMLWrapper extends CMXFileWrapper 
implements ParameterCompatible {
  private Map<String,ParamSet> map = new HashMap<String,ParamSet>();

  private String getParam(String namespace, String category,  String key) {
    String c = namespace + ":" + category;
    if (map.containsKey(c)) {
      return map.get(c).getHeaderElement(key);
    } else {
      Node node = selectSingleNode("/config/"+namespace+"/*[@name='"+category+"']");
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

  public String getParam(String category, String key) {
    return getParam(PARAM_NAMESPACE, category, key);
  }

  public String getFilterParam(String filter, String param) {
    return getParam(FILTER_NAMESPACE, filter, param);
  }

  private boolean containsParam(String namespace, 
                                String category, String key) {
    String c = namespace + ":" + category;
//    System.err.println(c);
    ParamSet header;
    if (map.containsKey(c)) {
      header = map.get(c);
    } else {
      Node node = selectSingleNode("/config/"+namespace+"/*[@name='"+category+"']");
      if (node == null) return false;
      header = new ParamSet(node);
      map.put(c, header);
    }
    return header.containsHeaderKey(key);
  }

  public boolean containsParam(String category, String key) {
    return containsParam(PARAM_NAMESPACE, category, key);
  }

  public boolean containsFilterParam(String filter, String param) {
    return containsParam(FILTER_NAMESPACE, filter, param);
  }

  private class ParamSet extends AbstractHeaderNodeInterface {
    protected ParamSet(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "category|filter";
    }
  }

  public int getParamInt(String category, String key) {
    return Integer.parseInt(getParam(category, key));
  }

  public double getParamDouble(String category, String key) {
    return Double.parseDouble(getParam(category, key));
  }
                            
  public int getFilterParamInt(String filter, String param) {
    return Integer.parseInt(getFilterParam(filter, param));
  }

  public double getFilterParamDouble(String filter, String param) {
    return Double.parseDouble(getFilterParam(filter, param));
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