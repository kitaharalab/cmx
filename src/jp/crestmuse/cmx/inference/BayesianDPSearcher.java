package jp.crestmuse.cmx.inference;

import java.util.LinkedList;
import jp.crestmuse.cmx.inference.MusicRepresentation2.*;


public class BayesianDPSearcher implements MusicLayerListener {

  private BayesNetCompatible bayesNet;
  private LinkedList<BayesianMapping> readMappings;
  private LinkedList<BayesianMapping> writeMappings;
    private String prevnode, currnode;
    private BayesianMapping bmPrevNode, bmCurrNode; 
    private double penalty = 0.0;
    

    public BayesianDPSearcher(BayesNetCompatible bayesNet, 
			      String prevnode, String currnode) {
    this.bayesNet = bayesNet;
    readMappings = new LinkedList<BayesianMapping>();
    writeMappings = new LinkedList<BayesianMapping>();
    this.prevnode = prevnode;
    this.currnode = currnode;
  }


    public void update(MusicRepresentation2 mr, MusicElement me, 
		      int measure, int tick) {
	if (measure == mr.getMeasureNum())
	    return;
	try {
	    for (BayesianMapping bm : readMappings) {
		MusicElement e = bm.mappedElement(mr, measure, tick);
		if (e != null) {
		    if (bm.evidenceOnly || e.hasEvidence()) {
			bayesNet.setEvidence(bm.bayesnetIndex, e.getHighestProbIndex());
		    } else {
			// ネットワークの始点じゃないとうまく行かない？
			double[][] distr = new double[1][];
			distr[0] = e.getAllProbs();
			bayesNet.setDistribution(bm.bayesnetIndex, distr);
		    }
		}
	    }
	    MusicElement prevElem = bmPrevNode.mappedElement(mr, measure, tick);
	    MusicElement currElem = bmCurrNode.mappedElement(mr, measure, tick);
	    if (prevElem == null) {
		bayesNet.update();
		double[] margins = bayesNet.getMargin(bmCurrNode.bayesnetIndex);
		for (int j = 0; j < margins.length; j++)
		    currElem.setProb(j, margins[j]);
	    } else {
		String[] values1 = bayesNet.getValues(bmPrevNode.bayesnetIndex);
		String[] values2 = bayesNet.getValues(bmCurrNode.bayesnetIndex);
		double[][] margins = new double[values1.length][];
		for (int i = 0; i < values1.length; i++) {
		    bayesNet.setEvidence(bmPrevNode.bayesnetIndex, i);
		    bayesNet.update();
		    margins[i] = bayesNet.getMargin(bmCurrNode.bayesnetIndex);
		}
		for (int j = 0; j < values2.length; j++) {
		    double max = Double.NEGATIVE_INFINITY;
		    int argmax = -1;
		    for (int i = 0; i < values1.length; i++) {
			double ll = Math.log(margins[i][j])
			    + prevElem.getLogLikelihood(i)
			    + (i == j ? -penalty : 0);
			if (ll > max) {
			    max = ll;
			    argmax = i;
			}
		    }
		    //double LL=prevElem.getLogLikelihood(argmax)+Math.log(max);
		    System.err.print(max + " ");
		    currElem.setLogLikelihood(j, max);
		    currElem.setBackPointer(j, argmax);
		}
		System.err.println();
		currElem.setBackPointerTo(prevElem);
	    }
	    for (BayesianMapping bm : writeMappings) {
		if (bm != bmCurrNode) {
		    MusicElement e = bm.mappedElement(mr, measure, tick);
		    if (e != null) {
			double[] margins = bayesNet.getMargin(bm.bayesnetIndex);
			for (int i = 0; i < margins.length; i++) {
			    e.removeEvidence(i);
			    e.setProb(i, margins[i]);
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new IllegalStateException(ex.toString());
	}
    }

    public void setContinuationPenalty(double penalty) {
	this.penalty = penalty;
    }
		    
  public void addReadMapping(BayesianMapping bm) {
    readMappings.add(bm);
    if (bm.bayesnetIndex == bayesNet.getNode(prevnode))
	bmPrevNode = bm;
  }

  public void addWriteMapping(BayesianMapping bm) {
    writeMappings.add(bm);
    if (bm.bayesnetIndex == bayesNet.getNode(currnode))
	bmCurrNode = bm;
  }
    
}
