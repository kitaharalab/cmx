package jp.crestmuse.cmx.sound;
import jp.crestmuse.cmx.amusaj.filewrappers.WAVXMLWrapper;
import jp.crestmuse.cmx.filewrappers.amusaj.*;

public class SynchroPlayTest implements WAVPlaySynchronized {

  SynchroPlayTest(String wavfile, String smffile) throws Exception {
    WAVXMLWrapper wavxml = WAVXMLWrapper.readWAV(wavfile);
    WAVPlayer wavplayer = new WAVPlayer(wavxml);
    SynchronizedSMFPlayer smfplayer = new SynchronizedSMFPlayer();
    smfplayer.readSMF(smffile);
    WAVPlaySynchronizer sync = new WAVPlaySynchronizer(wavplayer);
    sync.addSynchronizedComponent(smfplayer);
    sync.addSynchronizedComponent(this);
    sync.setSleepTime(1000);
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
      new SynchroPlayTest(args[0], args[1]);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}