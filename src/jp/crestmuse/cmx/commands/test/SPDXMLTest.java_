package jp.crestmuse.cmx.commands.test;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

public class SPDXMLTest extends CMXCommand {
  protected void run() {
    SPDXMLWrapper spd = (SPDXMLWrapper)indata();
    TimeSeriesCompatible<PeakSet> peaks = spd.getDataList().get(0);
    QueueReader<PeakSet> queue = peaks.getQueueReader();
    int t = 0;
    for (PeakSet peakset : queue) {
      int nPeaks = peakset.nPeaks();
      for (int i = 0; i < nPeaks; i++)
        System.out.println
          (t + "\t" + peakset.freq(i) + "\t" + peakset.power(i)
           + "\t" + peakset.phase(i) + "\t" + peakset.iid(i)
           + "\t" + peakset.ipd(i));
      t++;
    }
  }

  public static void main(String[] args) {
    SPDXMLTest t = new SPDXMLTest();
    try {
      t.start(args);
    } catch (Exception e) {
      t.showErrorMessage(e);
      System.exit(1);
    }
  }
}