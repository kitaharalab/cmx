package jp.crestmuse.cmx.amusaj.filewrappers;

public class MutableMIDINoteArraySeries extends MutableEventSeries<MIDINoteArray>{
  
  public MutableMIDINoteArraySeries(int nFrames, int timeunit){
    super(nFrames, timeunit);
  }

  public void add(MIDINoteArray d) throws InterruptedException {
    queue.put(d);
  }

  public int bytesize() {
    return MIDINoteArray.DIM * 4;
  }

  public int dim() {
    return MIDINoteArray.DIM;
  }
  
}
