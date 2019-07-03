package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.QueueReader;

import java.util.*;

public abstract class SPModule 
  implements ProducerConsumerCompatible {

    //private Map<String,String> params = null;

//  public TimeSeriesCompatible<E> createOutputInstance
//  (int nFrames, int timeunit) {
//    return new MutableTimeSeries<E>(getNumOfOutputFrames(nFrames), timeunit);
//  }

//  protected int getNumOfOutputFrames(int nInputFrames) {
//    return nInputFrames;
//  }

/*
  protected String getParamNameSpace() {
    return "param";
  }

  protected String getParamCategory() {
    return "default";
  }
  
  protected String[] getUsedParamNames() {
    return null;
  }
*/


/*
  public void setParams(Map<String,String> params) {
    this.params = params;
    String[] paramNames = getUsedParamNames();
    if (paramNames != null) {
      copyParamsFromConfigXML(getParamNameSpace(), 
                              getParamCategory(), paramNames);
                              
    }
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
*/

  public void terminated(TimeSeriesCompatible[] dest) {
  }

  public final void stop(QueueReader[] src, TimeSeriesCompatible[] dest) {}

  public void stop() {
  }
}
