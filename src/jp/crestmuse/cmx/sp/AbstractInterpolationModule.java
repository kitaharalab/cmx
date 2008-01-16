package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.math.*;

abstract class AbstractInterpolationModule {

  private int idxFrom;
  private int idxThru;

  abstract boolean isMissing(double x);
  abstract void calcCoefficient(int leftindex, double leftvalue, 
                                int rightindex, double rightvalue);
  abstract double calcValue(int index, int leftindex, double leftvalue, 
                            int rightindex, double rightvalue);

  synchronized void run(DoubleArray x, int maxlength) {
    run(x, 0, x.length(), maxlength);
  }
    
  synchronized void run(DoubleArray x, int leftbound, int rightbound, 
                               int maxlength) {
    int i = leftbound;
    while (true) {
      findDataToBeInterpolated(x, i, rightbound);
      if (idxThru == -1) break;
      if (idxFrom >= 0 && idxThru >= 0 && idxThru - idxFrom <= maxlength)
        interpolate(x);
      i = idxThru;
    }
  }

  private void interpolate(DoubleArray x) {
    int leftindex = idxFrom - 1;
    int rightindex = idxThru;
    double leftvalue = x.get(leftindex);
    double rightvalue = x.get(rightindex);
    calcCoefficient(leftindex, leftvalue, rightindex, rightvalue);
    for (int i = idxFrom; i < idxThru; i++)
      x.set(i, calcValue(i, leftindex, leftvalue, rightindex, rightvalue));
  }

  private void findDataToBeInterpolated(DoubleArray x, int leftbound, 
                                        int rightbound) {
    this.idxFrom = -1;
    this.idxThru = -1;
    for (int i = leftbound + 1; i < rightbound; i++) {
      if (!isMissing(x.get(i-1)) && isMissing(x.get(i))) {
        this.idxFrom = i;
      } else if (isMissing(x.get(i-1)) && !isMissing(x.get(i))) {
        this.idxThru = i;
        break;
      }
    }
  }
  
}