package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class AmusaParameterSet implements ParameterCompatible {

  private ParameterCompatible params = null;
  private HashMap<String,String> map = new HashMap<String,String>();
  private Set<String> usedKeys = new HashSet<String>();

  private static final AmusaParameterSet paramset = new AmusaParameterSet();

  private AmusaParameterSet() {

  }

  public static final AmusaParameterSet getInstance() {
    return paramset;
  }

  public final Set<String> keySet() {
    usedKeys.addAll(map.keySet());
    return usedKeys;
//    Set<String> s = map.keySet();
//    s.addAll(usedKeys);
    //    return s;
  }

  public final void setAnotherParameterSet(ParameterCompatible pc) {
    params = pc;
//    System.err.println(params);
  }

  public final void setParam(String key, String value) {
    String[] ss = key.split(":");
    setParam(ss[1], ss[2], value);
  }

  public final void setParam(String category, String key, String value) {
    map.put(PARAM_NAMESPACE + ":"+category+":"+key, value);
  }

  public final void setParam(String category, String key, int value) {
    setParam(category, key, String.valueOf(value));
  }

  public final void setParam(String category, String key, double value) {
    setParam(category, key, String.valueOf(value));
  }

  public boolean containsParam(String namespace, 
                                String category, String key) {
    String c = namespace + ":" + category + ":" + key;
    if (map.containsKey(c)) {
      usedKeys.add(c);
      return true;
    } else if (params.containsParam(namespace, category, key)) {
      usedKeys.add(c);
      return true;
    } else {
      return false;
    }
  }

  public final boolean containsParam(String category, String key) {
    return containsParam(PARAM_NAMESPACE, category, key);
  }

  public final boolean containsFilterParam(String filter, String param) {
    return containsParam(FILTER_NAMESPACE, filter, param);
  }

  public final String getParam(String key) {
    String[] ss = key.split(":");
    return getParam(ss[0], ss[1], ss[2]);
  }


  public String getParam(String namespace, String category, String key) {
    String c = namespace + ":" + category + ":" + key;
    if (map.containsKey(c)) {
      usedKeys.add(c);
      return map.get(c);
    } else if (params.containsParam(namespace, category, key)) {
      usedKeys.add(c);
      return params.getParam(namespace, category, key);
    } else
      return null;
  }

  public final int getParamInt(String namespace, String category, String key) {
    return Integer.parseInt(getParam(namespace, category, key));
  }

  public final double getParamDouble(String namespace, 
                                     String category, String key) {
    return Double.parseDouble(getParam(namespace, category,  key));
  }



  public final String getParam(String category, String key) {
    return getParam(PARAM_NAMESPACE, category, key);
  }

  public final int getParamInt(String category, String key) {
    return Integer.parseInt(getParam(category, key));
  }

  public final double getParamDouble(String category, String key) {
    return Double.parseDouble(getParam(category,  key));
  }

  public final String getFilterParam(String filter, String param) {
    return getParam(FILTER_NAMESPACE, filter, param);
  }

  public final int getFilterParamInt(String filter, String param) {
    return Integer.parseInt(getFilterParam(filter, param));
  }

  public final double getFilterParamDouble(String filter, String param) {
    return Double.parseDouble(getFilterParam(filter, param));
  }



}