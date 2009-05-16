package jp.crestmuse.cmx.math;

class DefaultBooleanArray implements BooleanArray, Cloneable {
    private boolean[] values;

    DefaultBooleanArray(int length) {
	values = new boolean[length];
    }

    DefaultBooleanArray(boolean[] values) {
	this.values = values;
    }

    DefaultBooleanArray(int length, boolean value) {
	values = new boolean[length];
	for (int i = 0; i < length; i++)
	    values[i] = value;
    }

    public int length() {
	return values.length;
    }

    public boolean get(int index) {
	return values[index];
    }

    public void set(int index, boolean value) {
	values[index] = value;
    }
}
