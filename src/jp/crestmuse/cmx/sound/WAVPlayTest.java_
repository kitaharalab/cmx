package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.amusaj.filewrappers.WAVXMLWrapper;

public class WAVPlayTest implements MusicPlaySynchronized {

  WAVPlayTest(String filename) throws Exception {
    WAVXMLWrapper wavxml = WAVXMLWrapper.readWAV(filename);
      WAVPlayer player = new WAVPlayer(wavxml);
      MusicPlaySynchronizer sync = 
        new MusicPlaySynchronizer(player);
      sync.addSynchronizedComponent(this);
      sync.play();
  }

  public void synchronize(double currentTime, long currentTick, MusicPlaySynchronizer sync) {
  }

  public void start(MusicPlaySynchronizer sync) {
  }

  public void stop(MusicPlaySynchronizer sync) {
    System.exit(0);
  }
    
  public static void main(String[] args) {
    try {
      new WAVPlayTest(args[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}