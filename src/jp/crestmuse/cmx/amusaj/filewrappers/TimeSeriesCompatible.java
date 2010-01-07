package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

/*********************************************************************
 *時系列データを表すクラスのためのインターフェイスです. 
 *ここで時系列データとは, 各フレームのデータが多次元ベクトルであり, 
 *一定のサンプリングレートでデータが並んでいるものです. 
 *時系列データへのアクセスはFirst-in First-outとします. 
 *********************************************************************/
public interface TimeSeriesCompatible<D>
//  extends AmusaDataCompatible<D> 
{

  public QueueReader<D> getQueueReader();
  /*******************************************************************
   *多次元ベクトルの次元数を返します. 
   *******************************************************************/
  public int dim();
//  public int frames();
//  public int bytesize();
  /*******************************************************************
   *時間分解能をミリ秒単位で返します. 
   *******************************************************************/
//  public int timeunit();
  String getAttribute(String key);
  int getAttributeInt(String key);
  double getAttributeDouble(String key);
  void setAttribute(String key, String value);
  void setAttribute(String key, int value);
  void setAttribute(String key, double value);
//  void setAttributeNS(String namespaceURI, String key, String value);
  Iterator<Map.Entry<String,String>> getAttributeIterator();

  boolean isComplete();

  /*******************************************************************
   *新たな多次元ベクトルを末尾に追加します. 
   *******************************************************************/
  public void add(D d) throws InterruptedException;
}
