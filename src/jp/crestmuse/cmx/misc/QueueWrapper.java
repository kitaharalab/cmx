package jp.crestmuse.cmx.misc;
import java.util.*;
import java.concurrent.*;

public class QueueWrapper<E> {
  private java.util.Queue<E> queue;
  private ArrayList<E> list;

  public QueueWrapper(java.util.Queue<E> q) {
    queue = q;
    list = new ArrayList<E>();
  }

  public QueueReader createReader() {
    return new QueueReaderImpl();
  }

  private class QueueReaderImpl implements QueueReader<E> {
    private int next;
    private QueueReaderImpl() {
      next = 0;
    }

    public E take() throws InterruptedException {
      if (next < list.size()) {
        return list.get(next++);
      } else {
        E e;
        if (queue instanceof BlockingQueue)
          e = ((BlockingQueue)queue).take();
        else
          e = queue.poll();
        list.add(e);
        next++;
        return e;
      }
    }

    public E get(int index) {
      if (index < next) {
        return list.get(index);
      } else {
        throw new IndexOutOfBoundsException();
      }
    }

    public boolean isAvailable(int index) {
      return index < next;
    }
  }
}
