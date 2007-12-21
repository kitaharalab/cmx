package jp.crestmuse.cmx.misc;

public class QueueReader<E> {
  public E take() throws InterruptedException;
  public E get(int index);
  public boolean isAvailable(int index);
}
