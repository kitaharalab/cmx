package jp.crestmuse.cmx.amusaj.commands.test;
import jp.crestmuse.cmx.amusaj.filewrappers.*;
import jp.crestmuse.cmx.filewrappers.*;

public class ReaderTest{
    public static void main(String[] args) {
	try {
	    AmusaXMLWrapper w = (AmusaXMLWrapper)CMXFileWrapper.readfile(args[0]);
	    w.write(System.out);
	    // AmusaDataSet dataset = SimpleAmusaXMLReader.readfile(args[0]);
	    // dataset.write(System.out);
	    // dataset.toWrapper().write(System.out);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
