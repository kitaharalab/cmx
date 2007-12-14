package jp.crestmuse.cmx.commands.test;
import jp.crestmuse.cmx.filewrappers.*;

public class WAVXMLTest {
  public static void main(String[] args) {
      try {
	  WAVXMLWrapper w = WAVXMLWrapper.readWAV(args[0]);
	  w.writefile("_.wavx");
          WAVXMLWrapper w2 =  (WAVXMLWrapper)CMXFileWrapper.readfile("_.wavx");
          w2.writefileAsWAV("_.wav");
      } catch (Exception e) {
	  e.printStackTrace();
      }
  }
}