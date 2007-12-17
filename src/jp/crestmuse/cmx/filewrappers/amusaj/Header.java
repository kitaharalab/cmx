package jp.crestmuse.cmx.filewrappers.amusaj;
import jp.crestmuse.cmx.filewrappers.*;
import org.w3c.dom.*;
import java.util.*;

public class Header extends NodeInterface {
  NodeList nodelist;
  int size;
  Map<String,String> map;
  Header(Node node) {
    super(node);
    nodelist = getChildNodes();
    size = nodelist.getLength();
    map = new HashMap<String,String>();
  }

  protected String getSupportedNodeName() {
    return "header";
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

  public static void addHeaderElementToWrapper(String name, String content, 
                                               CMXFileWrapper wrapper) {
    wrapper.addChild("meta");
    wrapper.setAttribute("name", name);
    wrapper.setAttribute("content", content);
    wrapper.returnToParent();
  }

}