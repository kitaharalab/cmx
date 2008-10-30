package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.math.*;
import java.util.*;

public class AmusaDecoder {

  private static final AmusaDecoder decoder = new AmusaDecoder();
  private static final DoubleArrayFactory factory = 
    DoubleArrayFactory.getFactory();

  private AmusaDecoder() {
    // do nothing
  }

  public static final AmusaDecoder getInstance() {
    return decoder;
  }

  public Object decode(StringTokenizer st, String format, int dim) {
    if (format.equals("array")) {
      DoubleArray array = factory.createArray(dim);
      for (int i = 0; i < dim; i++)
        array.set(i, Double.parseDouble(st.nextToken()));
      return array;
    } else if (format.equals("peaks")) {
      int nPeaks = Integer.parseInt(st.nextToken());
      PeakSet peakset = new PeakSet(nPeaks);
      for (int i = 0; i < nPeaks; i++)
        peakset.setPeak(i, Double.parseDouble(st.nextToken()), 
                        Double.parseDouble(st.nextToken()), 
                        Double.parseDouble(st.nextToken()), 
                        Double.parseDouble(st.nextToken()), 
                        Double.parseDouble(st.nextToken()));
      return peakset;
    } else {
      throw new UnsupportedFormatException(format);
    }
  }
}