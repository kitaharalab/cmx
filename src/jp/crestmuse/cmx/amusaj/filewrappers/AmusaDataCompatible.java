package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.misc.*;
import java.util.*;

public interface AmusaDataCompatible<D> {
  QueueReader<D> getQueueReader();
  int frames();
  String getAttribute(String key);
  int getAttributeInt(String key);
  double getAttributeDouble(String key);
  void setAttribute(String key, String value);
  void setAttribute(String key, int value);
  void setAttribute(String key, double value);
  Iterator<Map.Entry<String,String>> getAttributeIterator();
}