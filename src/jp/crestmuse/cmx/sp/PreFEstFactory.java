package jp.crestmuse.cmx.sp;

public abstract class PreFEstFactory extends SPFactory {
  public static PreFEstFactory getFactory() {
    return (PreFEstFactory)getFactory("prefestFactory", "PreFEstFactoryImpl");
  }

  public abstract PreFEst createPreFEst(double from, double thru, 
                                        double step);
}
