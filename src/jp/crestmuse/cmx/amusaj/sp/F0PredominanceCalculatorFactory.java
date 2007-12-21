package jp.crestmuse.cmx.amusaj.sp;

public abstract class F0PredominanceCalculatorFactory extends SPFactory {
  public static F0PredominanceCalculatorFactory getFactory() {
    return (F0PredominanceCalculatorFactory)
      getFactory("f0calcFactory", "F0PredominanceCalculatorFactoryImpl");
  }

  public abstract F0PredominanceCalculator 
  createCalculator(double from, double thru, double step);
}
