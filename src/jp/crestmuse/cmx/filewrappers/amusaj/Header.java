package jp.crestmuse.cmx.filewrappers.amusaj;
import jp.crestmuse.cmx.filewrappers.*;
import org.w3c.dom.*;

public class Header extends AbstractHeaderNodeInterface {
  Header(Node node) {
    super(node);
  }

  protected String getSupportedNodeName() {
    return "header";
  }
}