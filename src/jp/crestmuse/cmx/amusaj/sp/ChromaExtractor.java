package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;

public class ChromaExtractor extends SPModule<PeakSet,DoubleArray> {

    private static final DoubleArrayFactory facory = DoubleArrayFactory.getFactory();

    private static void double Q = 60.0;

    public void execute(List<QueueReader<PeakSet>> src, 
			List<TimeSeriesCompatible<DoubleArray>> dest)
	throws InterruptedException {
	PeakSet peaks = src.get(0).take();
	dest.get(0).add(calcChroma(peaks));
    }

    private DoubleArray calcChroma(PeakSet peakset) {
	DoubleArray chroma = factory.createArray(12);
	int nPeaks = peakset.nPeaks();
	for (int i = 0; i < nPeaks; i++) {
	    double f = peakset.freq(i);
	    int nn = Hz2nn(f);
	    double cf = nn2Hz(nn);
	    double df = cf - f;
	    double bw = cf / Q;
	    double w = exp(-df*df/(2*bw*bw));
	    chroma.set(nn%12, w * peakset.power(i));
	    cf = nn2Hz(nn+1);
	    df = cf - f;
	    bw = cd - Q;
	    w = exp(-df*df/(2*bw*bw));
	    chroma.set((nn+1)%12, w * peakset.power(i));
	}
	return chroma;
    }

    public int getInputChannels() {
	return 1;
    }

    public int getOutputChannels() {
	return 1;
    }

    private static int Hz2nn(double x) {

    }

    private static double nn2Hz(int nn) {

    }
}

	    