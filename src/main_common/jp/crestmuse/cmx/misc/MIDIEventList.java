package jp.crestmuse.cmx.misc;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static jp.crestmuse.cmx.misc.MIDIConst.msgNameToStatusNo;
import static jp.crestmuse.cmx.misc.MIDIConst.statusNoToMsgName;

public class MIDIEventList implements Iterable<MIDIEventList.MIDIEvent> {

    private SortedSet<MIDIEvent> s;

    public MIDIEventList() {
	s = new TreeSet<MIDIEvent>(new Comparator<MIDIEvent>() {
	    public int compare(MIDIEvent e1, MIDIEvent e2) {
		return 
		    e1.time == e2.time ?
		        (e1.status == e2.status ?
			    (e1.ch == e2.ch ?
                                (e1.value1 == e2.value1 ?
                                    e1.value2 - e2.value2
                                    : e1.value1 - e2.value1
                                ) : e1.ch - e2.ch
                            ) : e1.status - e2.status
                         ) : (int)(e1.time - e2.time);
             }
	});
    }

    public void addEvent(long time, String msgname, byte ch, 
                         int value1, int value2) {
        s.add(new MIDIEvent(time, msgname, ch, value1, value2));
    }

    public void addEvent(long time, short status, byte ch, 
                         int value1, int value2) {
        s.add(new MIDIEvent(time, status, ch, value1, value2));
    }

    public Iterator<MIDIEvent> iterator() {
        return s.iterator();
    }

    public final class MIDIEvent {
      private long time;
      private int value1 = 0, value2 = 0;
	private short status;
	private String msgname;
	private byte ch;
	MIDIEvent(long time, short status, byte ch, int value1, int value2) {
            this.time = time;
	    this.status = status;
	    this.value1 = value1;
	    this.value2 = value2;
	    this.msgname = statusNoToMsgName(status);
	    this.ch = ch;
        }
	MIDIEvent(long time, String msgname, byte ch, int value1, int value2) {
	    this.time = time;
            this.msgname = msgname;
	    this.value1 = value1;
	    this.value2 = value2;
	    this.status = msgNameToStatusNo(msgname);
	    this.ch = ch;
        }
	public final long time() {
            return time;
        }
        public final short status() {
            return status;
        }
        public final String msgname() {
            return msgname;
        }
        public final byte channel() {
            return ch;
        }
        public final int value1() {
            return value1;
        }
        public final int value2() {
            return value2;
        }
    }
	    
}
