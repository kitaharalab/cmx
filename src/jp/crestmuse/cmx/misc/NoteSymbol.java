package jp.crestmuse.cmx.misc;

import java.util.*;
import java.util.regex.*;

public final class NoteSymbol implements Comparable<NoteSymbol> {

  //  public static final NoteSymbol NONE =
  //    new NoteSymbol(NoteName.NONE, Sign.NONE);

  public static final NoteSymbol C = new NoteSymbol(NoteName.C, Sign.NONE);
  public static final NoteSymbol D = new NoteSymbol(NoteName.D, Sign.NONE);
  public static final NoteSymbol E = new NoteSymbol(NoteName.E, Sign.NONE);
  public static final NoteSymbol F = new NoteSymbol(NoteName.F, Sign.NONE);
  public static final NoteSymbol G = new NoteSymbol(NoteName.G, Sign.NONE);
  public static final NoteSymbol A = new NoteSymbol(NoteName.A, Sign.NONE);
  public static final NoteSymbol B = new NoteSymbol(NoteName.B, Sign.NONE);

  public static final NoteSymbol C_SHARP = 
    new NoteSymbol(NoteName.C, Sign.SHARP);
  public static final NoteSymbol D_SHARP = 
    new NoteSymbol(NoteName.D, Sign.SHARP);
  public static final NoteSymbol E_SHARP = 
    new NoteSymbol(NoteName.E, Sign.SHARP);
  public static final NoteSymbol F_SHARP = 
    new NoteSymbol(NoteName.F, Sign.SHARP);
  public static final NoteSymbol G_SHARP = 
    new NoteSymbol(NoteName.G, Sign.SHARP);
  public static final NoteSymbol A_SHARP = 
    new NoteSymbol(NoteName.A, Sign.SHARP);
  public static final NoteSymbol B_SHARP = 
    new NoteSymbol(NoteName.B, Sign.SHARP);

  public static final NoteSymbol C_FLAT = 
    new NoteSymbol(NoteName.C, Sign.FLAT);
  public static final NoteSymbol D_FLAT = 
    new NoteSymbol(NoteName.D, Sign.FLAT);
  public static final NoteSymbol E_FLAT = 
    new NoteSymbol(NoteName.E, Sign.FLAT);
  public static final NoteSymbol F_FLAT = 
    new NoteSymbol(NoteName.F, Sign.FLAT);
  public static final NoteSymbol G_FLAT = 
    new NoteSymbol(NoteName.G, Sign.FLAT);
  public static final NoteSymbol A_FLAT = 
    new NoteSymbol(NoteName.A, Sign.FLAT);
  public static final NoteSymbol B_FLAT = 
    new NoteSymbol(NoteName.B, Sign.FLAT);

  private static final NoteSymbol[] INSTANCES_SHARP =
  {C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B};

  private static final NoteSymbol[] INSTANCES_FLAT =
  {C, D_FLAT, D, E_FLAT, E, F, G_SHARP, G, A_FLAT, A, B_FLAT, B}; 
  
  private static final Map<String,NoteSymbol> MAP =
    new HashMap<String,NoteSymbol>();

  static {
    MAP.put("Cb", C_FLAT);
    MAP.put("C", C);
    MAP.put("C#", C_SHARP);
    MAP.put("Db", D_FLAT);
    MAP.put("D", D);
    MAP.put("D#", D_SHARP);
    MAP.put("Eb", E_FLAT);
    MAP.put("E", E);
    MAP.put("E#", E_SHARP);
    MAP.put("Fb", F_FLAT);
    MAP.put("F", F);
    MAP.put("F#", F_SHARP);
    MAP.put("Gb", G_FLAT);
    MAP.put("G", G);
    MAP.put("G#", G_SHARP);
    MAP.put("Ab", A_FLAT);
    MAP.put("A", A);
    MAP.put("A#", A_SHARP);
    MAP.put("Bb", B_FLAT);
    MAP.put("B", B);
    MAP.put("B#", B_SHARP);
  }
  
  public static enum NoteName {
    C(0), D(2), E(4), F(5), G(7), A(9), B(11);

    private static final NoteName[] LIST_SHARP = 
      {C, C, D, D, E, F, F, G, G, A, A, B};

