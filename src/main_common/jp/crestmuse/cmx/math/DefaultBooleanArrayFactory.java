package jp.crestmuse.cmx.math;

public class DefaultBooleanArrayFactory extends BooleanArrayFactory {
    public BooleanArray createArray(int length) {
	return new DefaultBooleanArray(length);
    }

    public BooleanArray createArray(boolean[] values) {
	return new DefaultBooleanArray(values);
    }

    public BooleanArray createArray(int length, boolean value) {
	return new DefaultBooleanArray(length, value);
    }
}
