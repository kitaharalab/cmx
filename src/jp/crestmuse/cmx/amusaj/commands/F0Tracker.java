package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/** F0Tracker extracts the most prodominant harmonic structure 
    from the given WAV file. 
    To execute this class, the implementation of F0PDFCaclulator is 
    required. 
    This class is written to provide a simple example of our 
    signal processing API, and therefore does not have a sufficient 
    accuracy. 
*/
public class F0Tracker extends AbstractWAVAnalyzer {
  private ProducerConsumerCompatible stft, peakext, f0calc, f0track, 
    spread, harmext;
  private static final DoubleArrayFactory factory = DoubleArrayFactory.getFactory();

  protected boolean setOptionsLocal(String option, String value) {
    if (super.setOptionsLocal(option, value)) 
      return true;
    else if (OptionUtils.setF0PDFOptions(option, value))
      return true;
    else
      return false;
  }

  protected ProducerConsumerCompatible[] getUsedModules() {
    return new ProducerConsumerCompatible[] {
      stft = new STFT(usesStereo()), 
      peakext = new PeakExtractor(), 
      spread = new SPSpreadModule(PeakSet.class, 2), 
      f0calc = new F0PDFCalculatorModule(), 
      f0track = new F0TrackingModule(), 
      harmext = new HarmonicsExtractor()
    };
  }

  protected ModuleConnection[] getModuleConnections() {
    return new ModuleConnection[] {
      new ModuleConnection(getWindowSlider(), 0, stft, 0), 
      new ModuleConnection(stft, 0, peakext, 0), 
      new ModuleConnection(stft, 1, peakext, 1), 
      new ModuleConnection(stft, 2, peakext, 2), 
      new ModuleConnection(peakext, 0, spread, 0), 
      new ModuleConnection(spread, 0, harmext, 0), 
      new ModuleConnection(spread, 1, f0calc, 0), 
      new ModuleConnection(f0calc, 0, f0track, 0), 
      new ModuleConnection(f0track, 0, harmext, 1)
    };
  }
  
  protected OutputData[] getOutputData() {
    return new OutputData[] {
      new OutputData(harmext, 0)
    };
  }

  protected String getAmusaXMLFormat() {
    return "peaks";
  }

  public static void main(String[] args) {
    F0Tracker f0track = new F0Tracker();
    try {
      f0track.start(args);
    } catch (Exception e) {
      f0track.showErrorMessage(e);
      System.exit(1);
    }
  }

  class F0TrackingModule extends SPModule {
    double thrs = 0.2;
    MaxResult maxresult = new MaxResult();
    double nnFrom = -1, step = -1;
    
    public void execute(Object[] src, TimeSeriesCompatible[] dest) throws InterruptedException {
      DoubleArray f0pdf = (DoubleArray)src[0];
      max(f0pdf, maxresult);
      if (maxresult.max > thrs) {
        nnFrom = nnFrom<0 ? getParamDouble("f0pdf","NOTENUMBER_FROM") : nnFrom;
        step = step < 0 ? getParamDouble("f0pdf", "STEP") : step;
        double f0 = nn2Hz(nnFrom + step * maxresult.argmax);
        dest[0].add(factory.createArray(1, f0));
      } else {
        dest[0].add(factory.createArray(1, 0.0));
      }
    }
  
    public Class[] getInputClasses() {
      return new Class[] { DoubleArray.class };
    }
    
    public Class[] getOutputClasses() {
      return new Class[] { DoubleArray.class };
    }
  }
}
                    
                                      