package jp.crestmuse.cmx.misc;

import java.util.*;
import java.util.regex.*;

public final class NoteSymbol {
  
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
    
    private static final Sign[] LIST_SHARP = 
      {NONE, SHARP, NONE, SHARP, NONE, NONE, SHARP, NONE, 
       SHARP, NONE, SHARP, NONE};
    
    private static final Sign[] LIST_FLAT = 
      {NONE, FLAT, NONE, FLAT, NONE, NONE, FLAT, NONE, 
       FLAT, NONE, FLAT, NONE};

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

  public NoteSymbol(NoteName notename, Sign sign) {
    this.notename = notename;
    this.sign = sign;
  }

  public NoteSymbol(int num, boolean sharp) {
    if (sharp) {
      this.notename = NoteName.LIST_SHARP[num];
      this.sign = Sign.LIST_SHARP[num];
    } else {
      this.notename = NoteName.LIST_FLAT[num];
      this.sign = Sign.LIST_FLAT[num];
    }
  }

  public NoteSymbol(String notename, String sign) {
    this(parseNoteName(notename), parseSign(sign));
  }

  public NoteSymbol(String notename, int alter) {
    this.notename = parseNoteName(notename);
    if (alter == 0)
      sign = Sign.NONE;
    else if (alter > 0)
      sign = Sign.SHARP;
    else
      sign = Sign.FLAT;
  }

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


  public boolean equals(Object o) {
    if (o != null && o instanceof NoteSymbol) {
      NoteSymbol a = (NoteSymbol)o;
      return notename.equals(a.notename) && sign.equals(a.sign);
    } else {
      return false;
    }
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
}