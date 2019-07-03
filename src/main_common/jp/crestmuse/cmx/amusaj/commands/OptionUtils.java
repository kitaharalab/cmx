package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.amusaj.sp.*;

public class OptionUtils {
  private static final AmusaParameterSet params = AmusaParameterSet.getInstance();

  public static boolean setFFTOptions(String option, String value) {
      if (option.equals("-winsize")) {
      params.setParam("fft", "WINDOW_SIZE", value);
      return true;
    } else if (option.equals("-wintype")) {
      params.setParam("fft", "WINDOW_TYPE", value);
      return true;
    } else if (option.equals("-shift")) {
      params.setParam("fft", "SHIFT", value);
      return true;
    } else if (option.equals("-ch")) {
      params.setParam("fft", "TARGET_CHANNEL", value);
      return true;
    } else {
      return false;
    }
  }

  public static boolean setF0PDFOptions(String option, String value) {
    if (option.equals("-from") || option.equals("-f")) {
      params.setParam("f0pdf", "NOTENUMBER_FROM", value);
      return true;
    } else if (option.equals("-thru") || option.equals("-t")) {
      params.setParam("f0pdf", "NOTENUMBER_THRU", value);
      return true;
    } else if (option.equals("-step")) {
      params.setParam("f0pdf", "STEP", value);
      return true;
    } else if (option.equals("-filter")) {
      params.setParam("f0pdf", "FILTER_NAME", value);
      return true;
    } else {
      return false;
    }
  }
 
  public static boolean setHMMOptions(String option, String value) {
    if (option.equals("-hmm")) {
      params.setParam("hmm", "HMM_FILENAME", value);
      return true;
    } else {
      return false;
    }
  }
 
}
