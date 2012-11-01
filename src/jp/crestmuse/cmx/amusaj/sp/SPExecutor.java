package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;
import java.util.concurrent.*;

/************************************************************************
 *<p>
 * Producer-Consumerパターンに基づいて設計されたデータ処理モジュールを登録し, モジュール同士を接続し, モジュールネットワークを実行し,
 * 実行結果を取得するという 一連の処理を行うためのクラスです.
 * </p>
 * 
 *<p>
 * Amusa (API for Musical Scene Analysis)では, 音響信号などの時系列データの
 * 処理をProducer-Consumerパターンに基づいたデータ処理モジュールのネットワークとして 構成します.
 * 各モジュールは時系列データから要素を1つ受け取って何らかの処理を行い, 処理結果を別の時系列データ構造に投げます. そのモジュールの後段に接続されている
 * モジュールがそれを受け取ってさらなる処理を行います. 時系列データのデータ構造は First-in-first-out (FIFO)を前提とします.
 * データ処理モジュールは ProducerConsumerCompatibleインターフェースを, 時系列データは
 * TimeSeriesCompatibleインターフェースを実装している必要があります. 詳しくはそれぞれのインターフェースのドキュメントをご覧ください.
 * </p>
 ************************************************************************/
public class SPExecutor {
  // private List<SPThread> list;
  // private List<SPThread> list;
  // private SPThread lastThread = null;
  private List<SPExecutorModule> modules;
  private Map<ProducerConsumerCompatible, SPExecutorModule> map;
//  private Map<String, String> params;
  // private int nFrames;
//  private int timeunit;
  private long sleepTime = 0;

  volatile private int nFinished = 0;

//  static Executor ex = null;

  private Thread intReceiver = null;


  public SPExecutor() {
    modules = new LinkedList<SPExecutorModule>();
    map = new HashMap<ProducerConsumerCompatible, SPExecutorModule>();
//    if (ex == null)
//    ex = new ThreadPoolExecutor(16, 4096, 100, TimeUnit.MILLISECONDS, 
//                                        new LinkedBlockingQueue<Runnable>());
  }

/*
  public SPExecutor() {
    this(null);
  }
*/

//  public SPExecutor(Map<String, String> params) {
//    this(params, 1);
//  }

/*
  @Deprecated
  public SPExecutor(Map<String, String> params, int timeunit) {
    // list = new LinkedList<SPThread>();
    // list = new ArrayList<SPModule>();
    modules = new LinkedList<SPExecutorModule>();
    map = new HashMap<ProducerConsumerCompatible, SPExecutorModule>();
    this.params = params;
    // this.nFrames = nFrames;
    this.timeunit = timeunit;
  }
*/

  /*********************************************************************
   *データ処理モジュールオブジェクトを登録します.
   *********************************************************************/
  public void addSPModule(ProducerConsumerCompatible module) {
    // if (lastThread == null)
    // list.add(lastThread = new SPThread());
//    module.setParams(params);
    SPExecutorModule spm = new SPExecutorModule(module);
    /*
    spm.module = module;
    int n = module.getOutputChannels();
    for (int i = 0; i < n; i++)
      spm.dest.add(new MutableTimeSeries<SPElement>());
    //       spm.dest.add(module.createOutputInstance(timeunit));
    n = module.getInputChannels();
    for (int i = 0; i < n; i++)
      spm.src.add(null);
    */
    // lastThread.modules.add(spm);
    //    if (modules.contains(spm)) {
    //	System.err.println(module + "has already been added. Addition was skipped.");
    //    } else {
	modules.add(spm);
	map.put(module, spm);
	//    }
  }

  /*
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
  */
  /*
    public void newThread() {
      lastThread = null;
    }
  */
  /*********************************************************************
   *指定されたデータ処理モジュールAの指定されたチャンネルch1の出力を,
   * 指定されたデータ処理モジュールBの指定されたチャンネルch2の入力に接続します. チャンネルの概念については,
   * ProducerConsumerCompatibleインターフェースの ドキュメントをご覧ください.
   *********************************************************************/
  public void connect(ProducerConsumerCompatible output, int ch1,
      ProducerConsumerCompatible input, int ch2) {
    try {
      output.getOutputClasses()[ch1].asSubclass
        (input.getInputClasses()[ch2]);
    } catch (ClassCastException e) {
      try {
        input.getInputClasses()[ch2].asSubclass
          (output.getOutputClasses()[ch1]);
      } catch (ClassCastException e2) {
        throw new SPIllegalConnectionException
          ("can't connect " + output.getOutputClasses()[ch1].getName() 
           + " and " + input.getInputClasses()[ch2].getName());
      }
    }
//    if (output.getOutputClasses()[ch1] != input.getInputClasses()[ch2])
//      throw new SPIllegalConnectionException("can't connect "
//          + output.getOutputClasses()[ch1].getName() + " and "
//          + input.getInputClasses()[ch2].getName());
    SPExecutorModule spm1 = map.get(output);
    SPExecutorModule spm2 = map.get(input);
    /*
    System.err.println(spm1);
    System.err.println(spm1.dest);
    System.err.println(spm1.dest.get(ch1));
    //    System.err.println(spm1.dest.get(ch1).getQueueReader());
    System.err.println(spm2);
    System.err.println(spm2.src);
    spm2.src.set(ch2, spm1.dest.get(ch1).getQueueReader());
    */
    spm2.src[ch2] = spm1.dest[ch1].getQueueReader();
  }

