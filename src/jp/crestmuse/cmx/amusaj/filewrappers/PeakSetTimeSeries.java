package jp.crestmuse.cmx.amusaj.filewrappers;

public class PeakSetTimeSeries extends SPElement {
  PeakSetTimeSeries[] peaks;

  public PeakSetTimeSeries(int nFrames) {
    peaks = new PeakSetTimeSeries[nFrames];
  }

  public void set(int i, PeakSet peakset) {
    peaks[i] = peakset;
  }

  public PeakSet get(int i) {
    return peaks[i];
  }

  public int nFrames() {
    return peaks.length;
  }

}

    
