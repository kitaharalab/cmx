package jp.crestmuse.cmx.sp;

public abstract class STFTFactory {
  public static final STFTFactory getFactory()
    throws ClassNotFoundException, InstantiationException,
    IllegalAccessException {
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
  }

  public abstract STFT createSTFT();
}
