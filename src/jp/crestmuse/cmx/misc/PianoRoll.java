package jp.crestmuse.cmx.misc;

public interface PianoRoll {

  public interface DataModel {
    public int getMeasureNum();
    public int getBeatNum();
    public int getFirstMeasure();
    public void setFirstMeasure(int measure);
    public boolean isSelectable();
    public boolean isEditable();
    public void selectNote(int measure, double beat, int notenum);
    public boolean isSelected(int measure, double beat, int notenum);
    public void drawData(PianoRoll pianoroll);
    public int tick2measure(long tick);
    public double tick2beat(long tick);
    public void shiftMeasure(int measure);
  }

  public void drawNote(int measure, double beat, double duration,
                       int notenum, boolean selected, DataModel data);


}
