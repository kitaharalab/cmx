package jp.crestmuse.cmx.filewrappers;
import org.w3c.dom.*;
import java.util.*;

public abstract class AbstractHeaderNodeInterface extends NodeInterface {
  NodeList nodelist;
  int size;
  Map<String,String> map;
  protected AbstractHeaderNodeInterface(Node node) {
    super(node);
    nodelist = getChildNodes();
    size = nodelist.getLength();
    map = new HashMap<String,String>();
  }

  public String getHeaderElement(String name) {
    if (map.containsKey(name)) {
      return map.get(name);
    } else {
      for (int i = 0; i < size; i++) {
        if (getAttribute(nodelist.item(i), "name").equals(name)) {
          String content = getAttribute(nodelist.item(i), "content");
          map.put(name, content);
          return content;
        }
      }
      return null;
    }
  }

  public int getHeaderElementInt(String name) {
    String content = getHeaderElement(name);
    if (content == null)
      return -1;
    else
      return Integer.parseInt(content);
  }

  public double getHeaderElementDouble(String name) {
    String content = getHeaderElement(name);
    if (content == null)
      return Double.NaN;
    else
      return Double.parseDouble(content);
  }

  public boolean containsHeaderKey(String name) {
    if (map.containsKey(name)) {
      return true;
    } else {
      for (int i = 0; i < size; i++) {
        if (getAttribute(nodelist.item(i), "name").equals(name)) {
          String content = getAttribute(nodelist.item(i), "content");
          map.put(name, content);
          return true;
        }
      }
      return false;
    }
  }

}