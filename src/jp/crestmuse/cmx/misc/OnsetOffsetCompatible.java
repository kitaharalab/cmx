package jp.crestmuse.cmx.misc;

public interface OnsetOffsetCompatible {
  int onset(int ticksPerBeat);
  int onsetInMilliSec();
  int offset(int ticksPerBeat);
  int offsetInMilliSec();
  boolean supportsMilliSec();
  int duration(int ticksPerBeat);
}