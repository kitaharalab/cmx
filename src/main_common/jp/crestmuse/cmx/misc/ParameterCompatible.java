package jp.crestmuse.cmx.misc;

public interface ParameterCompatible {
  String PARAM_NAMESPACE = "param";
  String FILTER_NAMESPACE = "filters";
  String MDB_NAMESPACE = "mdb";
  String PROGNUM_NAMESPACE = "prognum";
//  boolean containsParam(String category, String key);
//  String getParam(String category, String key);
//  int getParamInt(String category, String key);
//  double getParamDouble(String category, String key);
//  boolean containsFilterParam(String filter, String param);
//  String getFilterParam(String filter, String param);
//  int getFilterParamInt(String filter, String param);
//  double getFilterParamDouble(String filter, String param);
  String getParam(String namespace, String category, String key);
  int getParamInt(String namespace, String category, String key);
  double getParamDouble(String namespace, String category, String key);
  boolean containsParam(String namespace, String category, String key);

}
