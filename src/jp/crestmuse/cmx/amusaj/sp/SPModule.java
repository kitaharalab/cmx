package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import java.util.*;

public abstract class SPModule<D,E> 
  implements ProducerConsumerCompatible<D,E> {

  private Map<String,String> params = null;

  public TimeSeriesCompatible<E> createOutputInstance
  (int nFrames, int timeunit) {
    return new MutableTimeSeries<E>(nFrames, timeunit);
  }

  public void setParams(Map<String,String> params) {
    this.params = params;
  }

  protected String getParam(String key) {
    return params.get(key);
  }

  protected int getParamInt(String key) {
    return Integer.parseInt(getParam(key));
  }

  protected double getParamDouble(String key) {
    return Double.parseDouble(getParam(key));
  }

  protected boolean containsParam(String key) {
    return (params != null) && params.containsKey(key);
  }

  protected void setParam(String key, String value) {
    if (params != null) params.put(key, value);
  }

  protected void setParam(String key, int value) {
    if (params != null) params.put(key, String.valueOf(value));
  }

  protected void setParam(String key, double value) {
    if (params != null) params.put(key, String.valueOf(value));
  }

  protected void copyParamsFromConfigXML(String namespace, String category, 
                                         String... keys) {
    ConfigXMLWrapper config = CMXCommand.getConfigXMLWrapper();
    for (String key : keys) 
      if (!containsParam(key))
        setParam(key, config.getParam(namespace, category, key));
  }
}
