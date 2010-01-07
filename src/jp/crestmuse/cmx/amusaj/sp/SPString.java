package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.misc.*;


public class SPString implements Encodable {
  
  private String data;
  
  public SPString(String data){
    this.data = data;
  }

  public String encode() {
    return data;
  }

  public String toString() {
    return data;
  }

}
