package jp.crestmuse.cmx.amusaj.sp;

import java.io.IOException;
import java.util.LinkedList;

import jp.crestmuse.cmx.filewrappers.WAVWrapper;
import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Operations.*;

public class WaveformSaver extends SPModule {

  AmusaParameterSet params;
  private LinkedList<DoubleArray[]> queue;
  private boolean isStarted;
  private WindowSlider winslider;
  private int shift = 0;
  private static final DoubleArrayFactory factory = DoubleArrayFactory.getFactory();
	
  // 適宜コンストラクタを準備
  public WaveformSaver(WindowSlider ws) {
    winslider = ws;
    params = AmusaParameterSet.getInstance();
    double shift_ = params.getParamDouble("fft", "SHIFT");
    if (shift_ < 1)
      shift_ = shift_ * params.getParamInt("fft", "SAMPLE_RATE");
    shift = (int)shift_;
    queue = new LinkedList<DoubleArray[]>();
    isStarted = false;
  }
	
  public void start() {
    // queue を空にして，isStarted を true にする
    queue.clear();
    isStarted = true;
  }
	
  public void stop() {
    // isStarted を false にする (queue の中身はそのまま)
    isStarted = false;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest) throws InterruptedException {
    // isStarted が true なら src をキャストして queue に入れる
    if (isStarted) {
      DoubleArray[] w = new DoubleArray[winslider.chTarget.length];
      for (int i = 0; i < winslider.chTarget.length; i++) {
        w[i] = subarray((DoubleArray)src[i], 0, shift);
      }
      queue.offer(w);
    }
  }
	
  public WAVWrapper getWaveform() throws IOException {
    /*
     * チャンネルごとに queue に入っている波形の断片を（重複を除いて）つないだ
     * Doublearray を作り，WAVWrapper オブジェクトにして返す
     */
    //    System.err.print("Making a waveform ");
    /*
    AmusaParameterSet params = AmusaParameterSet.getInstance();
    int winsize = params.getParamInt("fft", "WINDOW_SIZE");
    double shift = params.getParamDouble("fft", "SHIFT");
    if (shift < 1)
      shift = shift * params.getParamInt("fft", "SAMPLE_RATE");
    int shift_ = (int)shift;
    */
    
    DoubleArray[] arrays = new DoubleArray[winslider.chTarget.length];

    for (int i = 0; i < arrays.length; i++) {
      arrays[i] = factory.createArray(shift * queue.size(), 0.0);
    }
    for (int t = 0; queue.size() > 0; t++) {
      DoubleArray[] w = queue.poll();
      int from = shift * t;
      for (int i = 0; i < arrays.length; i++) {
        Operations.addX(arrays[i], w[i], from);
      }
    }

    /*
    for (int i = 0; i < arrays.length; i++)
      arrays[i] = factory.createArray(0);
    
    if (queue.size() > 0) {
      DoubleArray[] w = queue.poll();
      
      for (int i = 0; i < arrays.length; i++) {
        arrays[i] = Operations.concat(new DoubleArray[]{arrays[i], w[i].subarrayX(0, winsize)});
      }
    }
		
    while (queue.size() > 0) {
      System.err.print(".");
      DoubleArray[] w = queue.poll();
			
      for (int i = 0; i < arrays.length; i++) {
        arrays[i] = Operations.concat(new DoubleArray[]{arrays[i], w[i].subarrayX(winsize - shift_, winsize)});
      }
    }

    System.err.println();
    */
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
