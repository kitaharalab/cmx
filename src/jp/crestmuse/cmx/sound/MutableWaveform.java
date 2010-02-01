package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Utils.*;
import static jp.crestmuse.cmx.math.Operations.*;
import static jp.crestmuse.cmx.math.SP.*;
import javax.sound.sampled.*;

public class MutableWaveform implements AudioDataCompatible,Cloneable {

    private DoubleArray[] waveform;
    private int sampleRate;

    public MutableWaveform(double length, int sampleRate, int channels) {
	int len = (int)(length * sampleRate);
	waveform = new DoubleArray[channels];
	for (int i = 0; i < channels; i++)
	    waveform[i] = createDoubleArray(len);
	this.sampleRate = sampleRate;
    }

    public MutableWaveform(DoubleArray[] waveform, int sampleRate) {
	this.waveform = waveform;
	this.sampleRate = sampleRate;
    }

    public MutableWaveform(AudioDataCompatible audiodata) {
	this.waveform = audiodata.getDoubleArrayWaveform();
	this.sampleRate = audiodata.sampleRate();
    }

    public Object clone() {
	try {
	    DoubleArray[] newwaveform = new DoubleArray[waveform.length];
	    for (int i = 0; i < waveform.length; i++)
		newwaveform[i] = (DoubleArray)waveform[i].clone();
	    return new MutableWaveform(newwaveform, sampleRate);
	} catch (CloneNotSupportedException e) {
	    throw new UnsupportedOperationException("Clone not supported");
	}
    }

    public int sampleRate() {
	return sampleRate;
    }

    private int indexOfLast() {
	for (int t = waveform[0].length() - 1; t >= 0; t--) 
	    for (int i = 0; i < waveform.length; i++)
		if (waveform[i].get(t) != 0)
		    return t;
	return 0;
    }

    public void trim() {
	cutAfter(indexOfLast());
    }

    public void trim(int lastSilence) {
	cutAfter(indexOfLast() + lastSilence);
    }
    
    public void cutAfter(int t) {
	for (int i = 0; i < waveform.length; i++)
	    waveform[i] = 
		waveform[i].subarrayX(0, Math.min(t+1, waveform[i].length()));
    }

    public void smoothOffset() {
	for (int i = 0; i < waveform.length; i++)
	    for (int t  = 1; t < 100; t++)
		mulX(waveform[i], waveform[i].length() - t, (double)t / 100.0);
    }

    public void cutBefore(int t) {
	for (int i = 0; i < waveform.length; i++)
	    waveform[i] = waveform[i].subarrayX(t, waveform[i].length());
    }

    int calcOnset(int n, double thresh) {
	int onset = Integer.MAX_VALUE;
	int localonset;
	for (int i = 0; i < waveform.length; i++)
	    if ((localonset = detectOnset(waveform[i], n, thresh)) < onset)
		onset = localonset;
	return onset;
    }

    public void cutBeforeOnset(int n, double thresh) {
	cutBefore(calcOnset(n, thresh));
    }
    
    public void mix(AudioDataCompatible audiodata, int onset, double weight) {
	if (onset < 0) return;
	if (sampleRate != audiodata.sampleRate())
	    throw new IllegalStateException("Sample rate mismatch");
	DoubleArray[] w = audiodata.getDoubleArrayWaveform();
	if (waveform.length == w.length) {
	    for (int i = 0; i < waveform.length; i++)
		for (int t = 0; t < w[0].length(); t++)
		    addX(waveform[i], t+onset, w[i].get(t) * weight);
	} else if (w.length == 1) {
	    for (int i = 0; i < waveform.length; i++)
		for (int t = 0; t < w[0].length(); t++)
		    addX(waveform[i], t+onset, w[0].get(t) * weight);
	} else if (w.length == 2) {
	    for (int i = 0; i < waveform.length; i++)
		for (int t = 0; t < w[0].length(); t++)
		    addX(waveform[i], t+onset, 
			(w[0].get(t) + w[1].get(t)) * weight / 2);
	} else {
	    throw new IllegalStateException("Num of channels mismatch");
	}
    }

    public void mix_st(AudioDataCompatible audiodata, int onsetL, int onsetR, 
		       double weightL, double weightR) {
	if (onsetL < 0 || onsetR < 0) return;
	if (sampleRate != audiodata.sampleRate())
	    throw new IllegalStateException("Sample rate mismatch");
	DoubleArray[] w = audiodata.getDoubleArrayWaveform();
	if (w.length == 1) {
	    for (int t = 0; t < w[0].length(); t++) {
		addX(waveform[0], t+onsetL, w[0].get(t) * weightL);
		addX(waveform[1], t+onsetR, w[0].get(t) * weightR);
	    }
	} else if (w.length == 2) {
	    for (int t = 0; t < w[0].length(); t++) {
		addX(waveform[0], t+onsetL, w[0].get(t) * weightL);
		addX(waveform[1], t+onsetR, w[1].get(t) * weightR);
	    }
	} else {
	    throw new IllegalStateException("Num of channels mismatch");
	}
    }

    public void conv(DoubleArray imp) {
	for (int i = 0; i < waveform.length; i++) 
	    waveform[i] = Operations.conv(waveform[i], imp);
    }

    public void conv_st(DoubleArray impL, DoubleArray impR) {
	waveform[0] = Operations.conv(waveform[0], impL);
	waveform[1] = Operations.conv(waveform[1], impR);
    }

    public void normalize() {
	double max = 0.0;
	double localmax;
	for (int i = 0; i < waveform.length; i++)
	    if ((localmax = absmax(waveform[i])) > max)
		max = localmax;
	for (int i = 0; i < waveform.length; i++)
	    divX(waveform[i], max * 1.01);
    }

    public boolean supportsWholeWaveformGetter() {
	return true;
    }

  private int next = 0;

  public DoubleArray[] readNext(int sampleSize, int nOverlap) {
      throw new UnsupportedOperationException();
  }

  public boolean hasNext(int sampleSize) {
      throw new UnsupportedOperationException();
  }

    public AudioFormat getAudioFormat() {
	throw new UnsupportedOperationException();
    }

    public byte[] getByteArrayWaveform() {
	throw new UnsupportedOperationException();
    }

    public DoubleArray[] getDoubleArrayWaveform() {
	return waveform;
    }

    public int channels() {
	return waveform.length;
    }

    public MutableWaveform changeSampleRate(int newrate) {
	DoubleArray[] w = new DoubleArray[waveform.length];
	for (int i = 0; i < waveform.length; i++) 
	    w[i] = changeRate(waveform[i], sampleRate, newrate);
	return new MutableWaveform(w, newrate);
    }

}
	
	