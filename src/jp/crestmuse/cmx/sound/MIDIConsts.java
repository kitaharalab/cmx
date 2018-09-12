package jp.crestmuse.cmx.sound;

public interface MIDIConsts {
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

  public static final short TEXT_EVENT = 0x01;
  public static final short COPYRIGHT_NOTICE = 0x02;
  public static final short TRACK_NAME = 0x03;
  public static final short INSTRUMENT_NAME = 0x04;
  public static final short LYRIC = 0x05;
  public static final short MARKER = 0x06;
  public static final short CUE_POINT = 0x07;
  public static final short END_OF_TRACK = 0x2F;
  public static final short SET_TEMPO = 0x51;
  public static final short SMPTE_OFFSET = 0x54; // non supported yet
  public static final short TIME_SIGNATURE = 0x58;
  public static final short KEY_SIGNATURE = 0x59;
  public static final short SEQUENCER_SPECIFIC = 0x7F;

  public static final short SYSTEM_EXCLUSIVE = 0xF0;
  

    public static final byte OFF = 0;
    public static final byte ON = 127;
    public static final byte DUMMY_VALUE = 0;

}
