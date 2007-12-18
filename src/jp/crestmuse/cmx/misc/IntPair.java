package jp.crestmuse.cmx.misc;

public class IntPair {
  private int a, b;
  public IntPair(int a, int b) {
    this.a = a;
    this.b = b;
  }
  public boolean equals(Object o) {
    IntPair anotherIntPair = (IntPair)o;
    return (a == anotherIntPair.a) && (b == anotherIntPair.b);
  }
  public int hashCode() {
    return a + b;
  }
}
