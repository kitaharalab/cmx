package jp.crestmuse.cmx.gui.deveditor.view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.crestmuse.cmx.gui.deveditor.model.DeviatedPerformance.DeviatedNote;


public class NoteEditPanel extends JPanel {

//  private PrintableDeviatedNote note;
  private JSpinner attackSpinner;
  private JSpinner releaseSpinner;
  private JSpinner dynamicsSpinner;
  private JSpinner endDynamicsSpinner;
  private JCheckBox missnote;
  private double prevAttack;
  private double prevRelease;

  public NoteEditPanel(){    
    setAttack();
    setRelease();
    setDynamics();
    setEndDynamics();
    setIsMissNote();
    JButton reset = new JButton("reset");
    reset.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        attackSpinner.setValue(0.0);
        releaseSpinner.setValue(0.0);
        dynamicsSpinner.setValue(1.0);
        endDynamicsSpinner.setValue(1.0);
        prevAttack = 0.0;
        prevRelease = 0.0;
        missnote.setSelected(false);
//        note.changeDeviation(0.0, 0.0, 1.0, 1.0);
//        note.setMissNote(false);
      }
    });
    JPanel spinners = new JPanel(new GridLayout(0, 2));
    spinners.add(new JLabel("attack"));
    spinners.add(attackSpinner);
    spinners.add(new JLabel("release"));
    spinners.add(releaseSpinner);
    spinners.add(new JLabel("dynamics"));
    spinners.add(dynamicsSpinner);
    spinners.add(new JLabel("endDynamincs"));
    spinners.add(endDynamicsSpinner);
    spinners.add(missnote);
    spinners.add(reset);
    add(spinners);
  }
  
  private void setAttack(){
    SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setValue(0.0);
    snm.setStepSize(0.1);
    attackSpinner = new JSpinner(snm);
    attackSpinner.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        double value = (Double)attackSpinner.getValue();
//        if(note.changeDeviation(value - prevAttack, 0, note.getDeviatedNote().getDynamics(), note.getDeviatedNote().getEndDynamics()))
//          prevAttack = value;
//        else
//          attackSpinner.setValue(prevAttack);
      }     
    });
  }
  
  private void setRelease(){
    SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setValue(0.0);
    snm.setStepSize(0.1);
    releaseSpinner = new JSpinner(snm);
    releaseSpinner.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        double value = (Double)releaseSpinner.getValue();
//        if(note.changeDeviation(0, value - prevRelease, note.getDeviatedNote().getDynamics(), note.getDeviatedNote().getEndDynamics()))
//          prevRelease = value;
//        else
//          releaseSpinner.setValue(prevRelease);
      }     
    });
  }
  
  private void setDynamics(){
    SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setMinimum(0.0);
    snm.setValue(0.0);
    snm.setStepSize(0.1);
    snm.setMaximum(1.0);
    dynamicsSpinner = new JSpinner(snm);
    dynamicsSpinner.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        double value = (Double)dynamicsSpinner.getValue();
//        note.changeDeviation(0, 0, value, note.getDeviatedNote().getEndDynamics());
      }
    });
  }
  
  private void setEndDynamics(){
    SpinnerNumberModel snm = new SpinnerNumberModel();
    snm.setMinimum(0.0);
    snm.setValue(0.0);
    snm.setStepSize(0.1);
    snm.setMaximum(1.0);
    endDynamicsSpinner = new JSpinner(snm);
    endDynamicsSpinner.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        double value = (Double)endDynamicsSpinner.getValue();
//        note.changeDeviation(0, 0, note.getDeviatedNote().getDynamics(), value);
      }
    });
  }
  
  private void setIsMissNote(){
    missnote = new JCheckBox("missnote");
    missnote.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
//        note.setMissNote(missnote.isSelected());
      }
    });
  }
  
//  public void setNote(PrintableDeviatedNote note){
//    this.note = note;
//    prevAttack = note.getDeviatedNote().getAttack();
//    prevRelease = note.getDeviatedNote().getRelease();
//    attackSpinner.setValue(note.getDeviatedNote().getAttack());
//    releaseSpinner.setValue(note.getDeviatedNote().getRelease());
//    dynamicsSpinner.setValue(note.getDeviatedNote().getDynamics());
//    endDynamicsSpinner.setValue(note.getDeviatedNote().getEndDynamics());
//    missnote.setSelected(note.getDeviatedNote().getIsMissNote());
//  }

}
