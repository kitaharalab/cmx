package jp.crestmuse.cmx.amusaj.sp;

public class SPTerminator implements SPElement {

    private static final SPTerminator spterm = new SPTerminator();
    private SPTerminator() {
    }
    public static final SPTerminator getInstance() {
	return spterm;
    }

}
