package jp.crestmuse.cmx.misc;
import java.util.*;
import java.util.regex.*;

public final class ChordSymbol {
  public static enum Root {C(0), D(2), E(4), F(5), G(7), A(9), B(11);
    private static final Root[] LIST_SHARP = 
      {C, C, D, D, E, F, F, G, G, A, A, B};
    private static final Root[] LIST_FLAT = 
      {C, D, D, E, E, F, G, G, A, A, B, B};
    private int value;
    Root(int n) {
      value = n;
    }
  }
  public static enum Sign {NONE(0), SHARP(1), FLAT(-1);
    private static final Sign[] LIST_SHARP = 
      {NONE, SHARP, NONE, SHARP, NONE, NONE, SHARP, NONE, SHARP, NONE, 
       SHARP, NONE};
    private static final Sign[] LIST_FLAT = 
      {NONE, FLAT, NONE, FLAT, NONE, NONE, FLAT, NONE, FLAT, NONE, 
       FLAT, NONE};
    private int value;
    static Map<String,String[]> strings = new HashMap<String,String[]>();
    Sign(int n) {
      value = n;
    }
    public String toString(String key) {
      return strings.get(key)[ordinal()];
    }
  }
  public static enum Mode {MAJ, MIN, AUG, DIM;
    static Map<String,String[]> strings = new HashMap<String,String[]>();
    public String toString(String key) {
      return strings.get(key)[ordinal()];
    }
  }
  public static enum Seventh {NONE, DOM7, MAJ7;
    static Map<String,String[]> strings = new HashMap<String,String[]>();
    public String toString(String key) {
      return strings.get(key)[ordinal()];
    }
  }

  private Root root;
  private Sign sign;
  private Mode mode;
  private Seventh seventh;
  
  private static Pattern p = 
    Pattern.compile("([CDEFGAB])(|\\#|b)(|M|maj|major|m|min|minor|aug|augument|augumented|dim|diminish|diminished)(|7|M7|maj7|major7)");
  
  static {
    addStringMap("default", new String[]{"", "#", "b"}, 
		 new String[]{"", "m", "aug", "dim"}, 
		 new String[]{"", "7", "maj7"});
  }
  
  public ChordSymbol(Root root, Sign sign, Mode mode, Seventh seventh) {
    this.root = root;
    this.sign = sign;
    this.mode = mode;
    this.seventh = seventh;
  }
  
  public ChordSymbol(int num, Mode mode, Seventh seventh, 
                     boolean sharp) {
    if (sharp) {
      this.root = Root.LIST_SHARP[num];
      this.sign = Sign.LIST_SHARP[num];
      this.mode = mode;
      this.seventh = seventh;
    } else {
      this.root = Root.LIST_FLAT[num];
      this.sign = Sign.LIST_FLAT[num];
      this.mode = mode;
      this.seventh = seventh;
    }
  }
    

  public static ChordSymbol[] getChordSymbolList(String[] cn) {
    ChordSymbol[] cs = new ChordSymbol[cn.length];
    for (int i = 0; i < cs.length; i++) {
      cs[i] = parse(cn[i]);
    }
    return cs;
  }
  
  public static ChordSymbol parse(String s) {
    return parse(s, false);
  }
  
  public static ChordSymbol parse(String s, boolean seventhIgnored) {
    s = s.trim();
    Matcher m = p.matcher(s);
    if (m.matches())
      return new ChordSymbol(parseRoot(m.group(1)), 
			     parseSign(m.group(2)),
			     parseMode(m.group(3)), 
			     seventhIgnored ? Seventh.NONE 
			     : parseSeventh(m.group(4), m.group(3)));
    else
      throw new IllegalStateException("Invalid chord symbol: " + s);
  }

  public static void addStringMap(String name, 
				  String[] sign, String[] mode, 
				  String[] seventh) {
    Sign.strings.put(name, sign);
    Mode.strings.put(name, mode);
    Seventh.strings.put(name, seventh);
  }
  
  public String encode() {
    return encode("default", false);
  }
  
  public String encode(String name, boolean seventhIgnored) {
    return root.toString() + sign.toString(name)
      + mode.toString(name) 
      + (seventhIgnored ? "" : seventh.toString(name));
  }

  public static String[] encodeAll(ChordSymbol[] cs) {
    String[] ss = new String[cs.length];
    for (int i = 0; i < ss.length; i++) {
      ss[i] = cs[i].encode();
    }
    return ss;
  }

  public static String[] encodeAll(ChordSymbol[] cs, String name, 
				   boolean seventhIgnored) {
    String[] ss = new String[cs.length];
    for (int i = 0; i < ss.length; i++) {
      ss[i] = cs[i].encode(name, seventhIgnored);
    }
    return ss;
  }
    

  private static Root parseRoot(String s) {
    if (s.equals("C"))
      return Root.C;
    else if (s.equals("D"))
      return Root.D;
    else if (s.equals("E"))
      return Root.E;
    else if (s.equals("F"))
      return Root.F;
    else if (s.equals("G"))
      return Root.G;
    else if (s.equals("A"))
      return Root.A;
    else if (s.equals("B"))
      return Root.B;
    else
      throw new IllegalStateException("Invalid root note: " + s);
  }

  private static Sign parseSign(String s) {
    if (s.equals("")) 
      return Sign.NONE;
    else if (s.equals("#"))
      return Sign.SHARP;
    else if (s.equals("b"))
      return Sign.FLAT;
    else
      throw new IllegalStateException("Invalid sign: " + s);
  }

  private static Mode parseMode(String s) {
    if (s.equals("") || s.equals("M") || s.startsWith("maj"))
      return Mode.MAJ;
    else if (s.equals("m") || s.startsWith("min"))
      return Mode.MIN;
    else if (s.startsWith("aug"))
      return Mode.AUG;
    else if (s.startsWith("dim"))
      return Mode.DIM;
    else
      throw new IllegalStateException("Invalid mode: " + s);
  }

  private static Seventh parseSeventh(String s, String mode) {
    if (s.equals(""))
      return Seventh.NONE;
    else if (s.equals("7") && (mode.equals("M") || mode.startsWith("maj")))
      return Seventh.MAJ7;
    else if (s.equals("7"))
      return Seventh.DOM7;
    else if (s.startsWith("M7") || s.startsWith("maj"))
      return Seventh.MAJ7;
    else 
      throw new IllegalStateException("Invalid seventh: " + s);
  }

  public boolean equals(Object o) {
    if (o != null && o instanceof ChordSymbol) {
      ChordSymbol a = (ChordSymbol)o;
      return root.equals(a.root) && sign.equals(a.sign)
	&& mode.equals(a.mode) && seventh.equals(a.seventh);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return root.hashCode() + sign.hashCode() 
      +  mode.hashCode() + seventh.hashCode();
  }

  public String toString() {
    return encode();
  }

  public int getRootNoteNumBase() {
    return root.value + sign.value;
  }

  public ChordSymbol transpose(int diff, boolean sharp) {
    return new ChordSymbol((getRootNoteNumBase() + diff) % 12, 
                           mode, seventh, sharp);
  }
}