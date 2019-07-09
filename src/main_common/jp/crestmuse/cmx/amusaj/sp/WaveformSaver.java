package jp.crestmuse.cmx.amusaj.sp;

import java.io.IOException;
import java.util.LinkedList;

import jp.crestmuse.cmx.filewrappers.WAVWrapper;
import jp.crestmuse.cmx.math.DoubleArray;
import jp.crestmuse.cmx.math.DoubleArrayFactory;
import jp.crestmuse.cmx.math.Operations;

public class WaveformSaver extends SPModule {
	
	private LinkedList<DoubleArray[]> queue;
	private boolean isStereo;
	private WindowSlider winslider;
	private static final DoubleArrayFactory factory = DoubleArrayFactory.getFactory();
	
	// 適宜コンストラクタを準備
	public WaveformSaver(WindowSlider ws) {
		winslider = ws;
		queue = new LinkedList<DoubleArray[]>();
		isStereo = false;
  }
	
	public void start() {
		// queue を空にして，isStereo を true にする
		queue.clear();
		isStereo = true;
	}
	
	public void stop() {
		// isStereo を false にする (queue の中身はそのまま)
		isStereo = false;
	}

	public void execute(Object[] src, TimeSeriesCompatible[] dest) throws InterruptedException {
		// isStereo が true なら src をキャストして queue に入れる
		if (isStereo) {
			DoubleArray[] w = new DoubleArray[winslider.chTarget.length];
			for (int i = 0; i < winslider.chTarget.length; i++) {
				w[i] = (DoubleArray)src[i];
			}
			queue.offer(w);
		}
	}
	
	public WAVWrapper getWaveform() throws IOException {
		/*
		 * チャンネルごとに queue に入っている波形の断片を（重複を除いて）つないだ
		 * Doublearray を作り，WAVWrapper オブジェクトにして返す
		 */
		AmusaParameterSet params = AmusaParameterSet.getInstance();
		int winsize = params.getParamInt("fft", "WINDOW_SIZE");
		double shift = params.getParamDouble("fft", "SHIFT");
    if (shift < 1)
      shift = shift * params.getParamInt("fft", "SAMPLE_RATE");
    int shift_ = (int)shift;
		
		DoubleArray[] arrays = new DoubleArray[winslider.chTarget.length];
		for (int i = 0; i < arrays.length; i++)
			arrays[i] = factory.createArray(0);
		
		if (queue.size() > 0) {
			DoubleArray[] w = queue.poll();
			
			for (int i = 0; i < arrays.length; i++) {
				arrays[i] = Operations.concat(new DoubleArray[]{arrays[i], w[i].subarrayX(0, winsize)});
			}
		}
		
		while (queue.size() > 0) {
			DoubleArray[] w = queue.poll();
			
			for (int i = 0; i < arrays.length; i++) {
				arrays[i] = Operations.concat(new DoubleArray[]{arrays[i], w[i].subarrayX(winsize - shift_, winsize)});
			}
		}
		
		return new WAVWrapper(arrays, params.getParamInt("fft", "SAMPLE_RATE"));
	}

	public Class[] getInputClasses() {
		Class[] cc = new Class[winslider.chTarget.length];
    for (int i = 0; i < cc.length; i++)
      cc[i] = DoubleArray.class;
    return cc;
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

}
