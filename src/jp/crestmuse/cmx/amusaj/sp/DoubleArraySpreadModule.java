package jp.crestmuse.cmx.amusaj.sp;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.amusaj.sp.Utils.*;

public class DoubleArraySpreadModule extends SPModule {
    private int dim;
    private Class[] destClasses;
    public DoubleArraySpreadModule(int dim) {
	this.dim = dim;
	destClasses = new Class[dim];
	for (int i = 0; i < dim; i++)
	    destClasses[i] = SPDoubleArray.class;
    }
    public void execute(SPElement[]src, 
			TimeSeriesCompatible<SPElement>[] dest) 
	throws InterruptedException {
	SPDoubleArray array = (SPDoubleArray)src[0];
	for (int i = 0; i < dim; i++)
	    dest[i].add(create1dimSPDoubleArray(array.get(i)));
    }

    public Class<SPElement>[] getInputClasses() {
	return new Class[] { SPDoubleArray.class };
    }

    public Class<SPElement>[] getOutputClasses() {
	return destClasses;
    }
}
