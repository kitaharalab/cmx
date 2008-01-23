package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

/*********************************************************************
 *音楽情景分析用API「AMUSA-J」における共通のデータ構造へのインターフェイスを定義します. 
 *データ構造は, 基本的になんらかのデータが時系列的に並んでいるものとします. 
 *データへのアクセスはFirst-in First-outを基本とします. 
 *それとは別に属性を持つことができます. 
 *********************************************************************/
public interface AmusaDataCompatible<D> {
  /*********************************************************************
   *このオブジェクトに格納されているデータへアクセスするためのキューリーダを返します. 
   *********************************************************************/
  QueueReader<D> getQueueReader();
  /*********************************************************************
   *フレーム数を返します. 
   *********************************************************************/
  int frames();
  String getAttribute(String key);
  int getAttributeInt(String key);
  double getAttributeDouble(String key);
  void setAttribute(String key, String value);
  void setAttribute(String key, int value);
  void setAttribute(String key, double value);
  Iterator<Map.Entry<String,String>> getAttributeIterator();
}