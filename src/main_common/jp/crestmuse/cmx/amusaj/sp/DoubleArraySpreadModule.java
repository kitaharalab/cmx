package jp.crestmuse.cmx.amusaj.sp;

import jp.crestmuse.cmx.math.DoubleArray;

import static jp.crestmuse.cmx.math.MathUtils.create1dimDoubleArray;

public class DoubleArraySpreadModule extends SPModule {
    private int dim;
    private Class[] destClasses;
    public DoubleArraySpreadModule(int dim) {
	this.dim = dim;
	destClasses = new Class[dim];
	for (int i = 0; i < dim; i++)
	    destClasses[i] = DoubleArray.class;
    }
    public void execute(Object[]src, TimeSeriesCompatible[] dest) 
	throws InterruptedException {
	DoubleArray array = (DoubleArray)src[0];
	for (int i = 0; i < dim; i++)
	    dest[i].add(create1dimDoubleArray(array.get(i)));
    }

    public Class[] getInputClasses() {
	return new Class[] { DoubleArray.class };
    }

    public Class[] getOutputClasses() {
	return destClasses;
    }
}
