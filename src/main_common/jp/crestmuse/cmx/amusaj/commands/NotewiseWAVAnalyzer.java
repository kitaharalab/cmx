package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.commands.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import java.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.xml.sax.*;


public abstract class NotewiseWAVAnalyzer 
  extends AbstractWAVAnalyzer {

  private String sccFileName = null;
  private SCCXMLWrapper scc;

  protected boolean setOptionsLocal(String option, String value) {
    if (super.setOptionsLocal(option, value)) {
      return true;
    } else if (option.equals("-scc")) {
      sccFileName = value;
      return true;
    } else {
      return false;
    }
  }

  protected void preproc() throws IOException, ParserConfigurationException, 
    TransformerException, SAXException {
    super.preproc();
    scc = (SCCXMLWrapper)CMXFileWrapper.readfile(sccFileName);
  }
  
  protected AmusaDataSetCompatible run(WAVWrapper wav)
    throws IOException,ParserConfigurationException,
    TransformerException, SAXException {
    AmusaDataSet dataset = new AmusaDataSet(getAmusaXMLFormat());
    Part[] partlist = scc.getPartList();
    for (Part part : partlist) {
      Note[] notelist = part.getNoteOnlyList();
      for (Note note : notelist) {
        SPExecutor ex = new SPExecutor();
        winslider = new WindowSlider(usesStereo());
        winslider.setInputData(wav, note.onsetInMilliSec(), 
                               note.offsetInMilliSec());
        ex.addSPModule(winslider);
        for (ProducerConsumerCompatible module : getUsedModules())
          ex.addSPModule(module);
        for (ProducerConsumerCompatible module : getUsedModules(note))
          ex.addSPModule(module);
        for (ModuleConnection mc : getModuleConnections())
          ex.connect(mc.inModule, mc.inCh, mc.outModule, mc.outCh);
        for (OutputData outdata : getOutputData()) {
          TimeSeriesCompatible ts 
            = ex.getResult(outdata.module).get(outdata.ch);
          ts.setAttribute("onset", note.onsetInMilliSec());
          ts.setAttribute("offset", note.offsetInMilliSec());
          ts.setAttribute("notenum", note.notenum());
          ts.setAttribute("velocity", note.velocity());
          ts.setAttribute("prognum", note.part().prognum());
          ts.setAttribute("xlink:href", 
                            "#xpointer(" + 
                            note.getXPathExpression() + ")");
//          ts.setAttributeNS("http://www.w3.org/1999/xlink", 
//                            "xlink:href", 
//                            "#xpointer(" + 
//                            note.getXPathExpression() + ")");
          dataset.add(ts);
        }
        customSetting(ex, dataset);
        customSetting(ex, note, dataset);
        Thread thCurrent = Thread.currentThread();
        ex.setInterruptionReceiver(thCurrent);
        ex.start();
        System.err.println(note.toString() + " being processed...");
        try {
          while (true)
            thCurrent.sleep(1000);
        } catch(InterruptedException e) {}
      }
    }
    return dataset;
  }

  protected final ProducerConsumerCompatible[] getUsedModules() {
    return new ProducerConsumerCompatible[] {};
  }

  protected abstract ProducerConsumerCompatible[] 
  getUsedModules(Note note);
  
  protected void customSetting(SPExecutor ex, Note note, 
                               AmusaDataSetCompatible dataset) {
    // do nothing
  }
}