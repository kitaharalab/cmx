package jp.crestmuse.cmx.gui.deveditor;

import jp.crestmuse.cmx.gui.deveditor.view.GUI;

public class Main {

  public static void main(String[] args) {
    GUI gui = GUI.getInstance();
    if(args.length >= 1)
      gui.open(args[0]);
  }

}
