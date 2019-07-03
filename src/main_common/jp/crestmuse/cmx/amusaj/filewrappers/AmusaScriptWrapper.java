package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import org.w3c.dom.*;
import java.util.*;

class AmusaScriptWrapper extends CMXFileWrapper {

  private Params params;
  private Modules modules;
  private Connections connections;
  private Outputs outputs;

  protected void analyze() {
    params = new Params(selectSingleNode("/script/params"));
    modules = new Modules(selectSingleNode("/script/modules"));
    connections = new Connections(selectSingleNode("/script/connections"));
    outputs = new Outputs(selectSingleNode("/script/outputs"));
  }

  public String[] getParamNameList() {
    return params.getHeaderNameList();
  }

  public String getParam(String name) {
    return params.getHeaderElement(name);
  }

  public Map<String,String> getModules() {
    return modules.map;
  }

  public Connection[] getConnections() {
    return connections.list;
  }

  public Output[] getOutputs() {
    return outputs.outputs;
  }

  public String getOutputFormat() {
    return outputs.fmt;
  }

  private class Params extends AbstractHeaderNodeInterface {
    private Params(Node node) {
      super(node);
    }
    protected String getSupportedNodeName() {
      return "params";
    }
  }

  private class Modules extends NodeInterface {
    private Map<String,String> map;
    private Modules(Node node) {
      super(node);
      NodeList nodelist = getChildNodes();
      int size = nodelist.getLength();
      map = new LinkedHashMap<String,String>();
      for (int i = 0; i < size; i++) 
        map.put(getAttribute(nodelist.item(i), "name"), 
                getAttribute(nodelist.item(i), "object"));
    }
    protected String getSupportedNodeName() {
      return "modules";
    }
  }

  private class Connections extends NodeInterface {
    private Connection[] list;
    private Connections(Node node) {
      super(node);
      NodeList nodelist = getChildNodes();
      int size = nodelist.getLength();
      list = new Connection[size];
      for (int i = 0; i < size; i++) {
        Connection c = new Connection();
        c.objFrom = getAttribute(nodelist.item(i), "objFrom");
        c.objTo = getAttribute(nodelist.item(i), "objTo");
        c.chFrom = getAttributeInt(nodelist.item(i), "chFrom");
        c.chTo = getAttributeInt(nodelist.item(i), "chTo");
        list[i] = c;
      }
    }
    protected String getSupportedNodeName() {
      return "connections";
    }
  }

  public class Connection {
    public String objFrom, objTo;
    public int chFrom, chTo;
  }

  private class Outputs extends NodeInterface {
    private String fmt;
    private Output[] outputs;
    private Outputs(Node node) {
      super(node);
      fmt = getAttribute("format");
      NodeList nodelist = getChildNodes();
      int size = nodelist.getLength();
      outputs = new Output[size];
      for (int i = 0; i < size; i++) {
        Output o = new Output();
        o.object = getAttribute(nodelist.item(i), "object");
        o.ch = getAttributeInt(nodelist.item(i), "ch");
        outputs[i] = o;
      }
    }
    protected String getSupportedNodeName() {
      return "outputs";
    }
  }

  public class Output {
    public String object;
    public int ch;
  }

    
    
}
        
  
    