package jp.crestmuse.cmx.misc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Misc {
    public static String readLineFromStdIn() {
	String s;
	try {
	    BufferedReader reader = 
		new BufferedReader(new InputStreamReader(System.in));
	    s = reader.readLine();
	} catch (IOException e) {
	    s = "";
	}
	return s;
    }

    public static String inputString(String message) {
	System.out.print(message);
	return readLineFromStdIn().trim();
    }

    public static double inputDouble(String message) {
	System.out.print(message);
	try {
	    return Double.parseDouble(readLineFromStdIn().trim());
	} catch (NumberFormatException e) {
	    return inputDouble(message);
	}
    }

    public static boolean inputYesNo(String message) {
	System.out.print(message);
	try {
	    String s = readLineFromStdIn().trim().substring(0, 1);
	    if (s.equalsIgnoreCase("y"))
		return true;
	    else if (s.equalsIgnoreCase("n"))
		return false;
	    else
		return inputYesNo(message);
	} catch (StringIndexOutOfBoundsException e) {
	    return inputYesNo(message);
	}
    }


  public static Object[] to1dimArray(Object[] array) {
    List l = new ArrayList();
    addElements(l, array);
    return l.toArray();
  }

  private static void addElements(List l, Object[] array) {
    for (Object o : array) 
      if (o instanceof Object[])
        addElements(l, (Object[])o);
      else
        l.add(o);
  }

  public static <K, V> Map<K, V>  createMap(K[] keys, V[] values) {
    if (keys.length != values.length)
      throw new IllegalArgumentException("invalid array size");
    Map<K, V> map = new HashMap<K, V>();
    for (int i = 0; i < keys.length; i++)
      map.put(keys[i], values[i]);
    return map;
  }

  public static String joinStrings(String separator, String[] data) {
    String s = "";
    for (int i = 0; i < data.length; i++) {
      if (data[i].length() > 0 && s.length() > 0)
        s += separator + data[i];
      else
        s += data[i];
    }
    return s;
  }

}

	    
