package jp.crestmuse.cmx.filewrappers;

import java.util.*;
//import javax.xml.transform.*;
import org.w3c.dom.*;
//import org.apache.xpath.*;

/**********************************************************************
 *<p>The abstract class <tt>NodeInterface</tt> is the common superclass 
 *of classes that provide interfaces for accessing XML nodes. 
 *</p>
 *
 *<p>抽象クラス<tt>NodeInterface</tt>は, XMLドキュメント内のノードに
 *アクセスするためのインターフェースを提供するクラスの共通基底クラスです.</p>
 *
 *<p>典型的な使いかたとしては, ファイルラッパ(CMXFileWrapperのサブクラス)内に, 
 *対応するXMLフォーマットの仕様に合わせて, 各要素へのアクセスをサポートする
 *クラスをこのクラスのサブクラスとして, またファイルラッパの内部クラスとして
 *定義します.
 *たとえば, MusicXMLWrapperクラスにはNoteというクラスがあり, 
 *これがMusicXMLフォーマットのnote要素へのアクセスをサポートします. 
 *NoteクラスはMusicXMLWrapperの内部クラスで, NodeInterfaceクラスの
 *サブクラスとなっています.</p>
 *
 *@author Tetsuro Kitahara <t.kitahara@ksc.kwansei.ac.jp>
 *@version 0.20.000
 *********************************************************************/
public abstract class NodeInterface {
  private Element node;
//  private Node node;
  private String nodename;
//  private NamedNodeMap attrmap = null;
//  private NodeList children = null;
//  private int nChildren;

  /**********************************************************************
   *<p>Constructs an node interface for the specified node. 
   *When the specified node is not supported, 
   *UnsupportedNodeException is thrown.</p>
   *
   *<p>指定されたノードに対するノードインターフェースを生成します. 
   *指定されたノードがサポートされていない場合, 
   *UnsupportedNodeExceptionがスローされます.</p>
   *
   *@exception jp.crestmuse.cmx.filewrappers.UnsupportedNodeException 
               when the specified node is not supported. 
   *********************************************************************/
  protected NodeInterface(Node node) {
    nodename = node.getNodeName();
    if (nodename.equals(getSupportedNodeName())) {
      this.node = (Element)node;
    } else if (getSupportedNodeName().indexOf(nodename) >= 0) {
      this.node = (Element)node;
    } else {
//      StringTokenizer t = new StringTokenizer(getSupportedNodeName(), "|");
//      while (t.hasMoreTokens()) {
//        if (t.nextToken().equals(nodename)) {
//	  this.node = node;
//	  return;
//        }
//      }
      throw new UnsupportedNodeException
			("Unsupported node: " + node.getNodeName());
    }
  }

  /**********************************************************************
   *<p>Returns the node wrapped by this object.</p>
   *
   *<p>このオブジェクトがラップするノードを返します. 
   *現在のところこのメソッドはpublicとなっていますが, publicにすべきかは
   *議論のあるところで, 今後変更される場合があります.
   *(試験的にprotectedに変更しています)
   *********************************************************************/
   protected final Node node() {     // kari
    return node;
  }

  /**********************************************************************
   *<p>Returns the node name supported by the class.
   *Please override this method in a subclass.</p>
   *
   *このオブジェクトがサポートするノード名を返します. 
   *このメソッドはサブクラスでオーバーライドしてください.
   *********************************************************************/
  protected abstract String getSupportedNodeName();

  /**********************************************************************
   *<p>Returns the name of the node wrapped by this object.</p>
   *<p>このオブジェクトがラップしているノード名を返します.</p>
   *********************************************************************/
  public final String getNodeName() {
    return nodename;
  }

  /**********************************************************************
   *<p>Checks whether the node wrapped by this object has a child with 
   *the specified name.</p>
   *<p>このオブジェクトがラップしているノードが指定された名前の子を持つかどうか
   *調べます.</p>
   *********************************************************************/
//  public final boolean hasChild(String tagname) {
//    return hasChild(tagname, node);
//  }

  /**********************************************************************
   *<p>Returns the text that a child with the specified tag name has.</p>
   *<p>指定されたタグ名の子が持つテキストを返します.</p>
   *********************************************************************/
  public final String getChildText(String tagname) {
    return getText(getChildByTagName(tagname));
  }

  /**********************************************************************
   *<p>Returns the text that a child with the specified tag name has 
   *as an integer.</p>
   *<p>指定されたタグ名の子が持つテキストを整数値として返します.</p>
   *********************************************************************/
  public final int getChildTextInt(String tagname) {
    return getTextInt(getChildByTagName(tagname));
  }

