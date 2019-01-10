package jp.crestmuse.cmx.misc;
import java.util.regex.*;
import java.util.*;
import jp.crestmuse.cmx.misc.*;
import static jp.crestmuse.cmx.misc.Misc.*;

public class ChordSymbol2 extends ChordSymbol implements Cloneable {

  public static final ChordSymbol2 NON_CHORD =
    new ChordSymbol2(null, Mode.MAJ, Seventh.NONE);
  
  public static final ChordSymbol2 C = 
    new ChordSymbol2(NoteSymbol.C, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 D = 
    new ChordSymbol2(NoteSymbol.D, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 E  =
    new ChordSymbol2(NoteSymbol.E, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 F = 
    new ChordSymbol2(NoteSymbol.F, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 G = 
    new ChordSymbol2(NoteSymbol.G, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol A = 
    new ChordSymbol2(NoteSymbol.A, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 B = 
    new ChordSymbol2(NoteSymbol.B, Mode.MAJ, Seventh.NONE);

  public static final ChordSymbol2 C_SHARP = 
    new ChordSymbol2(NoteSymbol.C_SHARP, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 D_SHARP = 
    new ChordSymbol2(NoteSymbol.D_SHARP, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 E_SHARP = 
    new ChordSymbol2(NoteSymbol.E_SHARP, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 F_SHARP = 
    new ChordSymbol2(NoteSymbol.F_SHARP, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 G_SHARP = 
    new ChordSymbol2(NoteSymbol.G_SHARP, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 A_SHARP = 
    new ChordSymbol2(NoteSymbol.A_SHARP, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 B_SHARP = 
    new ChordSymbol2(NoteSymbol.B_SHARP, Mode.MAJ, Seventh.NONE);

  public static final ChordSymbol2 C_FLAT = 
    new ChordSymbol2(NoteSymbol.C_FLAT, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 D_FLAT = 
    new ChordSymbol2(NoteSymbol.D_FLAT, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 E_FLAT = 
    new ChordSymbol2(NoteSymbol.E_FLAT, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 F_FLAT = 
    new ChordSymbol2(NoteSymbol.F_FLAT, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 G_FLAT = 
    new ChordSymbol2(NoteSymbol.G_FLAT, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 A_FLAT = 
    new ChordSymbol2(NoteSymbol.A_FLAT, Mode.MAJ, Seventh.NONE);
  public static final ChordSymbol2 B_FLAT = 
    new ChordSymbol2(NoteSymbol.B_FLAT, Mode.MAJ, Seventh.NONE);
  
  public static final ChordSymbol2 C_MIN = 
    new ChordSymbol2(NoteSymbol.C, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 D_MIN = 
    new ChordSymbol2(NoteSymbol.D, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 E_MIN = 
    new ChordSymbol2(NoteSymbol.E, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 F_MIN = 
    new ChordSymbol2(NoteSymbol.F, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 G_MIN = 
    new ChordSymbol2(NoteSymbol.G, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 A_MIN = 
    new ChordSymbol2(NoteSymbol.A, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 B_MIN = 
    new ChordSymbol2(NoteSymbol.B, Mode.MIN, Seventh.NONE);

  public static final ChordSymbol2 C_SHARP_MIN = 
    new ChordSymbol2(NoteSymbol.C_SHARP, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 D_SHARP_MIN = 
    new ChordSymbol2(NoteSymbol.D_SHARP, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 E_SHARP_MIN = 
    new ChordSymbol2(NoteSymbol.E_SHARP, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 F_SHARP_MIN = 
    new ChordSymbol2(NoteSymbol.F_SHARP, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 G_SHARP_MIN = 
    new ChordSymbol2(NoteSymbol.G_SHARP, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 A_SHARP_MIN = 
    new ChordSymbol2(NoteSymbol.A_SHARP, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 B_SHARP_MIN = 
    new ChordSymbol2(NoteSymbol.B_SHARP, Mode.MIN, Seventh.NONE);

  public static final ChordSymbol2 C_FLAT_MIN = 
    new ChordSymbol2(NoteSymbol.C_FLAT, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 D_FLAT_MIN = 
    new ChordSymbol2(NoteSymbol.D_FLAT, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 E_FLAT_MIN = 
    new ChordSymbol2(NoteSymbol.E_FLAT, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 F_FLAT_MIN = 
    new ChordSymbol2(NoteSymbol.F_FLAT, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 G_FLAT_MIN = 
    new ChordSymbol2(NoteSymbol.G_FLAT, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 A_FLAT_MIN = 
    new ChordSymbol2(NoteSymbol.A_FLAT, Mode.MIN, Seventh.NONE);
  public static final ChordSymbol2 B_FLAT_MIN = 
    new ChordSymbol2(NoteSymbol.B_FLAT, Mode.MIN, Seventh.NONE);

  public static final ChordSymbol2 C_AUG =
    new ChordSymbol2(NoteSymbol.C, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 D_AUG =
    new ChordSymbol2(NoteSymbol.D, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 E_AUG =
    new ChordSymbol2(NoteSymbol.E, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 F_AUG =
    new ChordSymbol2(NoteSymbol.F, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 G_AUG =
    new ChordSymbol2(NoteSymbol.G, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 A_AUG =
    new ChordSymbol2(NoteSymbol.A, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 B_AUG =
    new ChordSymbol2(NoteSymbol.B, Mode.AUG, Seventh.NONE);

  public static final ChordSymbol2 C_SHARP_AUG =
    new ChordSymbol2(NoteSymbol.C_SHARP, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 D_SHARP_AUG =
    new ChordSymbol2(NoteSymbol.D_SHARP, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 E_SHARP_AUG =
    new ChordSymbol2(NoteSymbol.E_SHARP, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 F_SHARP_AUG =
    new ChordSymbol2(NoteSymbol.F_SHARP, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 G_SHARP_AUG =
    new ChordSymbol2(NoteSymbol.G_SHARP, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 A_SHARP_AUG =
    new ChordSymbol2(NoteSymbol.A_SHARP, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 B_SHARP_AUG =
    new ChordSymbol2(NoteSymbol.B_SHARP, Mode.AUG, Seventh.NONE);

  public static final ChordSymbol2 C_FLAT_AUG =
    new ChordSymbol2(NoteSymbol.C_FLAT, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 D_FLAT_AUG =
    new ChordSymbol2(NoteSymbol.D_FLAT, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 E_FLAT_AUG =
    new ChordSymbol2(NoteSymbol.E_FLAT, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 F_FLAT_AUG =
    new ChordSymbol2(NoteSymbol.F_FLAT, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 G_FLAT_AUG =
    new ChordSymbol2(NoteSymbol.G_FLAT, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 A_FLAT_AUG =
    new ChordSymbol2(NoteSymbol.A_FLAT, Mode.AUG, Seventh.NONE);
  public static final ChordSymbol2 B_FLAT_AUG =
    new ChordSymbol2(NoteSymbol.B_FLAT, Mode.AUG, Seventh.NONE);

  public static final ChordSymbol2 C_DIM =
    new ChordSymbol2(NoteSymbol.C, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 D_DIM =
    new ChordSymbol2(NoteSymbol.D, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 E_DIM =
    new ChordSymbol2(NoteSymbol.E, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 F_DIM =
    new ChordSymbol2(NoteSymbol.F, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 G_DIM =
    new ChordSymbol2(NoteSymbol.G, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 A_DIM =
    new ChordSymbol2(NoteSymbol.A, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 B_DIM =
    new ChordSymbol2(NoteSymbol.B, Mode.DIM, Seventh.NONE);

  public static final ChordSymbol2 C_SHARP_DIM =
    new ChordSymbol2(NoteSymbol.C_SHARP, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 D_SHARP_DIM =
    new ChordSymbol2(NoteSymbol.D_SHARP, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 E_SHARP_DIM =
    new ChordSymbol2(NoteSymbol.E_SHARP, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 F_SHARP_DIM =
    new ChordSymbol2(NoteSymbol.F_SHARP, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 G_SHARP_DIM =
    new ChordSymbol2(NoteSymbol.G_SHARP, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 A_SHARP_DIM =
    new ChordSymbol2(NoteSymbol.A_SHARP, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 B_SHARP_DIM =
    new ChordSymbol2(NoteSymbol.B_SHARP, Mode.DIM, Seventh.NONE);
  
  public static final ChordSymbol2 C_FLAT_DIM =
    new ChordSymbol2(NoteSymbol.C_FLAT, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 D_FLAT_DIM =
    new ChordSymbol2(NoteSymbol.D_FLAT, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 E_FLAT_DIM =
    new ChordSymbol2(NoteSymbol.E_FLAT, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 F_FLAT_DIM =
    new ChordSymbol2(NoteSymbol.F_FLAT, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 G_FLAT_DIM =
    new ChordSymbol2(NoteSymbol.G_FLAT, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 A_FLAT_DIM =
    new ChordSymbol2(NoteSymbol.A_FLAT, Mode.DIM, Seventh.NONE);
  public static final ChordSymbol2 B_FLAT_DIM =
    new ChordSymbol2(NoteSymbol.B_FLAT, Mode.DIM, Seventh.NONE);
  

  

  public static enum ExtendedNote {
    NONE, NATURAL, SHARP, FLAT;
  }

  ExtendedNote ninth = ExtendedNote.NONE;
  ExtendedNote eleventh = ExtendedNote.NONE;
  ExtendedNote thirteenth = ExtendedNote.NONE;
  ExtendedNote fifth = ExtendedNote.NATURAL;

  public interface ParseRule {
    Mode mode(String s, Object o);
    Seventh seventh(String s, Object o);
    ExtendedNote ninth(String s, Object o);
    ExtendedNote eleventh(String s, Object o);
    ExtendedNote thirteenth(String s, Object o);
    ExtendedNote fifth(String s, Object o);
    String encode(Mode mode, Seventh seventh, ExtendedNote ninth, 
                  ExtendedNote eleventh, ExtendedNote thirteenth, 
                  ExtendedNote fifth);
  }

  public static final ParseRule RULE1 = new ParseRule() {
      public Mode mode(String s, Object o) {
        if (s.equals("") || s.startsWith("M") 
            || s.toLowerCase().startsWith("maj")
            || s.startsWith("7") || s.startsWith("9") || s.startsWith("11")
            || s.startsWith("13") || s.startsWith("6"))
          return Mode.MAJ;
        else if (s.startsWith("m"))
          return Mode.MIN;
        else if (s.toLowerCase().startsWith("aug"))
          return Mode.AUG;
        else if (s.toLowerCase().startsWith("dim"))
          return Mode.DIM;
        else if (s.toLowerCase().contains("sus") && s.endsWith("9"))
          return Mode.SUS9;
        else if (s.toLowerCase().contains("sus") && s.endsWith("2"))
          return Mode.SUS2;
        else if (s.toLowerCase().contains("sus") && s.endsWith("4"))
          return Mode.SUS4;
        //        else if (s.startsWith("sus"))
        //          return Mode.SUS4;
        else
          return Mode.MAJ;
//          throw new IllegalArgumentException("Invalid chord: " + s);
      }

      public Seventh seventh(String s, Object o) {
        if (s.toLowerCase().contains("maj7") || s.contains("M7") || 
            s.toLowerCase().contains("maj9") || s.contains("M9") || 
            s.toLowerCase().contains("maj11") || s.contains("M11") || 
            s.toLowerCase().contains("maj13") || s.contains("M13"))
          return Seventh.MAJ_SEVENTH;
        //else if (s.contains("7") || s.contains("9") || s.contains("11") || 
        //         s.contains("13"))
        else if (s.contains("7"))
          return Seventh.SEVENTH;
        else if (s.contains("6"))
          return Seventh.SIXTH;
        else
          return Seventh.NONE;
      }

      public ExtendedNote ninth(String s, Object o) {
        if (s.contains("#9") || s.contains("+9"))
          return ExtendedNote.SHARP;
        else if (s.contains("b9") || s.contains("-9"))
          return ExtendedNote.FLAT;
        else if (s.contains("9"))
          return ExtendedNote.NATURAL;
        else
          return ExtendedNote.NONE;
      }

      public ExtendedNote eleventh(String s, Object o) {
        if (s.contains("#11") || s.contains("+11"))
          return ExtendedNote.SHARP;
        else if (s.contains("b11") || s.contains("-11"))
          return ExtendedNote.FLAT;
        else if (s.contains("11"))
          return ExtendedNote.NATURAL;
        else
          return ExtendedNote.NONE;
      }

      public ExtendedNote thirteenth(String s, Object o) {
        if (s.contains("#13") || s.contains("+13"))
          return ExtendedNote.SHARP;
        else if (s.contains("b13") || s.contains("-13"))
          return ExtendedNote.FLAT;
        else if (s.contains("13"))
          return ExtendedNote.NATURAL;
        else
          return ExtendedNote.NONE;
      }

      public ExtendedNote fifth(String s, Object o) {
        if (s.contains("#5") || s.contains("+5"))
          return ExtendedNote.SHARP;
        else if (s.contains("b5") || s.contains("-5"))
          return ExtendedNote.FLAT;
        else
          return ExtendedNote.NATURAL;
      }

      private Map<Mode,String> modeString = createMap(
        new Mode[] 
        {Mode.MAJ, Mode.MIN, Mode.AUG, Mode.DIM, Mode.SUS2,
         Mode.SUS4, Mode.SUS9}, 
        new String[]{"", "m", "aug", "dim", "sus2", "sus4", "sus9"}
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
                                   NoteSymbol bass, Object option,
                                   ParseRule r) {
    Mode mode = r.mode(kind, option);
    Seventh seventh = r.seventh(kind, option);
    ChordSymbol2 c = new ChordSymbol2(root, mode, seventh);
    c.bass = bass;
    c.ninth = r.ninth(kind, option);
    c.eleventh = r.eleventh(kind, option);
    c.thirteenth = r.thirteenth(kind, option);
    c.fifth = r.fifth(kind, option);
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
    if (ss.length < 1) 
      return NON_CHORD;
    Matcher m1 = p1.matcher(ss[0]);
    if (m1.matches()) {
      NoteSymbol bass = null;
      if (ss.length >= 2) {
        Matcher m2 = p2.matcher(ss[1]);
        if (m2.matches())
          bass = NoteSymbol.getInstance(m2.group(1) + m2.group(2));
          //bass = new NoteSymbol(m2.group(1), m2.group(2));
        else
          throw new InvalidChordSymbolException("Invalid chord symbol: " + s);
//          throw new IllegalArgumentException("Invalid chord symbol: " + s);
      }
      NoteSymbol root = NoteSymbol.getInstance(m1.group(1) + m1.group(2));
      return parse(root, m1.group(3), bass, null, r);
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
    if (root != null) 
      return root.encode() 
        + r.encode(mode, seventh, ninth, eleventh, thirteenth, fifth) 
        + (bass == null ? "" : ("/" + bass.encode()));
    else
      return "/";
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
      if (root != null) {
        int newroot = (root.number() + diff) % 12;
        if (newroot < 0)
          newroot += 12;
        c.root = NoteSymbol.getInstance(newroot, sharp);
        if (c.bass != null) {
          int newbass = (bass.number() + diff) % 12;
          if (newbass < 0)
            newbass += 12;
          c.bass = NoteSymbol.getInstance(newbass, sharp);
        }
      }
      return c;
    } catch (CloneNotSupportedException e) {
      throw new Error();
    }
  }

  public boolean equals(Object another) {
    if (another instanceof ChordSymbol2) {
      //      try {
        ChordSymbol2 c = (ChordSymbol2)another;
        boolean equalRoort = root == null ? root == c.root : root.equals(c.root);
        return equalRoort && (mode.equals(c.mode)) && 
          ((bass == null && c.bass == null) || bass.equals(c.bass)) &&
          (seventh.equals(c.seventh)) && (ninth.equals(c.ninth)) &&
          (eleventh.equals(c.eleventh)) && (thirteenth.equals(c.thirteenth));
        //      } catch (NullPointerException e) {
        //        return false;     // 暫定
        //      }
    } else {
      return false;
    }
  }
        
  

  public boolean match(Object another, boolean root, boolean mode, 
                       boolean bass, boolean seventh, boolean ninth, 
                       boolean eleventh, boolean thirteenth, 
                       boolean ignoresSharpFlat) {
    if (another instanceof ChordSymbol2) {
      ChordSymbol2 c = (ChordSymbol2)another;
      return (root ? this.root.equals(c.root, ignoresSharpFlat) : true) && 
        (mode ? this.mode.equals(c.mode) : true) && 
        (bass ? this.bass.equals(c.bass) : true) && 
        (seventh ? this.seventh.equals(c.seventh) : true) && 
        (ninth ? this.ninth.equals(c.ninth) : true) && 
        (eleventh ? this.eleventh.equals(c.eleventh) : true) && 
        (thirteenth ? this.thirteenth.equals(c.thirteenth) : true);
    } else {
      return false;
    }
  }

  public Set<NoteSymbol> notes() {
    Set<NoteSymbol> set = new TreeSet<NoteSymbol>();
    if (bass != null)
      set.add(bass);
    set.add(root);
    if (mode == Mode.SUS2 || mode == Mode.SUS9)
      set.add(root.transpose(2));
    else if (mode == Mode.MIN || mode == Mode.DIM)
      set.add(root.transpose(3));
    else if (mode == Mode.MAJ || mode == Mode.AUG)
      set.add(root.transpose(4));
    else if (mode == Mode.SUS4)
      set.add(root.transpose(5));
    if (fifth == ExtendedNote.FLAT || mode == Mode.DIM)
      set.add(root.transpose(6));
    else if (fifth == ExtendedNote.SHARP || mode == Mode.AUG)
      set.add(root.transpose(8));
    else
      set.add(root.transpose(7));
    if (seventh == Seventh.SIXTH)
      set.add(root.transpose(9));
    else if (seventh == Seventh.SEVENTH || seventh == Seventh.DOM7)
      set.add(root.transpose(10));
    else if (seventh == Seventh.MAJ_SEVENTH || seventh == Seventh.MAJ7)
      set.add(root.transpose(11));
    if (ninth == ExtendedNote.FLAT)
      set.add(root.transpose(13));
    else if (ninth == ExtendedNote.NATURAL)
      set.add(root.transpose(14));
    else if (ninth == ExtendedNote.SHARP)
      set.add(root.transpose(15));
    if (eleventh == ExtendedNote.NATURAL)
      set.add(root.transpose(18));
    if (thirteenth == ExtendedNote.NATURAL)
      set.add(root.transpose(21));
    return set;
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
