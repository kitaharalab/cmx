package jp.crestmuse.cmx.misc;
import java.util.*;
import java.util.concurrent.*;

public class QueueWrapper<E> {
  private java.util.Queue<E> queue;
  private ArrayList<PacketWithReadedCount> list;
  private byte nReaders = (byte)0;
  int removedNum;
  
  public QueueWrapper(java.util.Queue<E> q){
    queue = q;
    list = new ArrayList<PacketWithReadedCount>();
    removedNum = 0;
  }

  @Deprecated
  public QueueWrapper(java.util.Queue<E> q, int size) {
    queue = q;
    list = new ArrayList<PacketWithReadedCount>();
    removedNum = 0;
  }

  public QueueReader<E> createReader() {
    nReaders++;
    return new QueueReaderImpl();
  }
  
  private class PacketWithReadedCount{
    E packet;
    int readedCount;
    public PacketWithReadedCount(E packet) {
      this.packet = packet;
      this.readedCount = 0;
    }
  }

  private class QueueReaderImpl implements QueueReader<E> {
    private int next;
    private QueueReaderImpl() {
      next = 0;
    }

    public E take() throws InterruptedException {
      PacketWithReadedCount pwrc;
      if (next < list.size() + removedNum) {
        pwrc = list.get(next - removedNum);
      } else {
        E e;
        if (queue instanceof BlockingQueue)
          e = ((BlockingQueue<E>)queue).take();
        else
          e = queue.poll();
        pwrc = new PacketWithReadedCount(e);
        list.add(pwrc);
      }
      pwrc.readedCount++;
      if (pwrc.readedCount >= nReaders){
        list.remove(0);
        removedNum++;
      }
      next++;
      return pwrc.packet;
    }

    public Iterator<E> iterator() {
      return new Iterator<E>() {
          public boolean hasNext() {
            return next < list.size() + removedNum;
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
