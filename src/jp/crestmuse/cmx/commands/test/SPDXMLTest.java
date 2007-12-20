package jp.crestmuse.cmx.commands.test;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import java.util.*;

public class SPDXMLTest extends CMXCommand {
  protected void run() {
    SPDXMLWrapper spd = (SPDXMLWrapper)indata();
    PeaksCompatible peaks = spd.getDataList().get(0);
    Queue<PeakSet> queue = peaks.getQueue();
    int t = 0;
    for (PeakSet peakset : queue) {
      int nPeaks = peakset.nPeaks();
      for (int i = 0; i < nPeaks; i++)
        System.out.println
          (t + "\t" + peakset.freq(i) + "\t" + peakset.power(i));
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