package jp.crestmuse.cmx.amusaj.sp;

public abstract class F0PDFCalculatorFactory extends SPFactory {
  public static F0PDFCalculatorFactory getFactory() {
    return (F0PDFCalculatorFactory)
      getFactory("f0calcFactory", "F0PredominanceCalculatorFactoryImpl");
  }

  public abstract F0PDFCalculator 
  createCalculator(double from, double thru, double step);
}
