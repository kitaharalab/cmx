package jp.crestmuse.cmx.amusaj.sp;


public class SPString implements SPElement {
  
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
