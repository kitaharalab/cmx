package jp.crestmuse.cmx.misc;
import java.util.regex.*;

public class ChordSymbol2 extends ChordSymbol {

  public interface ParseRule {
    Mode mode(String s);
    Seventh seventh(String s);
    String encode(Mode mode, Seventh seventh);
  }

  public static final ParseRule RULE1 = new ParseRule() {
      public Mode mode(String s) {
        if (s.equals("") || s.startsWith("M") || s.startsWith("maj"))
          return Mode.MAJ;
        else if (s.startsWith("m"))
          return Mode.MIN;
        else if (s.startsWith("aug"))
          return Mode.AUG;
        else if (s.startsWith("dim"))
          return Mode.DIM;
        else if (s.startsWith("sus") && s.endsWith("4"))
          return Mode.SUS4;
        else if (s.startsWith("sus") && s.endsWith("9"))
          return Mode.SUS9;
        else
          throw new IllegalArgumentException("Invalid chord: " + s);
      }
      public Seventh seventh(String s) {
        if (s.contains("maj7") || s.contains("M7"))
          return Seventh.MAJ_SEVENTH;
        else if (s.contains("7"))
          return Seventh.SEVENTH;
        else if (s.contains("6"))
          return Seventh.SIXTH;
        else
          return Seventh.NONE;
      }
      public String encode(Mode mode, Seventh seventh) {
        String s1;
        if (mode == Mode.MAJ)
          s1 = "";
        else if (mode == Mode.MIN)
          s1 = "m";
        else if (mode == Mode.AUG)
          s1 = "aug";
        else if (mode == Mode.DIM)
          s1 = "dim";
        else if (mode == Mode.SUS4)
          s1 = "sus4";
        else if (mode == Mode.SUS9)
          s1 = "sus9";
        else
          throw new IllegalArgumentException("Invalid chord: " + mode);
        String s2;
        if (seventh == Seventh.NONE)
          s2 = "";
        else if (seventh == Seventh.SIXTH)
          s2 = "6";
        else if (seventh == Seventh.SEVENTH)
          s2 = "7";
        else if (seventh == Seventh.MAJ_SEVENTH)
          s2 = "maj7";
        else
          throw new IllegalArgumentException("Invalid chord: " + seventh);
        return s1 + s2;
      }
        
    };

  private static Pattern p1 = 
    Pattern.compile("([CDEFGAB])(|\\#|b)([^\\#b]*)");
  private static Pattern p2 = 
    Pattern.compile("([CDEFGAB])(|\\#|b)");


  public ChordSymbol2(NoteSymbol root, Mode mode, Seventh seventh) {
    super(root, mode, seventh);
  }

  public ChordSymbol2(NoteSymbol root, Mode mode, Seventh seventh, 
                      NoteSymbol bass) {
    super(root, mode, seventh, bass);
  }

  public static ChordSymbol2 parse(NoteSymbol root, String kind, 
                                   NoteSymbol bass, ParseRule r) {
    Mode mode = r.mode(kind);
    Seventh seventh = r.seventh(kind);
    return new ChordSymbol2(root, mode, seventh, bass);
  }

  public static ChordSymbol2 parse(String s) {
    return parse(s, RULE1);
  }

  public static ChordSymbol2 parse(String s, ParseRule r) {
    String[] ss = s.trim().split("/");
    Matcher m1 = p1.matcher(ss[0]);
    if (m1.matches()) {
      NoteSymbol bass = null;
      if (ss.length >= 2) {
        Matcher m2 = p2.matcher(ss[1]);
        if (m2.matches())
          bass = new NoteSymbol(m2.group(1), m2.group(2));
        else
          throw new IllegalArgumentException("Invalid chord symbol: " + s);
      }
      NoteSymbol root = new NoteSymbol(m1.group(1), m1.group(2));
      Mode mode = r.mode(m1.group(3));
      Seventh seventh = r.seventh(m1.group(3));
      return new ChordSymbol2(root, mode, seventh, bass);
    } else {
      throw new IllegalArgumentException("Invalid chord symbol: " + s);
    }
  }

  public String encode() {
    return encode(RULE1);
  }
    
  public String encode(ParseRule r) {
    return root.encode() + r.encode(mode, seventh) 
      + (bass == null ? "" : ("/" + bass.encode()));
  }

  public String toString() {
    return encode(RULE1);
  }
  
}
