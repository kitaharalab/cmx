package jp.crestmuse.cmx.math;

public class DoubleArrayTest {
  public static void main(String[] args) {
    try {
      DoubleArrayFactory factory = DoubleArrayFactory.getFactory();
//      DoubleArrayFactory factory = new DoubleArrayFactory();
      DoubleArray array = factory.createArray(2);
      array.set(0, 1.0);
      array.set(1, -1.0);
      for (int i = 0; i < array.length(); i++)
        System.out.println(array.get(i));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}