  /*********************************************************************
   *登録されたデータ処理モジュールの実行を開始します.
   ********************************************************************/
  public void start() {
    // for (SPThread th : list)
    // th.start();
////    Executor ex = new ScheduledThreadPoolExecutor(16);
//    Executor ex = new ThreadPoolExecutor(4, modules.size(), 100, TimeUnit.MILLISECONDS, 
//                                        new LinkedBlockingQueue<Runnable>());
//    for (SPExecutorModule spm : modules)
//      ex.execute(spm);
    for (SPExecutorModule spm : modules)
      spm.start();
  }

  /*
    public void startSingleThread() {
      loop:
      for (int i = 0; i < nFrames || nFrames <= 0; i++) {
        for (SPModule m : list)
          try { 
        System.err.print(".");
            m.module.execute(m.src, m.dest);
          } catch (InterruptedException e) {
             break loop;
          }
      }
    }
  */

  public void stop() {
    // for(SPThread th : list)
    // th.interrupt();
    for (SPExecutorModule spm : modules)
      spm.interrupt();
  }

  public boolean finished() {
    // for(SPThread th : list)
    // if(!th.finish) return false;
    for (SPExecutorModule spm : modules)
      if (spm.isAlive())
        return false;
    return true;
  }

  public boolean finished(ProducerConsumerCompatible module) {
    // return map.get(module).finish;
    return !map.get(module).isAlive();
  }

  private synchronized void checkFinished() {
    if (intReceiver != null && nFinished >= modules.size())
      intReceiver.interrupt();      
  }

  public void setInterruptionReceiver(Thread th) {
    intReceiver = th;
  }

  /*********************************************************************
   *指定されたデータ処理モジュールの全チャンネルの出力を返します.
   *********************************************************************/
  public List<TimeSeriesCompatible> getResult(
      ProducerConsumerCompatible module) {
    // return map.get(module).dest;
    return Arrays.asList(map.get(module).dest);
  }

//  public Map<String, String> getParams() {
//    return params;
//  }

  // public List<AmusaDataCompatible> getResult(int index) {
  // return list.get(index).dest;
  // }

  public void setSleepTime(long sleepTime) {
    this.sleepTime = sleepTime;
  }

  /*
    private class SPThread extends Thread {
      private List<SPModule> modules = new LinkedList<SPModule>();
      private boolean finish = false;
      public void run() {
        int nModules = modules.size();
        int nFinished = 0;
        for(SPModule m : modules)
          if(m.dest.length == 0) nFinished++;
        while (nFinished < nModules) {
          try {
            for (SPModule m : modules) {
              if (!m.finish) {
                for(int i=0; i<m.inputElements.length; i++) {
                  m.inputElements[i] = m.src[i].take();
                }
                if(m.inputElements.length > 0 && m.inputElements[0] instanceof SPTerminator) {
                  m.finish = true;
                  nFinished++;
                  for(TimeSeriesCompatible<SPElement> tsc : m.dest)
                    tsc.add(new SPTerminator());
                }else {
                  m.module.execute(m.inputElements, m.dest);
                }
              }
            }
            if (sleepTime > 0)
              sleep(sleepTime);
            if (Thread.interrupted()) break;
          } catch (InterruptedException e) {
            break;
          }
        }
        for (SPModule m : modules)
          m.module.stop(m.src, m.dest);
        finish = true;
      }
    }
  */
  private class SPExecutorModule extends Thread {
    ProducerConsumerCompatible module;
    /*
    List<QueueReader<? extends SPElement>> src 
    = new ArrayList<QueueReader<? extends SPElement>>();
    List<TimeSeriesCompatible<? extends SPElement>> dest 
    = new ArrayList<TimeSeriesCompatible<? extends SPElement>>();
    */
    QueueReader[] src;
    TimeSeriesCompatible[] dest;
    Object[] inputElements;
    // boolean finish = false;
    int inputChannelNum;
    int outputChannelNum;

    SPExecutorModule(ProducerConsumerCompatible pcc) {
      inputChannelNum = pcc.getInputClasses().length;
      outputChannelNum = pcc.getOutputClasses().length;
      module = pcc;
      src = new QueueReader[inputChannelNum];
      dest = new TimeSeriesCompatible[outputChannelNum];
      inputElements = new Object[inputChannelNum];
      for (int i = 0; i < outputChannelNum; i++)
        dest[i] = new MutableTimeSeries();
    }

    public void run() {
      while (!Thread.interrupted()) {
        try {
          for (int i = 0; i < inputChannelNum; i++) {
            inputElements[i] = src[i].take();
          }
          if (inputChannelNum > 0 && inputElements[0] instanceof SPTerminator) {
            module.terminated(dest);
            for (TimeSeriesCompatible tsc : dest)     
		tsc.add(SPTerminator.getInstance());
            break;
          }
          module.execute(inputElements, dest);
          if(outputChannelNum > 0 && dest[0].isComplete())
            break;
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          e.printStackTrace();
          break;
        }
      }
      module.stop();
      nFinished++;
      checkFinished();
    }
  }

  /*
    private class SPModule extends Thread {
      ProducerConsumerCompatible module;
      List<QueueReader> src = new ArrayList<QueueReader>();
      List<TimeSeriesCompatible> dest = new ArrayList<TimeSeriesCompatible>();
      boolean finish = false;
      public void run() {
        for(int i=0; i<nFrames || nFrames<=0; i++){
          try {
            module.execute(src, dest);
            if (sleepTime > 0)
              sleep(sleepTime);
            if(Thread.interrupted()) break;
          } catch (InterruptedException e) {
            break;
          }
        }
        finish = true;
      }
    }
  */
}
