package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static java.lang.Math.*;

public class PeakExtractor {

    private static double powerthrs = 0;
    private static double rpowerthrs = 0;

    private double[][] fftresult, fftresultL, fftresultR;
    private int fs;
    private int winsize;
    private boolean isStereo;

    private int nFrames;
    private int size;
    private double[] power0;
    private DoubleArray freq, power, phase, iid, ipd;
    private int t;

  private DoubleArrayFactory factory = DoubleArrayFactory.getFactory();
  private ConfigXMLWrapper config;

    public PeakExtractor() {
      this.factory = factory;
      config = CMXCommand.getConfigXMLWrapper();
      powerthrs = config.getParamDouble("param", "fft", "POWER_THRESHOLD");
      rpowerthrs = config.getParamDouble("param", "fft", 
                                         "RELATIVE_POWER_THRESHOLD");
    }

    public void setFFTResult(double[][] fftresult, int fs, int winsize) {
	this.fftresult = fftresult;
	fftresultL = null;
	fftresultR = null;
	this.fs = fs;
	this.winsize = winsize;
	isStereo = false;
	t = 0;
	this.nFrames = fftresult.length;
	reset(fftresult[0].length / 2);
    }

    public void setFFTResult(double[][] fftresult, double[][] fftresultL, 
			     double[][] fftresultR, int fs, int winsize) {
	this.fftresult = fftresult;
	this.fftresultL = fftresultL;
	this.fftresultR = fftresultR;
	this.fs = fs;
	this.winsize = winsize;
	isStereo = (fftresultL != null && fftresultR != null);
	t = 0;
	this.nFrames = fftresult.length;
	reset(fftresult[0].length / 2);
    }

//    public final int getSize() {
//	return size;
//    }

    public final int sampleRate() {
	return fs;
    }

    public final int nFrames() {
	return nFrames;
    }

    private void reset(int size) {
	this.size = size;
	power0 = new double[size];
	freq = factory.createArray(size);
	power = factory.createArray(size);
	phase = factory.createArray(size);
	iid = factory.createArray(size);
	ipd = factory.createArray(size);
    }

    public void execute(PeaksCompatible peaks) throws InterruptedException {
	double maxpower = Double.NEGATIVE_INFINITY;
	if (size != fftresult[t].length / 2) 
	    reset(fftresult[t].length / 2);
	for (int i = 0; i < size; i++) {
	    double pp = hypot(fftresult[t][2*i], fftresult[t][2*i+1]);
	    power0[i] = pp;
	    if (pp > maxpower)
		maxpower = pp;
	}
	double powerthrs2 = maxpower * rpowerthrs;
	int k = 0, i = 2;
	while (i < size-2) {
	    if (power0[i] > power0[i+1]) {
		if (power0[i+1] > power0[i+2]) {
		    if (power0[i] > power0[i-1]
			&& power0[i-1] > power0[i-2] 
			&& power0[i] > powerthrs
			&& power0[i] > powerthrs2) {
			freq.set(k, (double)(i * fs) / (double)winsize);
			power.set(k, power0[i]);
			phase.set(k, atan2(fftresult[t][2*i+1], 
					   fftresult[t][2*i]));
			if (isStereo) {
			    iid.set(k, hypot(fftresultR[t][2*i], 
					     fftresultR[t][2*i+1])
				    / hypot(fftresultL[t][2*i], 
					    fftresultL[t][2*i+1]));
			    ipd.set(k, atan2(fftresultR[t][2*i+1], 
					     fftresultR[t][2*i])
				    - atan2(fftresultL[t][2*i+1], 
					    fftresultL[t][2*i]));
			} else {
			    iid.set(k, 1.0);
			    ipd.set(k, 0.0);
			}
			k++;
			i += 4;
		    } else {
			i += 4;
		    }
		} else {
		    i += 3;
		}
	    } else if (power0[i] <= power0[i-1]) {
		i += 2;
	    } else {
		i++;
	    }
	}
	peaks.addPeakSet
          (new PeakSet(subarray(freq, 0, k), subarray(power, 0, k), 
		       subarray(phase, 0, k), subarray(iid, 0, k), 
		       subarray(ipd, 0, k)));
	System.err.print("$");
	t++;
    }
}
