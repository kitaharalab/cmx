package jp.crestmuse.cmx.misc;
import java.util.*;

public class NoteStreamer<E extends OnsetOffsetCompatible> {

  private E[] data;
  private int ticksPerBeat;
  private int nextIndex = 0;
  private int nextTick = 0;

  public NoteStreamer(E[] data, int ticksPerBeat) {
    this.data = data;
    this.ticksPerBeat = ticksPerBeat;
  }

  public List<E> next(int duration) {
    if (nextIndex >= data.length)
      return null;
    else {
      List<E> l = new ArrayList<E>();
      while (nextIndex < data.length 
	     && data[nextIndex].onset(ticksPerBeat) < nextTick + duration) {
	l.add(data[nextIndex]);
	nextIndex++;
      }
      nextTick += duration;
      return l;
    }
  }

  public void skip(int duration) {
    while (nextIndex < data.length
	   && data[nextIndex].onset(ticksPerBeat) < nextTick + duration)
      nextIndex++;
    nextTick += duration;
  }
}
	
      