package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.misc.*;

public interface PeaksCompatible extends AmusaDataCompatible<PeakSet> {
  QueueReader<PeakSet> getQueueReader();
//  int frames();
  int bytesize();
  int timeunit();
  void addPeakSet(PeakSet peakset) throws InterruptedException;
}