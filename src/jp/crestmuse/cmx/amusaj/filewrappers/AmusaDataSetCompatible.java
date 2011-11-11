package jp.crestmuse.cmx.amusaj.filewrappers;
import java.util.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;

public interface AmusaDataSetCompatible<D extends TimeSeriesCompatible> 
    extends FileWrapperCompatible {
  public String getHeader(String category, String key);
  public int getHeaderInt(String category, String key);
  public double getHeaderDouble(String category, String key);
  public boolean containsHeaderKey(String category, String key);
  public void setHeader(String category, String key, String value);
  public void setHeader(String category, String key, int value);
  public void setHeader(String category, String key, double value);
  public void add(D d);
  public List<D> getDataList();
}
