package jp.crestmuse.cmx.gui.performancematcher;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;
import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance;
import jp.crestmuse.cmx.gui.deveditor.view.MainFrame;

public class Main {

  public static void main(final String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          DeviationInstanceWrapper deviation = (DeviationInstanceWrapper) CMXFileWrapper.readfile(args[0]);
          DeviatedPerformance deviatedPerformance = new DeviatedPerformance(
              deviation);
          EditFrame f = new EditFrame(deviatedPerformance);
          f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          f.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

}
