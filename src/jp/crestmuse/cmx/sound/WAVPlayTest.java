package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.amusaj.filewrappers.WAVXMLWrapper;

public class WAVPlayTest implements WAVPlaySynchronized {

  WAVPlayTest(String filename) throws Exception {
    WAVXMLWrapper wavxml = WAVXMLWrapper.readWAV(filename);
      WAVPlayer player = new WAVPlayer(wavxml);
      WAVPlaySynchronizer sync = 
        new WAVPlaySynchronizer(player);
      sync.addSynchronizedComponent(this);
      sync.wavplay();
  }

  public void synchronize(double currentTime, WAVPlaySynchronizer sync) {
  }

  public void start(WAVPlaySynchronizer sync) {
  }

  public void stop(WAVPlaySynchronizer sync) {
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