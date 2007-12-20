package jp.crestmuse.cmx.filewrappers.amusaj;
import jp.crestmuse.cmx.math.*;

public interface TimeSeriesCompatible extends AmusaDataCompatible {
  public java.util.Queue<DoubleArray> getQueue();
  public int dim();
  public int frames();
  public int timeunit();
  public void add(DoubleArray array);
}
