package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.math.*;

public interface AudioDataCompatible {
  int channels();
  int sampleRate();
  DoubleArray[] getDoubleArrayWaveform();
}
