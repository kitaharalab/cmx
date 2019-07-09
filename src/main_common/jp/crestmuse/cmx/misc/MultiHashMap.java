package jp.crestmuse.cmx.misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MultiHashMap<K, V> implements MultiMap<K,V> {

  private Map<K, List<V>> m;

  public MultiHashMap() {
    m = new HashMap<K, List<V>>();
  }

  public V get(K key, int index) {
    if (m.containsKey(key))
      return m.get(key).get(index);
    else
      return null;
  }

  public List<V> get(K key) {
    return m.get(key);
  }

  public void put(K key, V value) {
    if (!m.containsKey(key))
      m.put(key, new ArrayList<V>());
    m.get(key).add(value);
  }

  public Iterator<V> iterator(K key) {
      if (m.containsKey(key)) 
        return m.get(key).iterator();
      else
        return null;
  }

  public void add(K key, V value) {
    put(key, value);
  }

  public int size() {
    return m.size();
  }

  public int size(K key) {
    if (m.containsKey(key)) 
      return m.get(key).size();
    else
      return 0;
  }

  public Collection<List<V>> values() {
    return m.values();
  }
}