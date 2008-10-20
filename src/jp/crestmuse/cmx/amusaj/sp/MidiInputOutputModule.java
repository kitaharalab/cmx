package jp.crestmuse.cmx.amusaj.sp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import jp.crestmuse.cmx.amusaj.filewrappers.TimeSeriesCompatible;
import jp.crestmuse.cmx.misc.QueueReader;
import jp.crestmuse.cmx.sound.MusicPlayer;
import jp.crestmuse.cmx.sound.SMFPlayer;

public class MidiInputOutputModule {
	
	public class MidiInput implements ProducerConsumerCompatible<Object, MidiEventWithTicktime>, Receiver, Runnable{

		int dev_num = 0;
		MidiDevice.Info[] dev_infos= MidiSystem.getMidiDeviceInfo();
		MidiDevice input_device;
		MusicPlayer mp;
		Thread th;
		Transmitter tm;
		
		BlockingQueue<MidiEventWithTicktime> src_queue = new LinkedBlockingQueue<MidiEventWithTicktime>();
		
		public MidiInput(MusicPlayer mp){
			try{
				//initializing device
				input_device  = MidiSystem.getMidiDevice(dev_infos[dev_num]);
				tm = input_device.getTransmitter();
				tm.setReceiver(this);
				
				//initializing 
				mp.play();
				th = new Thread(this);
				th.start();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//ProducerConsumerCompatible
		@Override
		public TimeSeriesCompatible<MidiEventWithTicktime> createOutputInstance(
				int frames, int timeunit) {
			// TODO createOutputInstanceは何をしているのか、ドキュメント無い･･･
			return null;
		}

		@Override
		public void execute(List<QueueReader<Object>> src,
				List<TimeSeriesCompatible<MidiEventWithTicktime>> dest)
				throws InterruptedException {
			dest.get(0).add(src_queue.take());
			//TODO　destとかsrcとか0の決めうちでいいのー？
		}

		public int getInputChannels() {
			return 0; //TODO 決めうちでいいのかな
		}
		public int getOutputChannels() {
			return 1;
		}

		@Override
		public void setParams(Map<String, Object> params) {
			// TODO 自動生成されたメソッド・スタブ
			
		}

		//Receiver
		public void close() {}
		public void send(MidiMessage message, long timeStamp) {
			//MIDIメッセージが来るたびにsendが呼び出される。
			MidiEventWithTicktime miwt= new MidiEventWithTicktime(message, timeStamp, mp.getTickPosition());
			src_queue.add(miwt);
		}

		//Runnable
		//MusicPlayerを監視し、曲が停止したら終了処理
		public void run() {
			while(th != null){
				if(!mp.isNowPlaying()){
					th = null;
					tm.close();
				}
				try{
					Thread.sleep(1000);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public class MidiOutput implements ProducerConsumerCompatible<MidiEventWithTicktime, Object>{

		Receiver receiver = null;
		
		public MidiOutput(){
			try{
				receiver = MidiSystem.getReceiver();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public TimeSeriesCompatible<Object> createOutputInstance(int frames,
				int timeunit) {
			return null;
		}

		@Override
		public void execute(List<QueueReader<MidiEventWithTicktime>> src,
				List<TimeSeriesCompatible<Object>> dest)
				throws InterruptedException {
			
			receiver.send(src.get(0).take().message, src.get(0).take().tick);
		}

		public int getInputChannels() {
			return 1;
		}

		public int getOutputChannels() {
			return 0; //TODOここは決め打っちゃっていいんだろうか
		}

		@Override
		public void setParams(Map<String, Object> params) {
		}
		
	}

	public class MidiEventWithTicktime extends MidiEvent{
		
		MidiMessage message;
		long tick;
		long music_position;
		
		public MidiEventWithTicktime(MidiMessage message, long tick, long position){
			super(message, tick);
			//super(message, -1);
			music_position = position;
		}
		
	}

}


