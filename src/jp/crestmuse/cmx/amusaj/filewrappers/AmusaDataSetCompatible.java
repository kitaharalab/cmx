package jp.crestmuse.cmx.amusaj.filewrappers;
import java.util.*;
import jp.crestmuse.cmx.filewrappers.*;

public interface AmusaDataSetCompatible<D extends TimeSeriesCompatible> 
    extends FileWrapperCompatible {
  public String getHeader(String key);
  public int getHeaderInt(String key);
  public double getHeaderDouble(String key);
  public boolean containsHeaderKey(String key);
  public void setHeader(String key, String value);
  public void setHeader(String key, int value);
  public void setHeader(String key, double value);
  public void add(D d);
  public List<D> getDataList();
}
