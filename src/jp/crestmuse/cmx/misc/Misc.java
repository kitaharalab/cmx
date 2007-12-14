package jp.crestmuse.cmx.misc;
import java.io.*;

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
}

	    
