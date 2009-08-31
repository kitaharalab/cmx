package jp.crestmuse.cmx.gui.deveditor.controller;

import javax.sound.midi.InvalidMidiDataException;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;

public class ChangeDeviation extends DeviatedNoteCommand {

  private double attack;
  private double release;
  private double dynamics;
  private double endDynamics;
  private double prevDynamics;
  private double prevEndDynamics;

  public ChangeDeviation(DeviatedNote deviatedNote, double attack,
      double release) {
    this(deviatedNote, attack, release, deviatedNote.getDynamics(),
        deviatedNote.getEndDynamics());
  }

  public ChangeDeviation(DeviatedNote deviatedNote, double attack,
      double release, double dynamics, double endDynamics) {
    super(deviatedNote);
    this.attack = attack;
    this.release = release;
    this.dynamics = dynamics;
    this.endDynamics = endDynamics;
    this.prevDynamics = deviatedNote.getDynamics();
    this.prevEndDynamics = deviatedNote.getEndDynamics();
  }

  public void invoke() {
    try {
      deviatedNote.changeDeviation(attack, release, dynamics, endDynamics);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  public void redo() {
    invoke();
  }

  public void undo() {
    try {
      deviatedNote.changeDeviation(-attack, -release, prevDynamics,
          prevEndDynamics);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

}
