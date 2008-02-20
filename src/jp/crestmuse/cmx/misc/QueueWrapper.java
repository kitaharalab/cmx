package jp.crestmuse.cmx.misc;
import java.util.*;
import java.util.concurrent.*;

public class QueueWrapper<E> {
  private java.util.Queue<E> queue;
  private ArrayList<E> list;
  private int size;
  private byte nReaders = (byte)0;
  private byte[] alreadyRead;

  public QueueWrapper(java.util.Queue<E> q, int size) {
    queue = q;
    this.size = size;
    list = new ArrayList<E>();
    alreadyRead = new byte[size];
  }

  public QueueReader createReader() {
    nReaders++;
    return new QueueReaderImpl();
  }

  private class QueueReaderImpl implements QueueReader<E> {
    private int next;
    private QueueReaderImpl() {
      next = 0;
    }

    public E take() throws InterruptedException {
      E e;
      if (next < list.size()) {
        e = list.get(next);
      } else {
        if (queue instanceof BlockingQueue)
          e = ((BlockingQueue<E>)queue).take();
        else
          e = queue.poll();
        list.add(e);
      }
      alreadyRead[next]++;
      if (alreadyRead[next] >= nReaders)
        list.set(next, null);
      next++;
      return e;
    }

/*
    public E get(int index) {
      if (index < next) {
        return list.get(index);
      } else {
        throw new IndexOutOfBoundsException();
      }
    }
*/

//    public boolean isAvailable(int index) {
//      return index < next;
//    }

    public Iterator<E> iterator() {
      return new Iterator() {
          public boolean hasNext() {
            return next < size;
          }
          public E next() {
            try {
              return take();
            } catch (InterruptedException e) {
              throw new RuntimeException();
            }
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
    }

  }
}
