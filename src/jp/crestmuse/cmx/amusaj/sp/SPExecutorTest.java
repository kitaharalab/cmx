package jp.crestmuse.cmx.amusaj.sp;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.misc.QueueWrapper;

import org.testng.AssertJUnit;
import org.testng.annotations.*;

/**
 * TestNG用のテストクラスです
 * @author ntotani
 *
 */
public class SPExecutorTest {

  @Test
  public void singleModule() throws InterruptedException{
    int frame = 10;
    SPExecutor sp = new SPExecutor(null, 10, 0);
    IntGenerator ig = new IntGenerator();
    sp.addSPModule(ig);
    sp.start();
    while(!sp.finished(ig)){
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    QueueReader<Integer> q = sp.getResult(ig).get(0).getQueueReader();
    for(int i=0; i<frame; i++)
      AssertJUnit.assertEquals(i, q.take().intValue());
  }
  
  @Test(dependsOnMethods={"singleModule"})
  public void multiModule() throws InterruptedException{
    int frames = 10;
    SPExecutor sp = new SPExecutor(null, frames, 0);
    IntGenerator ig = new IntGenerator();
    MultiplyBy2 m2 = new MultiplyBy2();
    MultiplyBy2 m4 = new MultiplyBy2();
    IntPrinter ip = new IntPrinter();
    sp.addSPModule(ig);
    sp.addSPModule(m2);
    sp.addSPModule(m4);
    sp.addSPModule(ip);
    sp.connect(ig, 0, m2, 0);
    sp.connect(m2, 0, m4, 0);
    sp.connect(m4, 0, ip, 0);
    sp.start();
    
    while(!sp.finished()){
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    QueueReader<Integer> q = sp.getResult(m2).get(0).getQueueReader();
    for(int i=0; i<frames; i++)
      AssertJUnit.assertEquals(i*2, q.take().intValue());
    
    q = sp.getResult(m4).get(0).getQueueReader();
    for(int i=0; i<frames; i++)
      AssertJUnit.assertEquals(i*4, q.take().intValue());
  }
  
  @Test
  public void stop() throws InterruptedException{
    SPExecutor sp = new SPExecutor(null, 0, 0);
    sp.addSPModule(new IntGenerator());
    sp.start();
    Thread.sleep(1000);
    AssertJUnit.assertEquals(false, sp.finished());
    sp.stop();
    Thread.sleep(1000);
    AssertJUnit.assertEquals(true, sp.finished());
  }
  
  private class IntGenerator implements ProducerConsumerCompatible<Object, Integer>{
    
    int count = 0;

    public TimeSeriesCompatible<Integer> createOutputInstance(int frames,
        int timeunit) {
      return new IntSequence(frames);
    }

    public void execute(List<QueueReader<Object>> src,
        List<TimeSeriesCompatible<Integer>> dest) throws InterruptedException {
      dest.get(0).add(count++);
    }

    public int getInputChannels() {
      return 0;
    }

    public int getOutputChannels() {
      return 1;
    }

    public void setParams(Map<String, Object> params) {
    }
    
  }

  private class MultiplyBy2 implements ProducerConsumerCompatible<Integer, Integer>{

    public TimeSeriesCompatible<Integer> createOutputInstance(int frames,
        int timeunit) {
      return new IntSequence(frames);
    }

    public void execute(List<QueueReader<Integer>> src,
        List<TimeSeriesCompatible<Integer>> dest) throws InterruptedException {
      dest.get(0).add(src.get(0).take() * 2);
    }

    public int getInputChannels() {
      return 1;
    }

    public int getOutputChannels() {
      return 1;
    }

    public void setParams(Map<String, Object> params) {
    }
    
  }

  private class IntPrinter implements ProducerConsumerCompatible<Integer, Object>{

    public TimeSeriesCompatible<Object> createOutputInstance(int frames,
        int timeunit) {
      return null;
    }

    public void execute(List<QueueReader<Integer>> src,
        List<TimeSeriesCompatible<Object>> dest) throws InterruptedException {
      System.out.println(src.get(0).take());
    }

    public int getInputChannels() {
      return 1;
    }

    public int getOutputChannels() {
      return 0;
    }

    public void setParams(Map<String, Object> params) {
    }
    
  }

  private class IntSequence implements TimeSeriesCompatible<Integer>{

    private int frames;
    private LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
    private QueueWrapper<Integer> qWrapper;
    
    IntSequence(int frames){
      this.frames = frames;
      queue = new LinkedBlockingQueue<Integer>();
      qWrapper = new QueueWrapper<Integer>(queue, frames);
    }

    public void add(Integer d) throws InterruptedException {
      queue.add(d);
    }

    public int bytesize() {
      return 4;
    }

    public int dim() {
      return 1;
    }

    public int frames() {
      return frames;
    }

    public QueueReader<Integer> getQueueReader() {
      return qWrapper.createReader();
    }

    public int timeunit() {
      return 0;
    }

    public String getAttribute(String key) {
      return null;
    }

    public double getAttributeDouble(String key) {
      return 0;
    }

    public int getAttributeInt(String key) {
      return 0;
    }

    public Iterator<Entry<String, String>> getAttributeIterator() {
      return null;
    }

    public void setAttribute(String key, String value) {
    }

    public void setAttribute(String key, int value) {
    }

    public void setAttribute(String key, double value) {
    }
    
  }
}
