package jp.crestmuse.cmx.misc;
import java.util.regex.*;
import java.util.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.misc.Misc.*;

public class ChordSymbol2 extends ChordSymbol implements Cloneable {

  public static enum ExtendedNote {
    NONE, NATURAL, SHARP, FLAT;
  }

  ExtendedNote ninth = ExtendedNote.NONE;
  ExtendedNote eleventh = ExtendedNote.NONE;
  ExtendedNote thirteenth = ExtendedNote.NONE;
  ExtendedNote fifth = ExtendedNote.NATURAL;

  public interface ParseRule {
    Mode mode(String s);
    Seventh seventh(String s);
    ExtendedNote ninth(String s);
    ExtendedNote eleventh(String s);
    ExtendedNote thirteenth(String s);
    ExtendedNote fifth(String s);
    String encode(Mode mode, Seventh seventh, ExtendedNote ninth, 
                  ExtendedNote eleventh, ExtendedNote thirteenth, 
                  ExtendedNote fifth);
  }

  public static final ParseRule RULE1 = new ParseRule() {
      public Mode mode(String s) {
//        System.err.print(s + "\t");
        if (s.equals("") || s.startsWith("M") || 
            s.toLowerCase().startsWith("maj"))
          return Mode.MAJ;
        else if (s.startsWith("m"))
          return Mode.MIN;
        else if (s.toLowerCase().startsWith("aug"))
          return Mode.AUG;
        else if (s.toLowerCase().startsWith("dim"))
          return Mode.DIM;
        else if (s.toLowerCase().contains("sus") && s.endsWith("9"))
          return Mode.SUS9;
        else if (s.toLowerCase().contains("sus"))
          return Mode.SUS4;
        else
          return Mode.MAJ;
//          throw new IllegalArgumentException("Invalid chord: " + s);
      }

      public Seventh seventh(String s) {
        if (s.toLowerCase().contains("maj7") || s.contains("M7") || 
            s.toLowerCase().contains("maj9") || s.contains("M9") || 
            s.toLowerCase().contains("maj11") || s.contains("M11") || 
            s.toLowerCase().contains("maj13") || s.contains("M13"))
          return Seventh.MAJ_SEVENTH;
        else if (s.contains("7") || s.contains("9") || s.contains("11") || 
                 s.contains("13"))
          return Seventh.SEVENTH;
        else if (s.contains("6"))
          return Seventh.SIXTH;
        else
          return Seventh.NONE;
      }

      public ExtendedNote ninth(String s) {
        if (s.contains("#9") || s.contains("+9"))
          return ExtendedNote.SHARP;
        else if (s.contains("b9") || s.contains("-9"))
          return ExtendedNote.FLAT;
        else if (s.contains("9"))
          return ExtendedNote.NATURAL;
        else
          return ExtendedNote.NONE;
      }

      public ExtendedNote eleventh(String s) {
        if (s.contains("#11") || s.contains("+11"))
          return ExtendedNote.SHARP;
        else if (s.contains("b11") || s.contains("-11"))
          return ExtendedNote.FLAT;
        else if (s.contains("11"))
          return ExtendedNote.NATURAL;
        else
          return ExtendedNote.NONE;
      }

      public ExtendedNote thirteenth(String s) {
        if (s.contains("#13") || s.contains("+13"))
          return ExtendedNote.SHARP;
        else if (s.contains("b13") || s.contains("-13"))
          return ExtendedNote.FLAT;
        else if (s.contains("13"))
          return ExtendedNote.NATURAL;
        else
          return ExtendedNote.NONE;
      }

      public ExtendedNote fifth(String s) {
        if (s.contains("#5") || s.contains("+5"))
          return ExtendedNote.SHARP;
        else if (s.contains("b5") || s.contains("-5"))
          return ExtendedNote.FLAT;
        else
          return ExtendedNote.NATURAL;
      }

      private Map<Mode,String> modeString = createMap(
        new Mode[] 
        {Mode.MAJ, Mode.MIN, Mode.AUG, Mode.DIM, Mode.SUS4, Mode.SUS9}, 
        new String[]{"", "m", "aug", "dim", "sus4", "sus9"}
      );
      private Map<Seventh,String> seventhString = createMap(
        new Seventh[]
        {Seventh.NONE, Seventh.SIXTH, Seventh.SEVENTH, Seventh.MAJ_SEVENTH}, 
        new String[]{"", "6", "7", "maj7"}
      );
      private Map<ExtendedNote,String> ninthString = createMap(
        new ExtendedNote[] {ExtendedNote.NONE, ExtendedNote.NATURAL, 
                            ExtendedNote.SHARP, ExtendedNote.FLAT}, 
        new String[]{"", "9", "#9", "b9"}
      );
      private Map<ExtendedNote,String> eleventhString = createMap(
        new ExtendedNote[] {ExtendedNote.NONE, ExtendedNote.NATURAL, 
                            ExtendedNote.SHARP, ExtendedNote.FLAT}, 
        new String[]{"", "11", "#11", "b11"}
      );
      private Map<ExtendedNote,String> thirteenthString = createMap(
        new ExtendedNote[] {ExtendedNote.NONE, ExtendedNote.NATURAL, 
                            ExtendedNote.SHARP, ExtendedNote.FLAT}, 
        new String[]{"", "13", "#13", "b13"}
      );
      private Map<ExtendedNote,String> fifthString = createMap(
        new ExtendedNote[] {ExtendedNote.NONE, ExtendedNote.NATURAL, 
                            ExtendedNote.SHARP, ExtendedNote.FLAT}, 
        new String[]{"", "", "#5", "b5"}
      );

      public String encode(Mode mode, Seventh seventh, ExtendedNote ninth, 
                           ExtendedNote eleventh, ExtendedNote thirteenth, 
                           ExtendedNote fifth) {
        String s1 = modeString.get(mode);
        String s2 = seventhString.get(seventh);
        String s3 = joinStrings(",", new String[]{
            fifthString.get(fifth), 
            ninthString.get(ninth), 
            eleventhString.get(eleventh), 
            thirteenthString.get(thirteenth)});
        if (s3.length() > 0)
          return s1 + s2 + "(" + s3 + ")";
        else
          return s1 + s2;
/*
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
        String s3;

        return s1 + s2;
*/
      }
        
    };