    private static final NoteName[] LIST_FLAT = 
      {C, D, D, E, E, F, G, G, A, A, B, B};

    private int value;

    NoteName(int n) {
      value = n;
    }
  }

  public static enum Sign {
    NONE(0, ""), SHARP(1, "#"), FLAT(-1, "b");
    
    //    private static final Sign[] LIST_SHARP = 
    //      {NONE, SHARP, NONE, SHARP, NONE, NONE, SHARP, NONE, 
    //       SHARP, NONE, SHARP, NONE};
    
    //    private static final Sign[] LIST_FLAT = 
    //      {NONE, FLAT, NONE, FLAT, NONE, NONE, FLAT, NONE, 
    //       FLAT, NONE, FLAT, NONE};

    private final int value;
    private final String s;

//    static Map<String,String[]> strings = 
//      new HashMap<String,String[]>();
    
    Sign(int n, String s) {
      value = n;
      this.s = s;
    }

    public String toString() {
      return s;
    }
  }

  private final NoteName notename;
  private final Sign sign;

  private NoteSymbol(NoteName notename, Sign sign) {
    this.notename = notename;
    this.sign = sign;
  }

  public static final NoteSymbol getInstance(int num, boolean sharp) {
    if (sharp)
      return INSTANCES_SHARP[num];
    else
      return INSTANCES_FLAT[num];
  }
  
  /*
  public NoteSymbol(int num, boolean sharp) {
    if (sharp) {
      this.notename = NoteName.LIST_SHARP[num];
      this.sign = Sign.LIST_SHARP[num];
    } else {
      this.notename = NoteName.LIST_FLAT[num];
      this.sign = Sign.LIST_FLAT[num];
    }
  }
  */

  /*
  public NoteSymbol(String notename, String sign) {
    this(parseNoteName(notename), parseSign(sign));
  }
  */

  public static NoteSymbol getInstance(String s, int alter) {
    NoteSymbol note = getInstance(s);
    return getInstance((note.number() + alter) % 12, alter >= 0);
  }
  
  /*
  public NoteSymbol(String notename, int alter) {
    this.notename = parseNoteName(notename);
    if (alter == 0)
      sign = Sign.NONE;
    else if (alter > 0)
      sign = Sign.SHARP;
    else
      sign = Sign.FLAT;
  }
  */

  public static NoteSymbol getInstance(String s) {
    return MAP.get(s);
  }

  /*
  private static NoteName parseNoteName(String s) {
    if (s.equals("C"))
      return NoteName.C;
    else if (s.equals("D"))
      return NoteName.D;
    else if (s.equals("E"))
      return NoteName.E;
    else if (s.equals("F"))
      return NoteName.F;
    else if (s.equals("G"))
      return NoteName.G;
    else if (s.equals("A"))
      return NoteName.A;
    else if (s.equals("B"))
      return NoteName.B;
    else
      throw new IllegalArgumentException("Invalid note name: " + s);
  }
  */

  /*
  private static Sign parseSign(String s) {
    if(s.equals(""))
      return Sign.NONE;
    else if (s.equals("#"))
      return Sign.SHARP;
    else if (s.equals("b"))
      return Sign.FLAT;
    else
      throw new IllegalArgumentException("Invalid note sign: " + s);
  }
  */

  public boolean equals(Object o) {
    if (o != null && o instanceof NoteSymbol) {
      NoteSymbol a = (NoteSymbol)o;
      return notename.equals(a.notename) && sign.equals(a.sign);
    } else {
      return false;
    }
  }

  public boolean equals(Object o, boolean ignoresSharpFloat) {
    if (o != null && o instanceof NoteSymbol) 
      return number() == ((NoteSymbol)o).number();
    else
      return equals(o);
  }

  public int hashCode() {
    return notename.hashCode() + sign.hashCode();
  }

  public String toString() {
    return encode();
  }

  public String encode() {
    return notename.toString() + sign.toString();
  }

  public int number() {
    return notename.value + sign.value;
  }

  public NoteSymbol transpose(int diff) {
    return getInstance((number() + diff) % 12, !sign.equals(Sign.FLAT));
  }

  /** Note: Notes "C#" and "Db" are considered the same. */
  public int compareTo(NoteSymbol another) {
    return number() - another.number();
  }
}
