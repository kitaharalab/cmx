package jp.crestmuse.cmx.commands;

import java.io.IOException;
import java.util.List;

import jp.crestmuse.cmx.filewrappers.CSVWrapper;
import jp.crestmuse.cmx.filewrappers.DeviationDataSet;
import jp.crestmuse.cmx.filewrappers.DeviationInstanceWrapper;

public class ApplyCSVTempoDeviation extends CMXCommand<DeviationInstanceWrapper,DeviationInstanceWrapper> {
    private String csvfilename = null;
    private double tempo = 0;
    
    static {
	addOptionHelpMessage("-csv <filename>", "");
	addOptionHelpMessage("-tempo <tempo>", "");
    }

    protected boolean setOptionsLocal(String option, String value) {
	if (option.equals("-csv")) {
	    csvfilename = value;
	    return true;
	} else if (option.equals("-tempo")) {
	    tempo = Double.parseDouble(value);
	    return true;
	} else {
	    return false;
	}
    }

    protected DeviationInstanceWrapper run(DeviationInstanceWrapper indata) throws IOException {
	DeviationDataSet dds = indata.toDeviationDataSet();
	if (tempo > 0) {
	    int firstMeasure = 
		indata.getTargetMusicXML().getPartList()[0].firstMeasureNumber();
	    dds.addNonPartwiseControl(firstMeasure, 1, "tempo", tempo);
	}
	CSVWrapper csv = new CSVWrapper(csvfilename);
	for (List<String> data : csv) {
	    dds.addNonPartwiseControl(Integer.parseInt(data.get(0)), Double.parseDouble(data.get(1)), "tempo-deviation", Double.parseDouble(data.get(2)));
	}
	return dds.toWrapper();
    }

    public static void main(String[] args) {
	ApplyCSVTempoDeviation a = new ApplyCSVTempoDeviation();
	try {
	    a.start(args);
	} catch (Exception e) {
	    a.showErrorMessage(e);
	    System.exit(1);
	}
    }
}
				      