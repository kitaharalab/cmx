package jp.crestmuse.cmx.misc;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

public class NodeLinkManager {
    private MultiMap<Node,Node> h1, h2;

  public NodeLinkManager() {
      h1 = new MultiHashMap<Node,Node>();
      h2 = new MultiHashMap<Node,Node>();
  }

  public void addLink(Node from, Node to) {
    h1.put(from, to);
    h2.put(to, from);
  }

  public void addLink(Node from, NodeList to) {
    for (int i = 0; i < to.getLength(); i++) {
      Node n = to.item(i);
      h1.put(from, n);
      h2.put(n, from);
    }
  }

  public Iterator<Node> getNodesLinkedFrom(Node node) {
    return h1.iterator(node);
  }

  public Iterator<Node> getNodesLinkedTo(Node node) {
    return h2.iterator(node);
  }

  public Node getNodeLinkedFrom(Node node, String tagname) {
    Iterator<Node> it = getNodesLinkedFrom(node);
    if (it != null) {
      while (it.hasNext()) {
        Node n = it.next();
        if (n.getNodeName().equals(tagname))
          return n;
      }
    }
    return null;
  }

  public Node getNodeLinkedTo(Node node, String tagname) {
    Iterator<Node> it = getNodesLinkedTo(node);
    if (it != null) {
      while (it.hasNext()) {
        Node n = it.next();
        if (n.getNodeName().equals(tagname))
          return n;
      }
    }
    return null;
  }

//  public Node getNodeLinkedFrom(Node node) {
//    return (Node)h1.get(node);
//  }

//  public Node getNodeLinkedTo(Node node) {
//    return (Node)h2.get(node);
//  }
}
