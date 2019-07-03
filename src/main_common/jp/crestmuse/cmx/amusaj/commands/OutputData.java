package jp.crestmuse.cmx.amusaj.commands;
import jp.crestmuse.cmx.amusaj.sp.*;
import java.util.*;

public class OutputData {
    ProducerConsumerCompatible module;
    int ch;
//    Map<String,String> attrs;
    public OutputData(ProducerConsumerCompatible module, int ch) {
      this.module = module;
      this.ch = ch;
//      attrs = new HashMap<String,String>();
    }
//    protected void setAttribute(String key, String value) {
//      attrs.put(key, value);
//    }
//    protected void setAttribute(String key, int value) {
//      attrs.put(key, String.valueOf(value));
//    }
//    protected void setAttribute(String key, double value) {
//      attrs.put(key, String.valueOf(value));
//    }
  }
