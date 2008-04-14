package jp.crestmuse.cmx.amusaj.sp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import jp.crestmuse.cmx.amusaj.filewrappers.EventSeriesCompatible;
import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;

public class NotePredictorModule implements ProducerConsumerCompatible{
	
	public NotePredictorModule(String fileName){
		try {
			preChord = "";
			DataSource source = new DataSource(fileName);
			inst = source.getDataSet();
			inst.setClassIndex(129);
			bayes = new BayesNet();
			bayes.buildClassifier(inst);
			eval = new Evaluation(inst);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String predictNextChord(boolean[] melody){
		Instance instance = new Instance(inst.numAttributes());
		try {
			instance.setValue(inst.attribute(0), preChord);
		} catch (IllegalArgumentException e1) {
			instance.setMissing(inst.attribute(0));
		}
		for(int i=1; i<inst.numAttributes()-1; i+=1){
			instance.setValue(inst.attribute(i), melody[i-1] ? "t" : "f");
		}
		instance.setMissing(inst.classAttribute());
		instance.setDataset(inst);
		
		try {
			eval.evaluateModelOnceAndRecordPrediction(bayes, instance);
			int index = (int)Double.parseDouble(eval.predictions().firstElement().toString().split(" ")[2]);
			preChord = inst.attribute(0).value(index);
			return preChord;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public TimeSeriesCompatible createOutputInstance(int nFrames, int timeunit) {
		return new NoteArraySeriesCompatible();
	}

	public void execute(List src, List dest) throws InterruptedException {
		for(Object o : src){
			NoteArray na1 = (NoteArray)o;
			NoteArray na2 = new NoteArray();
			na2.noteArray = na1.noteArray;
			na2.chord = predictNextChord(na1.noteArray);
			dest.add(na2);
		}
	}

	public int getInputChannels() { return 1;	}
	public int getOutputChannels() { return 1; }
	public void setParams(Map params) {}
	
	private String preChord;
	private Instances inst;
	private BayesNet bayes;
	private Evaluation eval;
	
	class NoteArraySeriesCompatible implements EventSeriesCompatible<NoteArray>, QueueReader<NoteArray>{
		
		NoteArraySeriesCompatible(){
			this._noteArraySeries = new LinkedList<NoteArray>();
		}

		public void add(NoteArray d) throws InterruptedException {
			this._noteArraySeries.add(d);
		}

		public int dim() { return NoteArray.DIM; }

		public int frames() { return this._noteArraySeries.size(); }

		public int bytesize() { return NoteArray.DIM; }

		public QueueReader<NoteArray> getQueueReader() {
			return this;
		}

		public NoteArray take() throws InterruptedException {
			return this._noteArraySeries.poll();
		}

		public Iterator<NoteArray> iterator() {
			return this._noteArraySeries.iterator();
		}
		
		public int timeunit() { return 0; }

		public String getAttribute(String key) { return null;	}

		public double getAttributeDouble(String key) { return 0; }

		public int getAttributeInt(String key) { return 0; }

		public Iterator<Entry<String, String>> getAttributeIterator() { return null; }

		public void setAttribute(String key, String value) {}

		public void setAttribute(String key, int value) {}

		public void setAttribute(String key, double value) {}
		
		private LinkedList<NoteArray> _noteArraySeries;
		
	}
	
	class NoteArray{
		static final int DIM = 128;
		boolean[] noteArray = new boolean[DIM];
		String chord = "";
	}

}
