package jp.crestmuse.cmx.amusaj.filewrappers;
import java.util.*;

public interface PeaksCompatible extends AmusaDataCompatible {
  java.util.Queue<PeakSet> getQueue();
  int frames();
  int bytesize();
  int timeunit();
  void addPeakSet(PeakSet peakset) throws InterruptedException;
}