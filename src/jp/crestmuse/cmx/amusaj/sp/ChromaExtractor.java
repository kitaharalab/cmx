package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static java.lang.Math.*;
import java.util.*;

//public class ChromaExtractor extends SPModule<PeakSet,SPDoubleArray> {
public class ChromaExtractor extends SPModule {
    private static final DoubleArrayFactory factory = DoubleArrayFactory.getFactory();

    private static final double Q = 60.0;

  private double fL = 20.0;
  private double fH = Double.POSITIVE_INFINITY;
  private boolean paramSet = false;
/*
    public void execute(List<QueueReader<PeakSet>> src, 
			List<TimeSeriesCompatible<SPDoubleArray>> dest)
	throws InterruptedException {
	PeakSet peaks = src.get(0).take();
	dest.get(0).add(new SPDoubleArray(calcChroma(peaks), peaks.hasNext()));
    }
*/

  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
      throws InterruptedException {
    PeakSet peaks = (PeakSet)src[0];
    dest[0].add(new SPDoubleArray(calcChroma(peaks)));
  }

  private void setParams() {
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    if (params.containsParam("chroma", "CHROMA_LOW_LIMIT_FREQ"))
      fL = params.getParamDouble("chroma", "CHROMA_LOW_LIMIT_FREQ");
    if (params.containsParam("chroma", "CHROMA_HIGH_LIMIT_FREQ"))
      fH = params.getParamDouble("chroma", "CHROMA_HIGH_LIMIT_FREQ");
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
	double sum = sum(chroma);
	if (sum > 0) divX(chroma, sum);
	return chroma;
    }
/*
    public int getInputChannels() {
	return 1;
    }

    public int getOutputChannels() {
	return 1;
    }
*/
    public Class<SPElement>[] getInputClasses() {
      return new Class[]{ PeakSet.class };
    }

    public Class<SPElement>[] getOutputClasses() {
      return new Class[]{ SPDoubleArray.class };
    }

    private static int Hz2nn(double x) {
      return 57 + (int)(12 * log(x / 220.0) / log(2));
    }

    private static double nn2Hz(int nn) {
      return 220 * pow(2, ((double)(nn - 57)) / 12.0);
    }
}

	    