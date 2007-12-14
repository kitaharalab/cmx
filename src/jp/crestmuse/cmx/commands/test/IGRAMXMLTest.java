package jp.crestmuse.cmx.commands;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import java.util.*;

public class IGRAMXMLTest extends CMXCommand {
  protected void run() {
    IGRAMXMLWrapper igram = (IGRAMXMLWrapper)indata();
    IGRAMXMLWrapper.IGRAMData[] datalist = igram.getIGRAMDataList();
    for (IGRAMXMLWrapper.IGRAMData data : datalist) {
      System.out.println(data.inst());
      Queue<DoubleArray> queue = data.getQueue();
      for (DoubleArray array : queue) {
        for (int i = 0; i < array.length(); i++) {
          System.out.print(array.get(i) + " ");
        }
        System.out.println();
      }
    }
      
  }
  public static void main(String[] args) {
    IGRAMXMLTest t = new IGRAMXMLTest();
    try {
      t.start(args);
    } catch (Exception e) {
      t.showErrorMessage(e);
      System.exit(1);
    }
  }
}