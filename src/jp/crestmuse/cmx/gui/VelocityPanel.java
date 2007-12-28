package jp.crestmuse.cmx.gui;

import java.awt.*;

import javax.swing.JPanel;

/**
 * @author Mitsuyo Hashida @ CrestMuse Project, JST
 * @since 2007/12/21
 */
class VelocityPanel extends JPanel {

	static final int PANEL_HEIGHT = 130;

	private static final long serialVersionUID = 1L;

	/**
	 * @param frameWidth
	 */
	public VelocityPanel(int width) {
		setBackground(Color.white);
		setMinimumSize(new Dimension(width, PANEL_HEIGHT));
	}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
    @Override
    public void paintComponent(final Graphics g) {
      final Graphics2D g2 = (Graphics2D) g;
      super.paintComponent(g2);
/*      
      // 軸線
      g2.drawString("Vel.", getKeyboard().keyWidth - 25, 15);
      g2.drawLine(getKeyboard().keyWidth, 0, getKeyboard().keyWidth,
                  PANEL_HEIGHT);
      if (dev == null)
        return;
      int vel = dev.getBaseDynamics();
      // default_velocity
      g2.drawLine(getKeyboard().keyWidth - 10, PANEL_HEIGHT - vel,
                  getKeyboard().keyWidth, PANEL_HEIGHT - vel);
      g2.drawString(String.valueOf(vel), getKeyboard().keyWidth - 25,
                    PANEL_HEIGHT - vel - 1);
      // 座標移動
      g2.translate(getKeyboard().keyWidth + pianoroll.axisX, 0);
      drawVelocity(g2);
*/
    }

    private void drawVelocity(final Graphics2D g) {
      // ***TO DO***
/*
      if (xml == null)
        return;
      try {
        xml.processNotePartwise(new CMXNoteHandler() {
            
            @Override
            public void processMusicData(MusicData md,
                                         MusicXMLWrapper wrapper) {
              super.processMusicData(md, wrapper);
              
              if (!(md instanceof Note))
                return;
              Note n = (Note) md;
              if (n.rest())
                return;
              final int onset = getWidthOfNote(currentOnset);
              int velocity = dev.getBaseDynamics();
              NoteDeviation d = dev.getNoteDeviation(n);
              if (d != null)
                velocity *= d.dynamics();
                    g.setColor(setPartColor(partId));
                    g.fill3DRect(onset, PANEL_HEIGHT - velocity, 7,
                                 velocity, true);
                    g.drawString(String.valueOf(velocity), onset - 10,
                                 PANEL_HEIGHT - velocity);
                    
                    super.endNote(n);
            }
            
          });
      } catch (TransformerException e) {
        e.printStackTrace();
      }
*/      
    }
  }