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
  private List<SPModule> list;
  private Map<ProducerConsumerCompatible,SPModule> map;
  private Map<String,Object> params;
  int nFrames;
  int timeunit;

  /**
   * nFramesに0以下の値を指定すると、startを呼び出した際、stopを呼び出すまで処理を続けます．
   * @param params
   * @param nFrames
   * @param timeunit
   */
  public SPExecutor(Map params, int nFrames, int timeunit) {
    list = new ArrayList<SPModule>();
    map = new HashMap<ProducerConsumerCompatible,SPModule>();
    this.params = params;
    this.nFrames = nFrames;
    this.timeunit = timeunit;
  }

  /*********************************************************************
   *データ処理モジュールオブジェクトを登録します. 
   *********************************************************************/
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

  /*********************************************************************
   *指定されたデータ処理モジュールAの指定されたチャンネルch1の出力を, 
   *指定されたデータ処理モジュールBの指定されたチャンネルch2の入力に接続します. 
   *チャンネルの概念については, ProducerConsumerCompatibleインターフェースの
   *ドキュメントをご覧ください. 
   *********************************************************************/
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

  /*********************************************************************
   *登録されたデータ処理モジュールの実行を開始します. 
   ********************************************************************/
  public void start() {
    for (final SPModule m : list){
      m.thread = new Thread(){
        public void run() {
          for(int i=0; i<nFrames || nFrames<=0; i++){
            try {
              m.module.execute(m.src, m.dest);
              if(Thread.interrupted()) break;
            } catch (InterruptedException e) {
              break;
            }
          }
        }
      };
      m.thread.start();
    }
  }
  
  public void stop(){
    for(SPModule m : list)
      if(list != null) m.thread.interrupt();
  }

  /*********************************************************************
   *指定されたデータ処理モジュールの全チャンネルの出力を返します. 
   *********************************************************************/
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
    Thread thread = null;
  }
}
