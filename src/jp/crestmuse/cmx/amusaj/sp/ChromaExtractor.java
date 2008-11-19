package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static java.lang.Math.*;
import java.util.*;

public class ChromaExtractor extends SPModule<PeakSet,SPDoubleArray> {

    private static final DoubleArrayFactory factory = DoubleArrayFactory.getFactory();

    private static final double Q = 60.0;

  private double fL = 0.0;
  private double fH = Double.POSITIVE_INFINITY;
  private boolean paramSet = false;

    public void execute(List<QueueReader<PeakSet>> src, 
			List<TimeSeriesCompatible<SPDoubleArray>> dest)
	throws InterruptedException {
	PeakSet peaks = src.get(0).take();
	dest.get(0).add(new SPDoubleArray(calcChroma(peaks), peaks.hasNext()));
    }

  private void setParams() {
    if (containsParam("CHROMA_LOW_LIMIT_FREQ"))
      fL = getParamDouble("CHROMA_LOW_LIMIT_FREQ");
    if (containsParam("CHROMA_HIGH_LIMIT_FREQ"))
      fH = getParamDouble("CHROMA_HIGH_LIMIT_FREQ");
    paramSet = true;
  }
    
    private DoubleArray calcChroma(PeakSet peakset) {
      if (!paramSet) setParams();
	DoubleArray chroma = factory.createArray(12);
	int nPeaks = peakset.nPeaks();
	for (int i = 0; i < nPeaks; i++) {
	    double f = peakset.freq(i);
            if (f < fL || f > fH)
              continue;
	    int nn = Hz2nn(f);
	    double cf = nn2Hz(nn);
	    double df = cf - f;
	    double bw = cf / Q;
	    double w = exp(-df*df/(2*bw*bw));
	    chroma.set(nn%12, w * peakset.power(i));
	    cf = nn2Hz(nn+1);
	    df = cf - f;
	    bw = cf / Q;
	    w = exp(-df*df/(2*bw*bw));
	    chroma.set((nn+1)%12, w * peakset.power(i));
	}
        divX(chroma, sum(chroma));
	return chroma;
    }

    public int getInputChannels() {
	return 1;
    }

    public int getOutputChannels() {
	return 1;
    }

    private static int Hz2nn(double x) {
      return 57 + (int)(12 * log(x / 220.0) / log(2));
    }

    private static double nn2Hz(int nn) {
      return 220 * pow(2, ((double)(nn - 57)) / 12.0);
    }
}

	    