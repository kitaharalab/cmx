package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.math.*;

public class MutableVectorTimeSeries<D extends Array> 
  extends MutableTimeSeries<D> {
  private int dim = -1;

  public MutableVectorTimeSeries(int nFrames, int timeunit) {
    super(nFrames, timeunit);
  }

  public int dim() {
    return dim;
  }

  public void add(D array) throws InterruptedException {
    int dim1;
    if (dim == (dim1 = array.length())) {
      queue.put(array);
    } else if (dim == -1) {
      dim = dim1;
      queue.put(array);
    } else {
      throw new IllegalStateException("unmatch dimension");
    }
  }
}
