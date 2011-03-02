package jp.crestmuse.cmx.misc;
import java.util.*;

public enum NoteName {
  C("C", 0), 
    C_SHARP("C#", 1), 
    D("D", 2), 
    D_SHARP("D#", 3), 
    E("E", 4), 
    F("F", 5), 
    F_SHARP("F#", 6),
    G("G", 7), 
    G_SHARP("G#", 8), 
    A("A", 9), 
    A_SHARP("A#", 10), 
    B("B", 11);

  private String notation;
  private int num;
  private static final Map<String,NoteName> map;
  private static final NoteName[] all = 
    {C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B};
//  private static final NoteName[] c_major = 
//    {C, D, E, F, G, A, B};

  static {
    map = new HashMap<String,NoteName>();
    map("c", "b#", C);
    map("c#", "db", C_SHARP);
    map("d", null, D);
    map("d#", "eb", D_SHARP);
    map("e", "fb", E);
    map("e#", "f", F);
    map("f#", "gb", F_SHARP);
    map("g", null, G);
    map("g#", "ab", G_SHARP);
    map("a", null, A);
    map("a#", "bb", A_SHARP);
    map("b", "cb", B);
  }

  private static final void map(String s1, String s2, NoteName nn) {
    map.put(s1, nn);
    if (s2 != null) map.put(s2, nn);
  }

  private NoteName(String name, int ordinal) {
    notation = name;
    num = ordinal;
  }
  public String toString() {
    return notation;
  }
  public int number() {
    return num;
  }

  public static final NoteName parse(String s) {
    return map.get(s.toLowerCase());
  }
  
  public static final NoteName byNum(int i) {
    return values()[i];
  }

  public static final NoteName[] getAll() {
    return all;
  }

  public static void main(String[] args) {
    System.out.println(NoteName.parse(args[0]));
    System.out.println(NoteName.byNum(Integer.parseInt(args[1])));
  }
}