package jp.crestmuse.cmx.amusaj.sp;
import java.util.*;

public abstract class MySPModule extends SPModule {
    public abstract Object execute(Object src, Object dest);

    public void execute(Object[] src, TimeSeriesCompatible[] dest) {
	execute((Object)src, (Object)dest);
    }

    public abstract Object inputs();
    
    public Class[] getInputClasses() {
	Object inputs = inputs();
	if (inputs instanceof Class) 
	    return new Class[]{(Class)inputs};
	else if (inputs instanceof Class[]) 
	    return (Class[])inputs;
	else if (inputs instanceof List) {
	    List<Class> l = (List<Class>)inputs;
	    return l.toArray(new Class[l.size()]);
	} else
	    throw new IllegalStateException("Invalid input classes: " + inputs);
    }

    public abstract Object outputs();

    public Class[] getOutputClasses() {
	Object outputs = outputs();
	if (outputs instanceof Class)
	    return new Class[]{(Class)outputs};
	else if (outputs instanceof Class[])
	    return (Class[])outputs;
	else if (outputs instanceof List) {
	    List<Class> l = (List<Class>)outputs;
	    return l.toArray(new Class[l.size()]);
	} else
	    throw new IllegalStateException("Invalid output classes: " + outputs);
	//	return (Class[]) outputs();
    }
}

	