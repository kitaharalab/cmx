package jp.crestmuse.cmx.sp;

public abstract class STFTFactory extends SPFactory {

  public static STFTFactory getFactory() {
    return (STFTFactory)getFactory("stftFactory", "STFTFactoryImpl");
  }

/*  public static final STFTFactory getFactory() {
    try {
      Class c;
      String className = System.getProperty("stftFactory");
      if (className == null) {
        try {
          c = Class.forName("jp.crestmuse.cmx.sp.STFTFactoryImpl");
        } catch (ClassNotFoundException e) {
          c = Class.forName("STFTFactoryImpl");
        }
      } else {
        c = Class.forName(className);
      }
      return (STFTFactory)c.newInstance();
    } catch (ClassNotFoundException e) {
      throw new STFTFactoryException(e.toString());
    } catch (InstantiationException e) {
      throw new STFTFactoryException(e.toString());
    } catch (IllegalAccessException e) {
      throw new STFTFactoryException(e.toString());
    }
  }
*/

  public abstract STFT createSTFT();
}