  /**********************************************************************
   *<p>Returns the text that a child with the specified tag name has 
   *as a real number.</p>
   *<p>指定されたタグ名の子が持つテキストを実数値として返します.</p>
   *********************************************************************/
  public final double getChildTextDouble(String tagname) {
    return getTextDouble(getChildByTagName(tagname));
  }

  public final String getText() {
    return getText(node);
  }

  public boolean hasAttribute(String key) {
    return hasAttribute(node, key);
  }

  public String getAttributeNS(String key, String namespace) {
    return node.getAttributeNS(namespace, key);
//    if (node == null) return null;
//    if (attrmap == null) attrmap = node.getAttributes();
//    return attrmap.getNamedItemNS(namespace, key).getNodeValue();
  }

  public String getAttribute(String key) {
    return node.getAttribute(key);
//    if (node == null) return null;
//    if (attrmap == null) attrmap = node.getAttributes();
//    return attrmap.getNamedItem(key).getNodeValue();
  }

  public int getAttributeInt(String key) {
    return Integer.parseInt(getAttribute(key));
  }

  public double getAttributeDouble(String key) {
    return Double.parseDouble(getAttribute(key));
  }

  /**********************************************************************
   *<p>Returns all the child nodes</p>
   *<p>すべての子ノードを返します.</p>
   *********************************************************************/
  protected final NodeList getChildNodes() {
    return node.getChildNodes();
  }

  protected final Node getFirstChild() {
    return node.getFirstChild();
  }

  protected final Node getLastChild() {
    return node.getLastChild();
  }

  /**********************************************************************
   *<p>Returns the child node with the specified tag name.</p>
   *<p>指定されたタグ名の子ノードを返します.</p>
   *********************************************************************/
  protected final Node getChildByTagName(String tagname) {
    NodeList nl = node.getElementsByTagName(tagname);
    if (nl.getLength() >= 1)
      return nl.item(0);
    else
      return null;
//    if (node == null) return null;
//    if (children == null) {
//      children = node.getChildNodes();
//      nChildren = children.getLength();
//    }
//    for (int i = 0; i < nChildren; i++) {
//      Node n = children.item(i);
//      if (n.getNodeName().equals(tagname))
//        return n;
//    }
//    return null;
  }

  protected final Node getChildByTagNameNS(String tagname, String ns) {
    NodeList nl = node.getElementsByTagNameNS(ns, tagname);
    if (nl.getLength() >= 1)
      return nl.item(0);
    else
      return null;
  }
  
  /**********************************************************************
   *Returns the child node with the specified tag name 
   *of the specified node. <br>
   *指定されたノードに対する, 指定されたタグ名の子ノードを返します. 
   *********************************************************************/
  static Node getChildByTagName(String tagname, Node node) {
    if (node == null)
      return null;
    NodeList nl = ((Element)node).getElementsByTagName(tagname);
    if (nl.getLength() >= 1)
      return nl.item(0);
    else
      return null;
//    NodeList children = node.getChildNodes();
//    for (int i = 0; i < children.getLength(); i++) {
//      Node n = children.item(i);
//      if (n.getNodeName().equals(tagname))
//        return n;
//    }
//    return null;
  }

  protected static boolean hasChild(String tagname, Node node) {
    return (getChildByTagName(tagname, node) != null);
  }

  protected static String getText(Node node) {
    if (node == null)
      return null;
    NodeList children = node.getChildNodes();
    String s = "";
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (n.getNodeType() == Node.TEXT_NODE) 
        s += n.getNodeValue();
    }
    return s.trim();
  }

  protected static final int getTextInt(Node node) throws NullPointerException{
//    if (node == null) 
//      return -1;
//    else
      return Integer.parseInt(getText(node));
  }

  protected static final double getTextDouble(Node node) 
  throws NullPointerException {
//    if (node == null) 
//      return Double.NaN;
//    else
      return Double.parseDouble(getText(node));
  }

  protected static final boolean hasAttribute(Node node, String attrkey) {
    return ((Element)node).hasAttribute(attrkey);
  }

  protected static final String getAttribute(Node node, String attrkey) {
    try {
      return node.getAttributes().getNamedItem(attrkey).getNodeValue();
    } catch (NullPointerException e) {
      return null;                 
    }
  }

  protected static final int getAttributeInt(Node node, String attrkey) {
    return Integer.parseInt(getAttribute(node, attrkey));
  }

  protected static final double getAttributeDouble(Node node, String attrkey) {
    return Double.parseDouble(getAttribute(node, attrkey));
  }
    
//  NodeList selectNodeList(String xpath) throws TransformerException {
//    return XPathAPI.selectNodeList(node, xpath);
//  }
//
//  Node selectSingleNode(String xpath) throws TransformerException {
//    return XPathAPI.selectSingleNode(node, xpath);
//  }


}