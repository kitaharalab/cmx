package jp.crestmuse.cmx.amusaj.filewrappers;

/**
 * MutableTimeSeriesの型パラメータをDeltatimeを所得できるものに限定するクラス
 */
public abstract class MutableEventSeries<D extends DeltaTimeSupporting> extends MutableTimeSeries<D> implements EventSeriesCompatible<D> {
	
  MutableEventSeries(int nFrames, int timeunit){
    super(nFrames, timeunit);
  }

  public int timeunit() {
    throw new UnsupportedOperationException();
  }
}
