package jp.crestmuse.cmx.processing.gui;
import jp.crestmuse.cmx.misc.PianoRoll;
import jp.crestmuse.cmx.processing.CMXApplet;

/** This code is partly derived from Yuichi Tsuchiya's code */

public class SimplePianoRoll extends CMXApplet implements PianoRoll{
  double octaveWidth = 210.0;
  int nOctave = 3;
  //  double lineWidth=210.0/12.0;
  int basenn = 48;
  private DataModel data = null;
  private int noteR = 255, noteB = 25, noteG = 200;
  private int barR = 255, barG = 0, barB = 0;
  private boolean isVisible = true;

  public void settings() {
    size(1200, 700);
  }
  public void setup() {
    background(255);
    smooth();
  }

  public void draw() {
    drawPianoRoll();
  }

  //  public void size(int width, int height) {
  //    super.size(width, 700);
  //    System.err.println("Warning: Height must be 700");
  //  }

  public void setDataModel(DataModel data) {
    this.data = data;
  }

  public DataModel getDataModel() {
    return data;
  }

  public void setNoteColor(int r, int g, int b) {
    noteR = r;
    noteG = g;
    noteB = b;
  }

  public void setBaseNoteNum(int nn) {
    if (nn % 12 == 0)
      basenn = nn;
    else
      throw new IllegalArgumentException("Base note number must be 60*n");
  }

  public void setOctaveWidth(double width) {
    octaveWidth = width;
  }

  public void setNumOfOctaves(int n) {
    nOctave = n;
  }

  public boolean isInside(int x, int y) {
    return x >= 100 && x < width && y >= 0 && y < nOctave * octaveWidth;
  }
  
  public double y2notenum(double y) {
    int topnn = basenn + 12 * nOctave;
    return topnn - y / (octaveWidth / 12);
  }

  protected double notenum2y(int nn) {
    int topnn = basenn + 12 * nOctave;
    return (octaveWidth / 12) * (topnn - nn - 1);
  }

  protected double notenum2y(double nn) {
    int topnn = basenn + 12 * nOctave;
    return (octaveWidth / 12) * (topnn - nn - 1);
  }

  protected double beat2x(int measure, double beat) {
    return beat2x(measure, beat, data);
  }
  
  protected double beat2x(int measure, double beat, DataModel data) {
    double lenMeas = (double)(width - 100) / data.getMeasureNum();
    return 100 + measure * lenMeas + beat * lenMeas / data.getBeatNum();
  }

  protected int x2measure(double x) {
    return x2measure(x, data);
  }
  
  protected int x2measure(double x, DataModel data) {
    double lenMeas = (double)(width - 100) / data.getMeasureNum();
    return (int)((x - 100) / lenMeas);
  }

  protected double x2beat(double x) {
    return x2beat(x, data);
  }
  
  protected double x2beat(double x, DataModel data) {
    double lenMeas = (width - 100.0) / data.getMeasureNum();
    int meas = (int)((x - 100) / lenMeas);
    return ((x - 100.0) / lenMeas - meas) * data.getBeatNum();
  }
  
  public void drawNote(int measure, double beat, double duration,
                       int notenum, boolean selected, DataModel data) {
    if (isVisible) {
      double lenMeas = (double)(width - 100) / data.getMeasureNum();
      double x = beat2x(measure, beat, data);
      //double x = 100 + measure * lenMeas + beat * lenMeas / data.getBeatNum();
      double w = duration * lenMeas / data.getBeatNum();
      double y = notenum2y(notenum);
      rect((float)x, (float)y, (float)w, (float)octaveWidth / 12);
    }
  }
  
  private void drawPianoRoll() {
    background(255);              
    drawLines();
    for(int a=0;a<nOctave;a++){//鍵盤表示
      drawKeyboard(0, (int)(octaveWidth*a));
    }
    if (data != null) {
      fill(noteR, noteG, noteB);
      data.drawData(this);
      if (isNowPlaying()) {
        drawCurrentBar();
      }
    }
  }

  private void drawLines() {
    for(int a=0;a<nOctave;a++){
      for(int b=1;b<=12;b++){
        stroke(130);
        strokeWeight(0);
        double lineWidth = octaveWidth / 12;
        line(100,(a*octaveWidth)+lineWidth*b,width,(a*octaveWidth)+lineWidth*b);
      }
    }
    if (data != null) {
      double lengtheach = (double)(width - 100) / data.getMeasureNum();
      for (int i = 0; i < data.getMeasureNum(); i++) {
        line((int)(100 + i * lengtheach), 0,
             (int)(100 + i * lengtheach), nOctave*octaveWidth);
      }
    }
  }

  public int getCurrentMeasure() {
    return data.tick2measure(getTickPosition());
  }

  public double getCurrentBeat() {
    return data.tick2beat(getTickPosition());
  }
  
  private void drawCurrentBar() {
    int measure = getCurrentMeasure();
    double beat = getCurrentBeat();
    if (measure >= 0) {
    double x = beat2x(measure, beat);
      stroke(barR, barG, barB);
      line((int)x, 0, (int)x, 630);
//      System.out.println("drawCurrentBar():x=" + (int)x );
    }
  }
  
  private void drawKeyboard(int x,int y){
   stroke(130);
   line(100,y+0,100,y+octaveWidth);
   for (int i = 1; i <= 7; i++)
     line(x, y + (int)(i*octaveWidth/7), 100, y + (int)(i*octaveWidth/7));

   //黒鍵
   fill(0);
   rect(x, y + (int)(1*octaveWidth/12), 60, (int)(octaveWidth/12));
   rect(x, y + (int)(3*octaveWidth/12), 60, (int)(octaveWidth/12));
   rect(x, y + (int)(5*octaveWidth/12), 60, (int)(octaveWidth/12));
   rect(x, y + (int)(8*octaveWidth/12), 60, (int)(octaveWidth/12));
   rect(x, y + (int)(10*octaveWidth/12), 60, (int)(octaveWidth/12));
  }

  public void setNoteVisible(boolean b) {
    isVisible = b;
  }

  public boolean isNoteVisible() {
    return isVisible;
  }

}
