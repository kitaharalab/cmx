package jp.crestmuse.cmx.handlers;

import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;

/**
 * MIDIイベントを処理するためのインターフェース．<tt>MIDIXML</tt>クラスの
 * <tt>processNote</tt>メソッドから呼び出される．
 * @author totani
 * @since 2007.08.08
 *
 */
public interface MIDIHandler {
////	public void processFormat(byte format, MIDIXMLWrapper w);
////	public void processTrackCount(int trackCount, MIDIXMLWrapper w);
////	public void processTicksPerBeat(int ticksPerBeat, MIDIXMLWrapper w);
////	public void processTimeStampType(byte timestampType, MIDIXMLWrapper w);
//  public void processHeaders(byte format, int trackCount, 
//                             int ticksPerBeat, byte timestampType, 
//                             MIDIXMLWrapper w);
  public void beginTrack(MIDIXMLWrapper.Track track, MIDIXMLWrapper w);
  public void endTrack(MIDIXMLWrapper.Track track, MIDIXMLWrapper w);
  public void processMIDIEvent(MIDIXMLWrapper.MIDIEvent midiEvent, 
                               MIDIXMLWrapper w);

}
