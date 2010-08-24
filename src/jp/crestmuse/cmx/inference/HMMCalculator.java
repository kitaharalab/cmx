package jp.crestmuse.cmx.inference;

import java.io.*;
import java.util.*;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;

public class HMMCalculator implements MusicLayerListener {
  private MusicRepresentation mr;
  private Hmm<ObservationInteger>[] hmms;
  private int nHMMs;
  private List<String> labels;
  private String inputLayer, outputLayer;
  private int rest;
  private int hmmindex = -1;
  private List<ObservationInteger> oseq;
  private int[] sseq;

  public HMMCalculator(String[] hmmfiles, String labelfile, 
		       String inputLayer, String outputLayer, int rest, 
		       MusicRepresentation mr) 
    throws IOException, FileFormatException {
    nHMMs = hmmfiles.length;
    hmms = new Hmm[nHMMs];
    for (int i = 0; i < nHMMs; i++) {
      BufferedReader hmmreader = 
	new BufferedReader(new FileReader(new String(hmmfiles[i])));
      OpdfIntegerReader opdfreader = new OpdfIntegerReader();
      hmms[i] = HmmReader.read(hmmreader, opdfreader);
      hmmreader.close();
    }
    BufferedReader labelreader = 
      new BufferedReader(new FileReader(new String(labelfile)));
    labels = new ArrayList<String>();
    String line;
    while ((line = labelreader.readLine()) != null) {
      labels.add(line.trim());
    }
    labelreader.close();
    this.inputLayer = inputLayer;
    this.outputLayer = outputLayer;
    this.rest = rest;
    this.mr = mr;
  }

  public void calcViterbi(MusicRepresentation mr, int measure, int tick) {
    int inputTiedLength = mr.getTiedLength(inputLayer);
    int division = mr.getDivision();
    int measureNum = mr.getMeasureNum() / nHMMs;
    hmmindex = measure / measureNum;
    oseq = new ArrayList<ObservationInteger>();
    for (int i = 0; i < measureNum; i++) {
      for (int j = 0; j < division; j += inputTiedLength) {
	MusicElement e = mr.getMusicElement(inputLayer, 
					    i + hmmindex * measureNum, j);
	if (e.rest()) 
	  oseq.add(new ObservationInteger(rest));
	else
	  oseq.add(new ObservationInteger(e.getHighestProbIndex()));
      }
    }
    sseq = hmms[hmmindex].mostLikelyStateSequence(oseq);
  }


  public void update(MusicRepresentation mr, MusicElement me, 
		     int measure, int tick) {
    this.mr = mr;
    int outputTiedLength = mr.getTiedLength(outputLayer);
    int division = mr.getDivision();
    int measureNum = mr.getMeasureNum() / nHMMs;
    calcViterbi(mr, measure, tick);
    mr.suspendUpdate();
    for (int i = 0; i < measureNum; i++) {
      for (int j = 0; j < division ; j += outputTiedLength) {
	MusicElement e = mr.getMusicElement(outputLayer, 
					    i + hmmindex * measureNum, j);
	String l = labels.get(sseq[i * division + j]);
	e.setEvidence(e.getIndexOf(getLabel(l)));
	e.update();
      }
    }
    mr.resumeUpdate();
  }

  protected Object getLabel(String s) {
    return s;
  }

  public double[] tryDifferentStates(int measure, int tick, List<String> l) {
    int outputTiedLength = mr.getTiedLength(outputLayer);
    int division = mr.getDivision();
    int measureNum = mr.getMeasureNum() / nHMMs;
    int nChords = labels.size() / (measureNum * division);
    
    System.err.println(measure / measureNum);
    System.err.println(hmmindex);

    if (measure / measureNum != hmmindex)
      calcViterbi(mr, measure, tick);

    System.err.println(hmmindex);

    int[] mysseq = sseq.clone();
//    for (int i = 0; i < mysseq.length; i++)
//      System.err.print(mysseq[i] + " ");
//    System.err.println();
    double[] p = new double[nChords];
//    System.err.println("org: " + mysseq[measure * division + tick]);
    for (int k = 0; k < nChords; k++) {
//      System.err.println("measure: " + measure);
//      System.err.println("division: " + division);
//      System.err.println("tick: " + tick);
//      System.err.println("nChords: " + nChords);
//      System.err.println("k: " + k);
      int stindex = ((measure%measureNum) * division + tick) * nChords + k;
      l.add(labels.get(stindex));
      for (int j = 0; j < outputTiedLength; j++) {
	stindex = ((measure%measureNum) * division + tick + j) * nChords + k;
	mysseq[(measure%measureNum) * division + tick + j] = stindex;
      }

      p[k] = hmms[hmmindex].probability(oseq, mysseq);
//      System.err.println(p[k]);
    }
//    System.err.println(hmms[hmmindex].probability(oseq));
    return p;
  }
      

      


}

