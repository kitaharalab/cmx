package jp.crestmuse.cmx.filewrappers;

public class ConfigXMLWrapper extends CMXFileWrapper {
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

}