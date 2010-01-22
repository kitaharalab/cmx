package jp.crestmuse.cmx.math;

abstract class AbstractDoubleArrayImpl implements DoubleArray,Cloneable {

  public Object clone() throws CloneNotSupportedException {
      DoubleArray newarray = Utils.createDoubleArray(length());
      for (int i = 0; i < length(); i++)
	  newarray.set(i, get(i));
      return newarray;
  }

  public double[] toArray() {
      double[] newarray = new double[length()];
      for (int i = 0; i < length(); i++)
	  newarray[i] = get(i);
      return newarray;
  }

  public String encode() {
    return Utils.toString2(this);
  }

  public DoubleArray subarrayX(int from, int thru) {
      try {
	  DoubleArray newarray = (DoubleArray)clone();
      return newarray.subarrayX(from, thru);
      } catch (CloneNotSupportedException e) {
	  throw new RuntimeException();
      }
  }
    
}
