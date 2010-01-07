package jp.crestmuse.cmx.inference;

import be.ac.ulg.montefiore.run.jahmm.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;
import static jp.crestmuse.cmx.math.Utils.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import jp.crestmuse.cmx.amusaj.filewrappers.*;

public class LtoRHMMChainCalcModule extends SPModule {
    private LtoRHMMChain<ObservationVector> hmmchain;
    private int nHMMs;
    private double[][] prevLogLik = null;
    
    public LtoRHMMChainCalcModule(LtoRHMMChain<ObservationVector> hmmchain) {
	this.hmmchain = hmmchain;
	nHMMs = hmmchain.nHMMs();
    }

    public void execute(Object[] src, 
			TimeSeriesCompatible[] dest)  throws InterruptedException {
	DoubleArray array = (DoubleArray)src[0];
	ObservationVector vec = new ObservationVector(array.toArray());
	if (prevLogLik == null)
	    prevLogLik = hmmchain.calcLogLikelihood1st(vec);
	else
	    prevLogLik = hmmchain.calcLogLikelihood(vec, prevLogLik);
	for (int i = 0; i < nHMMs; i++)
	    dest[i].add(createDoubleArray(prevLogLik[i]));
    }

    public Class[] getInputClasses() {
	return new Class[] { DoubleArray.class };
    }

    public Class[] getOutputClasses() {
	Class[] classes = new Class[nHMMs];
	for (int i = 0; i < nHMMs; i++)
	    classes[i] = DoubleArray.class;
	return classes;
    }
}