package jp.crestmuse.cmx.amusaj.sp;

import java.util.LinkedList;

import jp.crestmuse.cmx.amusaj.filewrappers.MutableTimeSeries;
import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;

public class SPGroup extends SPModule {

  private Class<SPElement>[] inputClasses;
  private Class<SPElement>[] outputClasses;
  private LinkedList<SPGroupModule> modules = new LinkedList<SPGroupModule>();
  TimeSeriesCompatible<SPElement>[] headDest;
  QueueReader<SPElement>[] tailSrc;

  public void execute(SPElement[] src, TimeSeriesCompatible<SPElement>[] dest)
      throws InterruptedException {
    for(int i=0; i<src.length; i++)
      headDest[i].add(src[i]);
    for(SPGroupModule spm : modules)
      spm.run();
    if(tailSrc == null) {
      tailSrc = new QueueReader[outputClasses.length];
      for(int i=0; i<outputClasses.length; i++) {
        tailSrc[i] = modules.getLast().dest[i].getQueueReader();
      }
    }
    for(int i=0; i<outputClasses.length; i++) {
      dest[i].add(tailSrc[i].take());
    }
  }

  public Class<SPElement>[] getInputClasses() {
    return inputClasses;
  }

  public Class<SPElement>[] getOutputClasses() {
    return outputClasses;
  }

  public void addModule(ProducerConsumerCompatible module) {
    SPGroupModule addSpm = new SPGroupModule(module);
    if (modules.size() == 0) {
      inputClasses = module.getInputClasses();
      headDest = new TimeSeriesCompatible[inputClasses.length];
      for(int i=0; i<inputClasses.length; i++) {
        headDest[i] = new MutableTimeSeries<SPElement>();
        addSpm.src[i] = headDest[i].getQueueReader();
      }
    } else {
      SPGroupModule lastSpm = modules.getLast();
      if (lastSpm.outputChannelNum != addSpm.inputChannelNum) {
        throw new SPIllegalConnectionException("different channel length "
            + lastSpm.module.getClass().getName() + " and "
            + addSpm.module.getClass().getName());
      }
      for (int i = 0; i < lastSpm.outputChannelNum; i++) {
        if (lastSpm.module.getOutputClasses()[i] != module.getInputClasses()[i]) {
          throw new SPIllegalConnectionException("can't connect "
              + lastSpm.module.getOutputClasses()[i].getName() + " and "
              + module.getInputClasses()[i].getName());
        }
        addSpm.src[i] = lastSpm.dest[i].getQueueReader();
      }
    }
    modules.add(addSpm);
    outputClasses = module.getOutputClasses();
  }

  public void stop(QueueReader<SPElement>[] src,
      TimeSeriesCompatible<SPElement>[] dest) {
    for (SPGroupModule spm : modules)
      spm.stop();
  };

  private class SPGroupModule {
    ProducerConsumerCompatible module;
    QueueReader<SPElement>[] src;
    TimeSeriesCompatible<SPElement>[] dest;
    SPElement[] inputElements;
    int inputChannelNum;
    int outputChannelNum;

    SPGroupModule(ProducerConsumerCompatible pcc) {
      inputChannelNum = pcc.getInputClasses().length;
      outputChannelNum = pcc.getOutputClasses().length;
      module = pcc;
      src = new QueueReader[inputChannelNum];
      dest = new TimeSeriesCompatible[outputChannelNum];
      inputElements = new SPElement[inputChannelNum];
      for (int i = 0; i < outputChannelNum; i++)
        dest[i] = new MutableTimeSeries<SPElement>();
    }

    void run() {
      try {
        for (int i = 0; i < inputChannelNum; i++)
          inputElements[i] = src[i].take();
        if (inputChannelNum > 0 && inputElements[0] instanceof SPTerminator) {
          for (TimeSeriesCompatible<SPElement> tsc : dest)
            tsc.add(new SPTerminator());
          return;
        }
        module.execute(inputElements, dest);
//        if (outputChannelNum > 0 && dest[0].isComplete())
//          return;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    void stop() {
      module.stop(src, dest);
    }
  }

}
