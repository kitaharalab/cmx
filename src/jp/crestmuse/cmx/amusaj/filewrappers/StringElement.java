package jp.crestmuse.cmx.amusaj.filewrappers;

import jp.crestmuse.cmx.amusaj.sp.SPElement;

public class StringElement implements SPElement {
  
  private String data;
  
  public StringElement(String data){
    this.data = data;
  }

  public String encode() {
    return data;
  }

  public boolean hasNext() {
    return true;
  }

}
