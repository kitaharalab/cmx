package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper.*;
import java.util.*;

/** 
<p>TimeInSecForSMF checks the equivalency of two 
standard MIDI files. </p>
<p>Usage:
<pre>
$java jp.crestmuse.cmx.commands.TimeInSecForSMF file1.mid file2.mid
</pre>
</p>
<p>TimeInSecForSMFは、2つのSMFの等価性を検証するコマンドです。
このコマンドは、CrestMusePEDBのデータ作成の用いられます。</p>
*/
    

public class TimeInSecForSMF {
  MIDIEvent[] tempolist;
  int currentTick0 = 0;
  MIDIEvent[] notelist;
  int currentTick1 = 0;
  double currentTempo = 120.0;
  int currentTempoTick;
  double currentTempoTimeInSec;
  int ticksPerBeat;

  TimeInSecForSMF(MIDIXMLWrapper midi) {
    tempolist = midi.getTrackList()[0].getMIDIEventList();
    notelist = midi.getTrackList()[1].getMIDIEventList();
    ticksPerBeat = midi.ticksPerBeat();
  }

  List<NoteEvent> calcTimeInSec() {
    List<NoteEvent> l = new ArrayList<NoteEvent>();
    int i = 0, j = 0;
    for ( ; i < notelist.length; ) {
      int tick0 = (j < tempolist.length) ? 
        tempolist[j].deltaTime() + currentTick0 : 
        Integer.MAX_VALUE;
      int tick1 = notelist[i].deltaTime() + currentTick1;
      if (tick0 <= tick1) {
        if (tempolist[j].messageType().equals("SetTempo")) {
          double t = (double)(tick0 - currentTempoTick) * 60.0 / 
            ((double)ticksPerBeat * currentTempo) + currentTempoTimeInSec;
          currentTempo = (double)(60*1000*1000) / tempolist[j].value(0);
          currentTempoTick = tick0;
          currentTempoTimeInSec = t;
        }
        currentTick0 = tick0;
        j++;
      } else {
        if (notelist[i].messageType().startsWith("Note")) {
          double t = (double)(tick1 - currentTempoTick) * 60.0 / 
            ((double)ticksPerBeat * currentTempo) + currentTempoTimeInSec;
          l.add(new NoteEvent(t, notelist[i]));
        }
        currentTick1 = tick1;
        i++;
      }
    }
    return l;
  }

  static void compare(List<NoteEvent> l2, List<NoteEvent> l1) {
    int size2 = l2.size();
    int size1 = l1.size();
    int i = 0, j = 0;
  mainloop:
    while (i < size2 || j < size1) {
      NoteEvent e2 = i < size2 ? l2.get(i) : null;
      NoteEvent e1 = j < size1 ? l1.get(j) : null;
//      for (int k = 1; k < 10; k++) {
//        if (i+k < size2 && e1.approxEquals(l2.get(i+k)) && 
//            j+k < size1 && e2.approxEquals(l1.get(j+k))) {
//          Collections.swap(l1, j, j+k);
//          e2 = i < size2 ? l2.get(i) : null;
//          e1 = j < size1 ? l1.get(j) : null;
//          break;
//        }
//      }
      if (e1 == null) {
        System.out.println(makeString(e2, null));
//        System.out.println(makeString(e2, null) + "  ==> diff");
        i++;
        j++;
      } else if (e2 == null) {
        System.out.println(makeString(null, e1));
//        System.out.println(makeString(null, e1) + "  ==> diff");
        i++;
        j++;
      } else if (e1.approxEquals(e2)) {
        System.out.println(makeString(e2, e1));
//        System.out.println(makeString(e2, e1) + "  ==> ok");
        i++;
        j++;
      } else {
        for (int k = 1; k < 10; k++) {
          if (e2.approxEquals(j+k<size1 ? l1.get(j+k): null)) {
            for (int m = 1; m <= k; m++) 
              System.out.println(makeString(null, l1.get(j++)));
//            System.out.println(makeString(null, l1.get(j++)) + "  ==> diff");
//            j++;
            continue mainloop;
          }
        }
        for (int k = 1; k < 10; k++) {
          if (e1.approxEquals(i+k<size2 ? l2.get(i+k) : null)) {
            for (int m = 0; m < k; m++)
              System.out.println(makeString(l2.get(i++), null));
//            System.out.println(makeString(l2.get(i++), null) + "  ==> diff");
//            i++;
            continue mainloop;
          }
        }
        System.out.println(makeString(e2, e1));
        i++;
        j++;
      }
    }
  }

