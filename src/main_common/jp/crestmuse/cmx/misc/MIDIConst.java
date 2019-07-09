package jp.crestmuse.cmx.misc;
import java.util.HashMap;

import jp.crestmuse.cmx.sound.MIDIConsts;

/** @deprecated TO DO 代わりとなるクラスをsoundパッケージに作る */
public class MIDIConst implements MIDIConsts {

/*
    public static final short NOTE_OFF = 0x80;
    public static final short NOTE_ON = 0x90;
    public static final short POLY_KEY_PRESSURE = 0xA0;
    public static final short CONTROL_CHANGE = 0xB0;
    public static final short PROGRAM_CHANGE = 0xC0;
    public static final short CHANNEL_KEY_PRESSURE = 0xD0;
    public static final short PITCH_BEND_CHANGE = 0xE0;
    public static final short ALL_SOUND_OFF = 0x78;
    public static final short RESET_ALL_CONTROLLERS = 0x79;
    public static final short LOCAL_CONTROL = 0x7A;
    public static final short ALL_NOTES_OFF = 0x7B;
    public static final short OMNI_OFF = 0x7C;
    public static final short OMNI_ON = 0x7D;
    public static final short MONO_MODE = 0x7E;
    public static final short POLY_MODE = 0x7D;

    public static final short META_EVENT = 0x7F;

    public static final short END_OF_TRACK = 0x2F;
    public static final short SET_TEMPO = 0x51;
    public static final short SMPTE_OFFSET = 0x54; // non supported yet
    public static final short TIME_SIGNATURE = 0x58;
    public static final short KEY_SIGNATURE = 0x59;
    
    public static final byte OFF = 0;
    public static final byte ON = 127;
    public static final byte DUMMY_VALUE = 0;
*/

    //    private static final HashMap<String,String> ATTRIBUTE_KEY1 = 
    //	new HashMap<String,String>();
    //    private static final HashMap<String,String> ATTRIBUTE_KEY2 = 
    //	new HashMap<String,String>();
    //    private static final HashMap<String,Byte> NUM_OF_ATTRIBUTES = 
    //	new HashMap<String,Byte>();
    private static final HashMap<String,Short> CHNL_MSG = 
	new HashMap<String,Short>();
    private static final HashMap<Short,String> CHNL_MSG2 = 
	new HashMap<Short,String>();
    private static final HashMap<String,Short> META_EVENTS = 
	new HashMap<String,Short>();
    private static final HashMap<Short,String> META_EVENTS2 = 
	new HashMap<Short,String>();
  private static final HashMap<String,Boolean> HAS_STRING = 
    new HashMap<String,Boolean>();
    private static final HashMap<String,String[]> ATTLIST = 
	new HashMap<String,String[]>();


    static {
	addChannelMessage("NoteOn", NOTE_ON, "Note", "Velocity");
	
	addChannelMessage("NoteOff", NOTE_OFF, "Note", "Velocity");
	addChannelMessage("PolyKeyPressure", POLY_KEY_PRESSURE, 
			  "Note", "Pressure");
	addChannelMessage("ControlChange", CONTROL_CHANGE, 
			  "Control", "Value");
	addChannelMessage("ProgramChange", PROGRAM_CHANGE, "Number");
	addChannelMessage("ChannelKeyPresure", CHANNEL_KEY_PRESSURE, 
                          "Pressure");	
	addChannelMessage("PitchBendChange", PITCH_BEND_CHANGE, "Value");
	addChannelMessage("AllSoundOff", ALL_SOUND_OFF);
	addChannelMessage("ResetAllControllers", RESET_ALL_CONTROLLERS);
	addChannelMessage("LocalControl", LOCAL_CONTROL, "Value");
	addChannelMessage("AllNotesOff", ALL_NOTES_OFF);
	addChannelMessage("OmniOff", OMNI_OFF);
	addChannelMessage("OmniOn", OMNI_ON);
	addChannelMessage("MonoMode", MONO_MODE, "Value");
	addChannelMessage("PolyMode", POLY_MODE);
	//addAttributeKey("ControlChange14", "Control", "Value");
	//addAttributeKey("RPNChange", "RPN", "Value", RPN);
	//addAttributeKey("NRPNChange", "NRPN", "Value", NRPN);

        addMetaEvent("TextEvent", TEXT_EVENT, true);
        addMetaEvent("CopyrightNotice", COPYRIGHT_NOTICE, true);
        addMetaEvent("TrackName", TRACK_NAME, true);
        addMetaEvent("InstrumentName", INSTRUMENT_NAME, true);
        addMetaEvent("Lyric", LYRIC, true);
        addMetaEvent("Marker", MARKER, true);
        addMetaEvent("CuePoint", CUE_POINT, true);
	addMetaEvent("EndOfTrack", END_OF_TRACK, false);
	addMetaEvent("SetTempo", SET_TEMPO, false, "Value");
	// the numbers of bytes and attributes do not match for SMPTEOffset
	//addMetaEvent("SMPTEOffset", SMPTE_OFFSET, "TimeCodeType", "Hour", 
	//	     "Minute", "Second", "Frame", "FractionalFrame");
	addMetaEvent("TimeSignature", TIME_SIGNATURE, false, "Numerator", 
		     "LogDenominator", "MIDIClocksPerMetronomeClick", 
		     "ThirtySecondsPer24Clocks");
	addMetaEvent("KeySignature", KEY_SIGNATURE, false, "Fifths", "Mode");
//        addMetaEvent("SequencerSpecific", SEQUENCER_SPECIFIC, true);
    }

