package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.math.*;

public class MutableComplexTimeSeries extends MutableData<ComplexArray> {
  private int dim = -1;

  private static final ComplexArrayFactory factory = 
    ComplexArrayFactory.getFactory();

  public MutableComplexTimeSeries(int nFrames, int timeunit) {
    super(nFrames, timeunit);
  }

  public int dim() {
    return dim;
  }

  public int bytesize() {
    return 8 * dim;
  }

  public void add(ComplexArray array) throws InterruptedException {
    if (dim == -1) {
      dim = array.length();
      queue.put(array);
    } else if (dim == array.length()) {
      queue.put(array);
    } else {
      throw new IllegalStateException("unmatch dimension");
    }
  }
}