  private static Pattern p1 = 
    Pattern.compile("([CDEFGAB])(|\\#|b)(([^\\#b].*)?)");
//    Pattern.compile("([CDEFGAB])(|\\#|b)(.*?)");
//    Pattern.compile("([CDEFGAB])(|\\#|b)([^\\#b]*)");
  private static Pattern p2 = 
    Pattern.compile("([CDEFGAB])(|\\#|b)");


  public ChordSymbol2(NoteSymbol root, Mode mode, Seventh seventh) {
    super(root, mode, seventh);
  }

/*
  public ChordSymbol2(NoteSymbol root, Mode mode, Seventh seventh, 
                      NoteSymbol bass) {
    super(root, mode, seventh, bass);
  }
*/

  public static ChordSymbol2 parse(NoteSymbol root, String kind, 
                                   NoteSymbol bass, ParseRule r) {
    Mode mode = r.mode(kind);
    Seventh seventh = r.seventh(kind);
    ChordSymbol2 c = new ChordSymbol2(root, mode, seventh);
    c.bass = bass;
    c.ninth = r.ninth(kind);
    c.eleventh = r.eleventh(kind);
    c.thirteenth = r.thirteenth(kind);
    c.fifth = r.fifth(kind);
    return c;
  }

  public static ChordSymbol2 parse(String s) {
//    System.err.print("chord:\t" + s + "\t");
    ChordSymbol2 c = parse(s, RULE1);
//    System.err.println(c);
    return c;
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
          throw new InvalidChordSymbolException("Invalid chord symbol: " + s);
//          throw new IllegalArgumentException("Invalid chord symbol: " + s);
      }
      NoteSymbol root = new NoteSymbol(m1.group(1), m1.group(2));
      return parse(root, m1.group(3), bass, r);
//      Mode mode = r.mode(m1.group(3));
//      Seventh seventh = r.seventh(m1.group(3));
//      ChordSymbol2 c = new ChordSymbol2(root, mode, seventh);
//      c.bass = bass;
//      c.ninth = r.ninth(m1.group(3));
//      c.eleventh = r.eleventh(m1.group(3));
//      c.thirteenth = r.thirteenth(m1.group(3));
//      c.fifth = r.fifth(m1.group(3));
//      return c;
    } else {
          throw new InvalidChordSymbolException("Invalid chord symbol: " + s);
//      throw new IllegalArgumentException("Invalid chord symbol: " + s);
    }
  }

  public String encode() {
    return encode(RULE1);
  }
    
  public String encode(ParseRule r) {
    return root.encode() 
      + r.encode(mode, seventh, ninth, eleventh, thirteenth, fifth) 
      + (bass == null ? "" : ("/" + bass.encode()));
  }

  public String toString() {
    return encode(RULE1);
  }

  public ChordSymbol2 clone() throws CloneNotSupportedException {
    return (ChordSymbol2)super.clone();
  }

  public ChordSymbol2 transpose(int diff, boolean sharp) {
    try {
      ChordSymbol2 c = clone();
      int newroot = (root.number() + diff) % 12;
      if (newroot < 0)
        newroot += 12;
      c.root = new NoteSymbol(newroot, sharp);
      if (c.bass != null) {
        int newbass = (bass.number() + diff) % 12;
        if (newbass < 0)
          newbass += 12;
        c.bass = new NoteSymbol(newbass, sharp);
      }
      return c;
    } catch (CloneNotSupportedException e) {
      throw new Error();
    }
  }
    

/*
  public ChordSymbol2 transpose(int diff, boolean sharp) {
    int newnumber = (root.number() + diff) % 12;
    if (newnumber < 0)
      newnumber += 12;
    ChordSymbol2 c = new ChordSymbol2(new NoteSymbol(newnumber, sharp), 
                                      mode, seventh);
  }
*/
}
