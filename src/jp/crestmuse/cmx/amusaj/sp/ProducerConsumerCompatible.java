package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

/************************************************************************
 *<p>Producer-Consumerパターンに基づいたデータ処理モジュールのインターフェースです.
 *このインターフェースでは, 1つ以上のキューから要素を1つずつ取り出して, 
 *何らかの処理を行った後, 処理結果を別のキューに書き込みます. 
 *各モジュールには, 基本的には入力チャンネルと出力チャンネルが各々1つ以上あり, 
 *各チャンネルにキューが接続される形になります.  </p>
 ***********************************************************************/
public interface ProducerConsumerCompatible<D, E> {
  public void setParams(Map<String,Object> params);
//  public boolean setOptionsLocal(String option, String value);
  /**********************************************************************
   *ここに処理内容を記述します. 引数srcに全入力チャンネルに対するQueueReaderが
   *格納されているので, takeメソッドでそこから1つずつ要素を取り出します. 
   *@param src 全入力チャンネルに対するQueueReaderオブジェクト
   *@param dest 全出力チャンネル
   **********************************************************************/
  public void execute(List<QueueReader<D>> src, 
                      List<TimeSeriesCompatible<E>> dest) 
    throws InterruptedException;
//  public void execute(List<QueueReader<D>> src, 
//                      List<E> dest) throws InterruptedException;
  /**********************************************************************
   *入力チャンネル数を返します. 
   **********************************************************************/
  public int getInputChannels();
  /**********************************************************************
   *出力チャンネル数を返します 
   **********************************************************************/
  public int getOutputChannels();
  /**********************************************************************
   *
   **********************************************************************/
  public TimeSeriesCompatible<E> 
    createOutputInstance(int nFrames, int timeunit);
}
