package jp.crestmuse.cmx.gui.deveditor;

import jp.crestmuse.cmx.gui.deveditor.view.MainFrame;

public class Main {

  public static void main(String[] args) {
    MainFrame gui = MainFrame.getInstance();
    if(args.length >= 1)
      gui.open(args[0]);
  }

}
