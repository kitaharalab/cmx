package jp.crestmuse.cmx.misc;
import java.util.*;

public interface MultiMap<K,V> {
  public V get(K key, int index);
  public List<V> get(K key);
  public void put(K key, V value);
  public Iterator<V> iterator(K key);
  public void add(K key, V value);
  public int size();
  public int size(K key);
  public Collection<List<V>> values();
}