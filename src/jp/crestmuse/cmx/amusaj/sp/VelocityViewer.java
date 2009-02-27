package jp.crestmuse.cmx.amusaj.sp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;

public class VelocityViewer extends SPModule<MidiEventWithTicktime, SPDummyObject> {
  
  private PrintWriter pw;
  private FileOutputStream fos;
  
  public VelocityViewer(String fileName) throws FileNotFoundException{
    fos = new FileOutputStream(new File(fileName), true);
    pw = new PrintWriter(fos);
  }

  public void execute(List<QueueReader<MidiEventWithTicktime>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest)
      throws InterruptedException {
    MidiEventWithTicktime  me = src.get(0).take();
    ShortMessage sm = (ShortMessage)me.getMessage();
    int num = sm.getData2();
    if(num != 0){
      System.out.println(sm.getData2());
      pw.println(num);
    }
  }

  public int getInputChannels() {
    return 1;
  }

  public int getOutputChannels() {
    return 1;
  }

  @Override
  public void stop(List<QueueReader<MidiEventWithTicktime>> src,
      List<TimeSeriesCompatible<SPDummyObject>> dest) {
    pw.println();
    pw.close();
    try {
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args){
    try { 
      String filename = "out.txt";
      if(args.length == 1) filename = args[0];
      SPExecutor sp = new SPExecutor(null, 0);
      MidiInputModule mi = new MidiInputModule(getMidiDevice(true));
      VelocityViewer vv = new VelocityViewer(filename);
      sp.addSPModule(mi);
      sp.addSPModule(vv);
      sp.connect(mi, 0, vv, 0);
      sp.start();
      //
      System.out.println("press button to exit...");
      System.in.read();
      sp.stop();
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static MidiDevice getMidiDevice(boolean trans) {
    MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
    MidiDevice device = null;

    for (int i = 0; i < info.length; i++) {
      try {
        device = MidiSystem.getMidiDevice(info[i]);
        if(trans && device.getMaxTransmitters() == 0) continue;
        if(!trans && device.getMaxReceivers() == 0) continue;
        System.err.println("*** " + i + " ***");
        System.err.println("  Description:" + info[i].getDescription());
        System.err.println("  Name:" + info[i].getName());
        System.err.println("  Vendor:" + info[i].getVendor());
        System.err.println();
      } catch (MidiUnavailableException e) {
        e.printStackTrace();
      }
    }

    try {
      BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
      if(trans) System.err.print("Using Input Device Number: ");
      else System.err.print("Using Output Device Number: ");
      String s = r.readLine();
      device = MidiSystem.getMidiDevice(info[Integer.parseInt(s)]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return device;
  }
}