    /*
    private static void 
	addAttributeKey(String msgname, String key1, String key2, 
			short status) {
	ATTRIBUTE_KEY1.put(msgname, key1);
	ATTRIBUTE_KEY2.put(msgname, key2);
	if (key1 == null && key2 == null)
	    NUM_OF_ATTRIBUTES.put(msgname, (byte)0);
	else if (key1 != null && key2 == null)
	    NUM_OF_ATTRIBUTES.put(msgname, (byte)1);
	else if (key2 != null && key2 == null)
	    NUM_OF_ATTRIBUTES.put(msgname, (byte)2);
	STATUS_BASE.put(msgname, status);
	STATUS_BASE2.put(status, msgname);
    }
    */

    private static void
	addChannelMessage(String msgname, short status, String... attlist) {
	CHNL_MSG.put(msgname, status);
	CHNL_MSG2.put(status, msgname);
	ATTLIST.put(msgname, attlist);
    }

  private static void addMetaEvent(String evtname, short evttype, 
                                   boolean hasString, String... attlist) {
    META_EVENTS.put(evtname, evttype);
    META_EVENTS2.put(evttype, evtname);
    HAS_STRING.put(evtname, hasString);
    ATTLIST.put(evtname, attlist);
    }

  public static boolean isStringMetaEvent(String evtname) {
    return HAS_STRING.get(evtname);
  }

/*
    private static void 
	addMetaEvent(String evtname, short evttype, String... attlist) {
	META_EVENTS.put(evtname, evttype);
	META_EVENTS2.put(evttype, evtname);
	ATTLIST.put(evtname, attlist);
    }
*/
    public static boolean isSupportedMessage(String name) {
	return CHNL_MSG.containsKey(name);
    }

    public static boolean isSupportedMessage(short msg) {
	return CHNL_MSG2.containsKey(msg);
    }

    //    public static String getAttributeKey1(String msgname) {
    //	return ATTRIBUTE_KEY1.get(msgname);
    //    }

    //    public static String getAttributeKey2(String msgname) {
    //	return ATTRIBUTE_KEY2.get(msgname);
    //    }

    //    public static byte getNumOfAttributes(String msgname) {
    //	return NUM_OF_ATTRIBUTES.get(msgname);
    //    }

    public static short msgNameToStatusNo(String msgname) {
	return CHNL_MSG.get(msgname);
    }

    public static String statusNoToMsgName(short status) {
	return CHNL_MSG2.get(status);
    }

    public static boolean isSupportedMetaEvent(String evtname) {
	return META_EVENTS.containsKey(evtname);
    }

    public static boolean isSupportedMetaEvent(short evttype) {
	return META_EVENTS2.containsKey(evttype);
    }

    public static short metaEventNameToType(String evtname) {
	return META_EVENTS.get(evtname);
    }

    public static String metaEventTypeToName(short evttype) {
	return META_EVENTS2.get(evttype);
    }

    public static String[] getAttributeList(String evtname) {
	return ATTLIST.get(evtname);
    }

    public static boolean msbFirst(String msgname) {
	if (msgname.equals("PitchBendChange"))
	    return false;
	else
	    return true;
    }

    public static boolean msgFirst(short msgType) {
	if (msgType == PITCH_BEND_CHANGE)
	    return false;
	else
	    return true;
    }

    public static byte getByteLength(String msgname) {
	if (msgname.equals("PitchBendChange"))
	    return (byte)2;
	else if (msgname.equals("SetTempo"))
	    return (byte)3;
	else
	    return (byte)1;
    }

    public static byte getByteLength(short msgType) {
	if (msgType == PITCH_BEND_CHANGE)
	    return (byte)2;
	else if (msgType == SET_TEMPO)
	    return (byte)3;
	else
	    return (byte)1;
    }

    public static boolean isOnOffMessage(String msgname) {
	return msgname.equals("LocalControl");
    }

    public static byte onOffMsgStringToByte(String onoff) {
	if (onoff.equalsIgnoreCase("on"))
	    return ON;
	else if (onoff.equalsIgnoreCase("off"))
	    return OFF;
	else
	    throw new IllegalArgumentException("Not on or off: " + onoff);
    }

    public static String onOffMsgByteToString(byte onoff) {
	if (onoff == ON)
	    return "on";
	else if (onoff == OFF)
	    return "off";
	else
	    throw new IllegalArgumentException("Illegal value: " + onoff);
    }


}