  static boolean isOn1 = false;
  static boolean isOn2 = false;

  static NoteEvent lastE1 = null;
  static NoteEvent lastE2 = null;
  

    private static String makeString(NoteEvent e1, NoteEvent e2) {
      StringBuffer sbuff = new StringBuffer();
      if (e1 != null)
        sbuff.append("[").append(e1.toString()).append("]  ");
      else
        sbuff.append("[null]                    ");
      if (e2 != null)
        sbuff.append("[").append(e2.toString()).append("]");
      else
        sbuff.append("[null]                  ");
      if (e1 != null && e2 != null && e1.approxEquals(e2))
        sbuff.append("  ==> ok");
//      else if (e1 == null && !isOn2 && e2.msg.equals("NoteOff"))
//        ;
//      else if (e2 == null && !isOn1 && e1.msg.equals("NoteOff"))
//        ;
//      else if (e1 == null && isOn1 && e2.msg.equals("NoteOff"))  // kari
//        ;
//      else if (e2 == null && e1.equals(lastE1))
//        ;
      else
        sbuff.append(" ==> diff");
      if (e1 != null && e1.msg.equals("NoteOn"))
        isOn1 = true;
      else if (e1 != null && e1.msg.equals("NoteOff"))
        isOn1 = false;
      if (e2 != null && e2.msg.equals("NoteOn"))
        isOn2 = true;
      else if (e2 != null && e2.msg.equals("NoteOff"))
        isOn2 = false;
      lastE1 = e1;
      lastE2 = e2;
      return sbuff.toString();
    }
        
  public static void main(String[] args) {
    try {
      TimeInSecForSMF2 t2 = new TimeInSecForSMF2
        (MIDIXMLWrapper.readSMF(args[0]));
      List<NoteEvent> l2 = t2.calcTimeInSec();
      Collections.sort(l2);
      TimeInSecForSMF t = new TimeInSecForSMF
        (MIDIXMLWrapper.readSMF(args[1]));
      List<NoteEvent> l1 = t.calcTimeInSec();
      Collections.sort(l1);
      compare(l2, l1);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}

class TimeInSecForSMF2 {
  MIDIEvent[] notelist;
  int currentTick = 0;
  static final double TEMPO = 120.0;
  int ticksPerBeat;

  TimeInSecForSMF2(MIDIXMLWrapper midi) {
    notelist = midi.getTrackList()[0].getMIDIEventList();
    ticksPerBeat = midi.ticksPerBeat();
  }

  List<NoteEvent> calcTimeInSec() {
    List<NoteEvent> l = new ArrayList<NoteEvent>();
    for (int i = 0;  i < notelist.length; i++) {
      int tick = notelist[i].deltaTime() + currentTick;
      if (notelist[i].messageType().startsWith("Note")) {
        double t = (double)tick * 60.0 / ((double)ticksPerBeat * TEMPO);
        l.add(new NoteEvent(t, notelist[i]));
      }
      currentTick = tick;
    }
    return l;
  }
}

class NoteEvent implements Comparable<NoteEvent> {
  double time;
  String msg;
  int notenum;
  int vel;
  NoteEvent(double time, MIDIEvent e) {
    this.time = time;
    msg = e.messageType();
    notenum = e.value(0);
    vel = e.value(1);
    if (vel == 0)
      msg = "NoteOff";
    else if (msg.equals("NoteOff"))
      vel = 0;
  }
  public String toString() {
    return String.format("%7f", time) + " " + msg + " " + notenum + " " + vel;
  }
  public boolean equals(Object o) {
    NoteEvent e = (NoteEvent)o;
    return e != null && time == e.time && msg.equals(e.msg)
      && notenum == e.notenum && vel == e.vel;
  }
  boolean approxEquals(NoteEvent e) {
    return e != null && Math.abs(time - e.time) < 0.02 && msg.equals(e.msg)
      && notenum == e.notenum && Math.abs(vel - e.vel) <= 1;
  }
  public int compareTo(NoteEvent e) {
    if (notenum == e.notenum)
      return time > e.time ? 1 : -1;
    else
      return notenum - e.notenum;
  }        
}
