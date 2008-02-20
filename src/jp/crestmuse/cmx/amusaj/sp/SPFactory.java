package jp.crestmuse.cmx.amusaj.sp;

abstract class SPFactory {

  static SPFactory getFactory(String property, String defaultClass) {
    try {
      Class c;
      String className = System.getProperty(property);
      if (className == null) {
        try {
          c = Class.forName("jp.crestmuse.cmx.amusaj.sp." + defaultClass);
        } catch (ClassNotFoundException e) {
          c = Class.forName(defaultClass);
        }
      } else {
        c = Class.forName(className);
      }
      return (SPFactory)c.newInstance();
    } catch (ClassNotFoundException e) {
      throw new SPFactoryException(e.toString());
    } catch (InstantiationException e) {
      throw new SPFactoryException(e.toString());
    } catch (IllegalAccessException e) {
      throw new SPFactoryException(e.toString());
    }
  }
}
