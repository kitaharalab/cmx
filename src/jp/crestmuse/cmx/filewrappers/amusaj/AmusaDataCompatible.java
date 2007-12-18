package jp.crestmuse.cmx.filewrappers.amusaj;
import java.util.*;

public interface AmusaDataCompatible {
  String getAttribute(String key);
  int getAttributeInt(String key);
  double getAttributeDouble(String key);
  void setAttribute(String key, String value);
  void setAttribute(String key, int value);
  void setAttribute(String key, double value);
  Iterator<Map.Entry<String,String>> getAttributeIterator();
}