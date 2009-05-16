package jp.crestmuse.cmx.math;

public interface BooleanArray extends Array {
    int length();
  boolean get(int index);
  void set(int index, boolean value);
}
