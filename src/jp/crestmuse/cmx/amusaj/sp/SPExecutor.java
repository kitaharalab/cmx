package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

import java.util.*;

/************************************************************************
 *<p>Producer-Consumerパターンに基づいて設計されたデータ処理モジュールを登録し, 
 *モジュール同士を接続し, モジュールネットワークを実行し, 実行結果を取得するという
 *一連の処理を行うためのクラスです. </p>
 *
 *<p>Amusa (API for Musical Scene Analysis)では, 音響信号などの時系列データの
 *処理をProducer-Consumerパターンに基づいたデータ処理モジュールのネットワークとして
 *構成します. 各モジュールは時系列データから要素を1つ受け取って何らかの処理を行い, 
 *処理結果を別の時系列データ構造に投げます. そのモジュールの後段に接続されている
 *モジュールがそれを受け取ってさらなる処理を行います. 時系列データのデータ構造は
 *First-in-first-out (FIFO)を前提とします. データ処理モジュールは
 *ProducerConsumerCompatibleインターフェースを, 時系列データは
 *TimeSeriesCompatibleインターフェースを実装している必要があります. 
 *詳しくはそれぞれのインターフェースのドキュメントをご覧ください. </p>
 ************************************************************************/
public class SPExecutor {
//  private List<SPThread> list;
  private List<SPThread> list;
  private SPThread lastThread = null;
  private Map<ProducerConsumerCompatible,SPModule> map;
  private Map<String,String> params;
//  private int nFrames;
  private int timeunit;
  private long sleepTime = 0;

  /**
   * nFramesに0以下の値を指定すると、startを呼び出した際、stopを呼び出すまで処理を続けます．
   * @param params
   * @param nFrames
   * @param timeunit
   */
  public SPExecutor(Map<String,String> params, int timeunit) {
    list = new LinkedList<SPThread>();
//    list = new ArrayList<SPModule>();
    map = new HashMap<ProducerConsumerCompatible,SPModule>();
    this.params = params;
//    this.nFrames = nFrames;
    this.timeunit = timeunit;
  }

  /*********************************************************************
   *データ処理モジュールオブジェクトを登録します. 
   *********************************************************************/
   public void addSPModule(ProducerConsumerCompatible module) {
     if (lastThread == null)
       list.add(lastThread = new SPThread());
     module.setParams(params);
     SPModule spm = new SPModule();
     spm.module = module;
     int n = module.getOutputChannels();
     for (int i = 0; i < n; i++)
       spm.dest.add(new MutableTimeSeries());
//       spm.dest.add(module.createOutputInstance(timeunit));
     n = module.getInputChannels();
     for (int i = 0; i < n; i++)
       spm.src.add(null);
     lastThread.modules.add(spm);
     map.put(module, spm);
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

  public void newThread() {
    lastThread = null;
  }

  /*********************************************************************
   *指定されたデータ処理モジュールAの指定されたチャンネルch1の出力を, 
   *指定されたデータ処理モジュールBの指定されたチャンネルch2の入力に接続します. 
   *チャンネルの概念については, ProducerConsumerCompatibleインターフェースの
   *ドキュメントをご覧ください. 
   *********************************************************************/
  public void connect
  (ProducerConsumerCompatible output, int ch1, 
   ProducerConsumerCompatible input, int ch2) {
    SPModule spm1 = map.get(output);
    SPModule spm2 = map.get(input);
    System.err.println(spm1);
    System.err.println(spm1.dest);
    System.err.println(spm1.dest.get(ch1));
//    System.err.println(spm1.dest.get(ch1).getQueueReader());
    System.err.println(spm2);
    System.err.println(spm2.src);
    spm2.src.set(ch2, spm1.dest.get(ch1).getQueueReader());
  }

  /*********************************************************************
   *登録されたデータ処理モジュールの実行を開始します. 
   ********************************************************************/
  public void start() {
    for (SPThread th : list)
      th.start();
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
  
  public void stop(){
    for(SPThread th : list)
      th.interrupt();
  }
  
  public boolean finished(){
    for(SPThread th : list)
      if(!th.finish) return false;
    return true;
  }
  
  public boolean finished(ProducerConsumerCompatible module){
    return map.get(module).finish;
  }
  /*********************************************************************
   *指定されたデータ処理モジュールの全チャンネルの出力を返します. 
   *********************************************************************/
  public List<TimeSeriesCompatible<? extends SPElement>>
  getResult(ProducerConsumerCompatible module) {
    return map.get(module).dest;
  }

  public Map<String,String> getParams() {
    return params;
  }

//  public List<AmusaDataCompatible> getResult(int index) {
//    return list.get(index).dest;
//  }

  public void setSleepTime(long sleepTime) {
    this.sleepTime = sleepTime;
  }

  private class SPThread extends Thread {
    private List<SPModule> modules = new LinkedList<SPModule>();
    private boolean finish = false;
    public void run() {
      int nModules = modules.size();
      int nFinished = 0;
      while (nFinished < nModules) {
        try {
          for (SPModule m : modules) {
            if (!m.finish) {
//              if (!m.src.get(0).peek().hasNext()) {
//                m.finish = true;
//                nFinished++;
//              }
              m.module.execute(m.src, m.dest);
              if (m.dest.get(0).isComplete()) {
                System.err.println("finished: " + m.module);
                m.finish = true;
                nFinished++;
              }
            }
          }
          if (sleepTime > 0)
            sleep(sleepTime);
          if (Thread.interrupted()) break;
        } catch (InterruptedException e) {
          break;
        }
        for (SPModule m : modules)
          m.module.stop(m.src, m.dest);
      }
//      for (SPModule m : modules)
//        m.finish = true;
      finish = true;
    }
  }

  private class SPModule {
    ProducerConsumerCompatible module;
    List<QueueReader<? extends SPElement>> src 
    = new ArrayList<QueueReader<? extends SPElement>>();
    List<TimeSeriesCompatible<? extends SPElement>> dest 
    = new ArrayList<TimeSeriesCompatible<? extends SPElement>>();
    boolean finish = false;
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
