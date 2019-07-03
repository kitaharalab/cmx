package jp.crestmuse.cmx.misc;
import java.util.regex.*;
import static jp.crestmuse.cmx.misc.NoteSymbol.*;

public class KeySymbol {
  public static enum Mode {MAJ,MIN};
  public static NoteSymbol[] ROOT_SHARP_MAJ =
  {C, G, D, A, E, B, F_SHARP, C_SHARP, G_SHARP, D_SHARP, A_SHARP, F};
  public static NoteSymbol[] ROOT_FLAT_MAJ =
  {C, F, B_FLAT, E_FLAT, A_FLAT, D_FLAT, G_FLAT, B, E, A, D, G};
  public static NoteSymbol[] ROOT_SHARP_MIN =
  {A, E, B, F_SHARP, C_SHARP, G_SHARP, D_SHARP, A_SHARP, F, C, G, D};
  public static NoteSymbol[] ROOT_FLAT_MIN =
  {A, D, G, C, F, B_FLAT, E_FLAT, A_FLAT, D_FLAT, G_FLAT, B, E};
  

  NoteSymbol root;
  Mode mode;

  private KeySymbol(NoteSymbol root, Mode mode) {
    this.root = root;
    this.mode = mode;
  }
  
  public NoteSymbol root() {
    return root;
  }

  public Mode mode() {
    return mode;
  }

  public String encode() {
    return root.encode() + (mode.equals(Mode.MIN) ? "m" : "");
  }
      
  private static Pattern p =
    Pattern.compile("([CDEFGAB])(|\\#|b)\\-?(|M|m|maj|min|dor|moll)");
  
  
  public static KeySymbol parse(String s) {
    String[] ss = s.trim().split(" ");
    try {
      int n =
        (ss[0].startsWith("+") ?
         Integer.parseInt(ss[0].substring(1)) : Integer.parseInt(ss[0]));  
      return fromInteger(n, !ss[1].toLowerCase().startsWith("min"));
    } catch (NumberFormatException e) {
      Matcher m = p.matcher(s);
      if (m.matches()) {
        NoteSymbol root = NoteSymbol.getInstance(m.group(1) + m.group(2));
        Mode mode = ((m.group(3).equals("m") || m.group(3).equals("min")
                      || m.group(3).equals("moll"))
                     ? Mode.MIN : Mode.MAJ);
        return new KeySymbol(root, mode);
      } else {
        throw new IllegalArgumentException(s + ": invalid key string");
      }
    }
  }
    
  public static KeySymbol fromInteger(int n, boolean maj) {
    n = n % 12;
    NoteSymbol root =
      (maj ? (n >= 0 ? ROOT_SHARP_MAJ[n] : ROOT_FLAT_MAJ[-n]) :
       (n >= 0 ? ROOT_SHARP_MIN[n] : ROOT_FLAT_MIN[-n]));
    Mode mode = (maj ? Mode.MAJ : Mode.MIN);
    return new KeySymbol(root, mode);
  }

  public int toInteger() {
    if (mode.equals(Mode.MAJ)) {
      for (int i = 0; i < ROOT_SHARP_MAJ.length; i++) {
        if (ROOT_SHARP_MAJ[i].equals(root))
          return i;
        else if (ROOT_FLAT_MAJ[i].equals(root))
          return -i;
      }
      throw new IllegalStateException(root + ": unsupported");
    } else {
      for (int i = 0; i < ROOT_SHARP_MIN.length; i++) {
        if (ROOT_SHARP_MIN[i].equals(root))
          return i;
        else if (ROOT_FLAT_MIN[i].equals(root))
          return -i;
      }
      throw new IllegalStateException(root + ": unsupported");
    }
  }

  public KeySymbol transpose(int diff) {
    if (diff == 0) {
      return this;
    } else if (diff > 0) {
      int fifth = 0;
      while (fifth * 7 % 12 != diff % 12) {
        fifth++;
      }
      return transposeByFifth(fifth);
    } else {
      int fifth = 0;
      while (fifth * 7 % 12 != diff % 12) {
        fifth--;
      }
      return transposeByFifth(fifth);
    }
  }

  public KeySymbol transposeByFifth(int diff) {
    return KeySymbol.fromInteger(toInteger() + diff,
                                         mode.equals(Mode.MAJ));
  }

  public boolean equals(Object another) {
    if (another != null && another instanceof KeySymbol) {
      KeySymbol a = (KeySymbol)another;
      return root.equals(a.root) && mode.equals(a.mode);
    } else {
      return false;
    }
  }

  public String toString() {
    return encode();
  }
        
}
