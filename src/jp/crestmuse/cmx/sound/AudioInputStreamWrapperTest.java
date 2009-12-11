package jp.crestmuse.cmx.sound;
import javax.sound.sampled.*;
import static jp.crestmuse.cmx.math.Utils.*;

public class AudioInputStreamWrapperTest {
  public static void main(String[] args) {
    try {
      AudioInputStreamWrapper wrapper = 
        AudioInputStreamWrapper.createWrapper8(16000);
      TargetDataLine line = wrapper.getLine();
      line.start();
//      while (true)
      while (wrapper.hasNext(4096))
        System.out.println(toString2(wrapper.readNext(4096, 4096-160)[0]));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}

  