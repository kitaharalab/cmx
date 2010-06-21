package jp.crestmuse.cmx.inference;

import java.io.*;
import java.util.*;
import jp.crestmuse.cmx.inference.MusicRepresentation2.*;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;

public class SimpleHMMChainCalculator implements MusicLayerListener {
  private List<Hmm<ObservationInteger>> hmms = 
    new ArrayList<Hmm<ObservationInteger>>();
  private List<String> filenames = 
    new ArrayList<String>();
  private List<String> labels = 
    new ArrayList<String>();
  private String inputLayer, outputLayer;

  private PriorProbCalculator priorcalc;

  private MusicRepresentation2 mr = null;
  private int tiedLength;
  private int division;
  private ObservationInteger[] oseq;

    public SimpleHMMChainCalculator(File hmmfile, String workingDir, 
				  String inputLayer, String outputLayer, 
				  PriorProbCalculator priorcalc)
    throws IOException,FileFormatException {
    BufferedReader r = new BufferedReader(new FileReader(hmmfile));
    String line;
    OpdfIntegerReader opdfread = new OpdfIntegerReader();
    while ((line = r.readLine()) != null) {
      if (!line.startsWith("#")) {
	line = line.trim();
	String[] data = line.split(",");
	File f = data[0].startsWith(File.separator) ? 
	    new File(data[0]) : new File(workingDir, data[0]);
	hmms.add(HmmReader.read(new BufferedReader(new FileReader(f)), 
				opdfread));
	filenames.add(data[0]);
	labels.add(data[1]);
      }
    }
    this.inputLayer = inputLayer;
    this.outputLayer = outputLayer;
    this.priorcalc = priorcalc;
  }

  public void update(MusicRepresentation2 mr, MusicElement me, 
		     int measure, int tick) {
    if (mr != this.mr) {
      this.mr = mr;
      tiedLength = mr.getTiedLength(outputLayer);
      division = mr.getDivision();
      int measureNum = mr.getMeasureNum();
      oseq = new ObservationInteger[measureNum * division];
      for (int i = 0; i < measureNum * division; i++)
	oseq[i] = null;
    }
    oseq[measure * division + tick] = 
      new ObservationInteger(me.getHighestProbIndex());

    List<ObservationInteger> oseq1 = new ArrayList<ObservationInteger>();
    int from = ((measure * division + tick) / tiedLength) * tiedLength;
    int thru = ((measure * division + tick) / tiedLength + 1) * tiedLength;

    for (int i = from; i < thru; i++) {
      if (oseq[i] == null) {
	MusicElement e = mr.getMusicElement(inputLayer, 
					    i / division, i % division);
	oseq[i] = new ObservationInteger(me.getHighestProbIndex());
      }
      oseq1.add(oseq[i]);
    }

    double max = Double.NEGATIVE_INFINITY;
    int argmax = -1;
    for (int i = 0; i < hmms.size(); i++) {
      double prior = 1.0;
      if (priorcalc != null)
	prior = priorcalc.calcPriorProb(labels.get(i), null, mr, measure, tick);
      double p = hmms.get(i).probability(oseq1) * prior;
      if (p > max) {
	max = p;
	argmax = i;
      }
    }
    MusicElement e2 = mr.getMusicElement(outputLayer, measure, tick);
    e2.setEvidence(e2.addNewLabel(labels.get(argmax)));
  }


}