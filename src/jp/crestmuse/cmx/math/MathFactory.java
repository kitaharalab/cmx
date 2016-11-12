package jp.crestmuse.cmx.math;

abstract class MathFactory {
  static MathFactory getFactory(String property, String defaultClass) {
    try {
      String className;
      try {
        className = System.getProperty(property);
        if (className == null)
          className = "jp.crestmuse.cmx.math." + defaultClass;
      } catch (java.security.AccessControlException e) {
        className = "jp.crestmuse.cmx.math." + defaultClass;
      }
      return (MathFactory)Class.forName(className).newInstance();
    } catch (ClassNotFoundException e) {
      throw new MathFactoryException(e.toString());
    } catch (InstantiationException e) {
      throw new MathFactoryException(e.toString());
    } catch (IllegalAccessException e) {
      throw new MathFactoryException(e.toString());
    }
  }
}