package jp.crestmuse.cmx.amusaj.sp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import jp.crestmuse.cmx.amusaj.filewrappers.MutableMIDINoteArraySeries;
import jp.crestmuse.cmx.amusaj.filewrappers.MIDINoteArray;
import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper.MIDIEvent;
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper.Track;
import jp.crestmuse.cmx.misc.QueueReader;

public class NotePredictorModule implements ProducerConsumerCompatible<MIDINoteArray, MIDINoteArray>{
	
	public NotePredictorModule(String fileName, MutableMIDINoteArraySeries input){
		try {
			preChord = "";
			DataSource source = new DataSource(fileName);
			inst = source.getDataSet();
			inst.setClassIndex(129);
			bayes = new BayesNet();
			bayes.buildClassifier(inst);
			eval = new Evaluation(inst);
			queuereader = input.getQueueReader();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TimeSeriesCompatible<MIDINoteArray> createOutputInstance(int nFrames, int timeunit) {
		return new MutableMIDINoteArraySeries(nFrames, timeunit);
	}

	public void execute(List<QueueReader<MIDINoteArray>> src, List<TimeSeriesCompatible<MIDINoteArray>> dest) throws InterruptedException {
		MIDINoteArray na1 = queuereader.take();
	  MIDINoteArray na2 = new MIDINoteArray();
	  na2.delta = na1.delta;
	  na2.noteArray = na1.noteArray.clone();
  	na2.chord = predictNextChord(na1.noteArray);
  	dest.get(0).add(na2);
  	//System.out.println(na1+"    "+na2);
	}

	public int getInputChannels() { return 1;	}
	public int getOutputChannels() { return 1; }
	public void setParams(Map<String, Object> params) {}
  
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
	
	private String preChord;
	private Instances inst;
	private BayesNet bayes;
	private Evaluation eval;
	private QueueReader<MIDINoteArray> queuereader;
	
	public static void main(String[] args){
	  MutableMIDINoteArraySeries input = SMF2MIDINoteArraySeries(args[0]);
	  NotePredictorModule npm = new NotePredictorModule(args[1], input);
	  SPExecutor sp = new SPExecutor(null, input.frames(), 0);
	  sp.addSPModule(npm);
	  try {
      sp.start();
      for(TimeSeriesCompatible<MIDINoteArray> tsc : sp.getResult(npm)){
        for(MIDINoteArray m : tsc.getQueueReader()){
          System.out.println(m);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
	}
	
	public static MutableMIDINoteArraySeries SMF2MIDINoteArraySeries(String fileName){
	  try {
      MIDIXMLWrapper midi = MIDIXMLWrapper.readSMF(fileName);
      LinkedList<MIDINoteArray> inputlist = new LinkedList<MIDINoteArray>();
      boolean[] prev = new boolean[128];
      int delta = 0;
      for(Track track : midi.getTrackList()){
        for(MIDIEvent event : track.getMIDIEventList()){
          delta += event.deltaTime();
          if(event.messageType().equals("NoteOn")){
            boolean[] now = prev.clone();
            now[event.value(0)] = true;
            MIDINoteArray n = new MIDINoteArray();
            n.noteArray = now;
            n.delta = delta;
            inputlist.add(n);
            prev = now;
            delta = 0;
          }else if(event.messageType().equals("NoteOff")){
            boolean[] now = prev.clone();
            now[event.value(0)] = false;
            MIDINoteArray n = new MIDINoteArray();
            n.noteArray = now;
            n.delta = delta;
            inputlist.add(n);
            prev = now;
            delta = 0;
          }
        }
      }
      MutableMIDINoteArraySeries input = new MutableMIDINoteArraySeries(inputlist.size(), 0);
      for(MIDINoteArray m : inputlist) input.add(m);
      return input;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
	}
	
}
