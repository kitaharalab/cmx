import jp.crestmuse.cmx.amusaj.sp.*
import static jp.crestmuse.cmx.math.Utils.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*
import jp.crestmuse.cmx.amusaj.commands.*
import jp.crestmuse.cmx.math.*

DoubleArray.mixin(Operations)

/***********************************************************************

 ***********************************************************************/

args = "-conf config.xml sample1.wav"

class MyF0Tracker extends SPModule {

  void execute(Object[] src, TimeSeriesCompatible[] dest) {
    def freq = src[0].freq()
    def power = src[0].power()
    def power_thrs = 0.001 * power.sum()
    def f0 = 0.0
    if (freq.length() > 0) {
      for (i in 0..(freq.length()-1)) {
        if (power[i] > power_thrs && freq[i] > 20) {
          f0 = freq[i]
          break
        }
      }
    }
    dest[0].add(create1dimDoubleArray(f0))
  }

  Class[] getInputClasses() {
    [PeakSet.class]
  }

  Class[] getOutputClasses() {
    [DoubleArray.class]
  }
}

class SimpleF0Tracker extends AbstractWAVAnalyzer {

  ProducerConsumerCompatible stft, peakext, f0track

  ProducerConsumerCompatible[] getUsedModules(){
      stft = new STFT(usesStereo())
      peakext = new PeakExtractor() 
      f0track = new MyF0Tracker()
      [stft, peakext, f0track]
  }

  ModuleConnection[] getModuleConnections(){
    [  new ModuleConnection(getWindowSlider(), 0, stft, 0), 
      new ModuleConnection(stft, 0, peakext, 0), 
      new ModuleConnection(stft, 1, peakext, 1), 
      new ModuleConnection(stft, 2, peakext, 2), 
      new ModuleConnection(peakext, 0, f0track, 0) ]
  }

  String getAmusaXMLFormat() {
    return "array";
  }

  OutputData[] getOutputData() {
    [  new OutputData(f0track, 0) ]
  }
}

// main
(new SimpleF0Tracker()).start(args)


