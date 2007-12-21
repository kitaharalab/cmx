package jp.crestmuse.cmx.misc;

public interface QueueReader<E> extends Iterable<E> {
  public E take() throws InterruptedException;
  public E get(int index);
  public boolean isAvailable(int index);
}
