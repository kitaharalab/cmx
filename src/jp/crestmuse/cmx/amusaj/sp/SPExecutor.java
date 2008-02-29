package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public class SPExecutor {
  private List<SPModule> list;
  private Map<ProducerConsumerCompatible,SPModule> map;
  private Map<String,Object> params;
  int nFrames;
  int timeunit;

  public SPExecutor(Map params, int nFrames, int timeunit) {
    list = new ArrayList<SPModule>();
    map = new HashMap<ProducerConsumerCompatible,SPModule>();
    this.params = params;
    this.nFrames = nFrames;
    this.timeunit = timeunit;
  }

  public void addSPModule(ProducerConsumerCompatible module) {
    module.setParams(params);
    SPModule spm = new SPModule();
    spm.module = module;
    int n = module.getOutputChannels();
    for (int i = 0; i < n; i++)
      spm.dest.add(module.createOutputInstance(nFrames, timeunit));
    n = module.getInputChannels();
    for (int i = 0; i < n; i++)
      spm.src.add(null);
    list.add(spm);
    map.put(module, spm);
  }

  public void connect(ProducerConsumerCompatible output, int ch1, 
                      ProducerConsumerCompatible input, int ch2) {
    SPModule spm1 = map.get(output);
    SPModule spm2 = map.get(input);
    System.err.println(spm1);
    System.err.println(spm1.dest);
    System.err.println(spm1.dest.get(ch1));
    System.err.println(spm1.dest.get(ch1).getQueueReader());
    System.err.println(spm2);
    System.err.println(spm2.src);
    spm2.src.set(ch2, spm1.dest.get(ch1).getQueueReader());
  }

  public void start() throws InterruptedException {
    for (int t = 0; t < nFrames; t++)
      for (SPModule m : list)
        m.module.execute(m.src, m.dest);
  }

  public List<TimeSeriesCompatible> 
  getResult(ProducerConsumerCompatible module) {
    return map.get(module).dest;
  }

//  public List<AmusaDataCompatible> getResult(int index) {
//    return list.get(index).dest;
//  }

  private class SPModule {
    ProducerConsumerCompatible module;
    List<QueueReader> src = new ArrayList<QueueReader>();
    List<TimeSeriesCompatible> dest = new ArrayList<TimeSeriesCompatible>();
  }
}
