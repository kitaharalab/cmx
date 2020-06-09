# CrestMuse Toolkit (CMX) Basic usages

## Use CMXController class

### Play wave file with CMXController

```
import jp.crestmuse.cmx.processing.CMXController;

public class UseCMXController {

  public static void main(String[] args) {
    CMXController cmx = CMXController.getInstance();
    cmx.wavread("wavefilename.wav");
    cmx.playMusic();
  }

}
```

## Create a subclass of CMXApplet

Play wave file with a processing GUI

```
import jp.crestmuse.cmx.processing.CMXApplet;

public class SubclassOfCMXApplet extends CMXApplet {

  public static void main(String[] args) {
    SubclassOfCMXApplet.start
      (SubclassOfCMXApplet.class.getName());
  }

  @Override
  public void setup() {
    wavread("wavefilename.wav");
    playMusic();
  }

  @Override
  public void draw() {
    // do nothing  }
  }
}
```

## Use CMX as a file converter from CLI

CMX converts files between MusicXML, SCCXML, MIDIXML, standard MIDI files from a command line.

```
java -cp cmx_jre.jar;libs\* jp.crestmuse.cmx.commands.[class name] [options]
```

Converts a standard MIDI file to a SCCXML file

```
java -cp cmx_jre.jar;libs\* jp.crestmuse.cmx.commands.SMF2SCC filename.mid
```

Converts to a standard MIDI file to a SCCXML file

```
java -cp cmx_jre.jar;libs\* jp.crestmuse.cmx.commands.SMF2SCC midifilename.mid -o sccfilename.xml
```
