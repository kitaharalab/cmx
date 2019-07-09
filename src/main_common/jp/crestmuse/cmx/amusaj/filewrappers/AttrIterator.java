package jp.crestmuse.cmx.amusaj.filewrappers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Map;


 class AttrIterator implements Iterator<Map.Entry<String,String>> {
    private int i = 0;
    private NamedNodeMap nodemap;
    private int length;
    AttrIterator(NamedNodeMap nodemap) {
      this.nodemap = nodemap;
      length = nodemap.getLength();
    }
    public boolean hasNext() {
      return i < length;
    }
    public AttrEntry next() {
      return new AttrEntry(nodemap.item(i++));
    }
    public void remove() {
      throw new UnsupportedOperationException();
    }
        
   private class AttrEntry implements Map.Entry<String,String> {
     private String k, v;
     private AttrEntry(Node node) {
       this.k = node.getNodeName();
       this.v = node.getNodeValue();
     }
     public String getKey() {
       return k;
     }
     public String getValue() {
       return v;
     }
     public String setValue(String v) {
       throw new UnsupportedOperationException();
     }
     public boolean equals(Object o) {
       if (!(o instanceof Map.Entry))
         return false;
       Map.Entry e = (Map.Entry)o;
       return k.equals(e.getKey()) && v.equals(e.getValue());
     }
     public int hashCode() {
       int khash = (k==null ? 0 : k.hashCode());
       int vhash = (v==null ? 0 : v.hashCode());
       return khash ^ vhash;
     }
   }
 }