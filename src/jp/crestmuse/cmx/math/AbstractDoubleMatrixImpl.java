package jp.crestmuse.cmx.math;

public abstract class AbstractDoubleMatrixImpl implements DoubleMatrix,Cloneable {

  public Object clone() throws CloneNotSupportedException {
      DoubleMatrix newmatrix = Utils.createDoubleMatrix(nrows(), ncols());
      for (int i = 0; i < nrows(); i++)
	  for (int j = 0; j < ncols(); j++)
	      newmatrix.set(i, j, get(i, j));
      return newmatrix;
  }

  public String encode() {
    return Utils.toString2(this);
  }

}
