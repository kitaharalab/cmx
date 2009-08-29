package jp.crestmuse.cmx.gui.deveditor;

import javax.swing.SwingUtilities;

import jp.crestmuse.cmx.gui.deveditor.view.MainFrame;

public class Main {

  public static void main(String[] args) {
    MainFrame.getInstance();
    if(args.length >= 1) {
//      try {
//        // たまに表示がおかしくなるので
//        Thread.sleep(500);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
      final String fileName = args[0];
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          MainFrame.getInstance().open(fileName);
        }
      });
    }
  }

}
