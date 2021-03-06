package jp.crestmuse.cmx.misc;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Queue<E> {

  private LinkedList<E> list;

  public Queue() {
    list = new LinkedList<E>();
  }

  public void enqueue(E e) {
    list.add(e);
  }

  public E dequeue() {
    try {
      return list.removeFirst();
    } catch (NoSuchElementException e) {
      throw new EmptyQueueException();
    }
  }

  public int size() {
    return list.size();
  }

  public String[] toArray() {
    return list.toArray(new String[0]);
  }

}