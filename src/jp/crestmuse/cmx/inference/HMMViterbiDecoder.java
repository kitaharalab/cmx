package jp.crestmuse.cmx.inference;

import be.ac.ulg.montefiore.run.jahmm.*;
import jp.crestmuse.cmx.math.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import java.util.*;

public class HMMViterbiDecoder extends SPModule {
    private Hmm<ObservationVector> hmm;
    private List<ObservationVector> l 
	= new ArrayList<ObservationVector>();
    
    public HMMViterbiDecoder(Hmm<ObservationVector> hmm) {
	this.hmm = hmm;
    }

    public void execute(SPElement[] src, 
			TimeSeriesCompatible<SPElement>[] dest)  
	throws InterruptedException {
	DoubleArray x = (DoubleArray)src[0];
	ObservationVector v = new ObservationVector(x.toArray());
	l.add(v);
    }

    public void stop(SPElement[] src, 
		     TimeSeriesCompatible<SPElement>[] dest) {
	try {
	    int[] states = hmm.mostLikelyStateSequence(l);
	    dest[0].add(new SPIntArray(states));
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public Class<SPElement>[] getInputClasses() {
	return new Class[] { SPDoubleArray.class };
    }

    public Class<SPElement>[] getOutputClasses() {
	return new Class[] { SPIntArray.class };
    }
}