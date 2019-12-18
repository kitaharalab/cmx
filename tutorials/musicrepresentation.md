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

### Read/write MIDI files

Convert SMF to MIDIXML

```
import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.processing.CMXController;

public class UseCMXController {

  public static void main(String[] args) {
    MIDIXMLWrapper midixmlWrapper = CMXController.readSMFAsMIDIXML("midifilename.mid");
    try {
      midixmlWrapper.println();
    } catch (SAXException e) {
      e.printStackTrace();
    }
  }

}
```

Convert SMF to SCCXML

```
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;
import jp.crestmuse.cmx.processing.CMXController;

public class UseCMXController {

  public static void main(String[] args) {
    MIDIXMLWrapper midixmlWrapper = CMXController.readSMFAsMIDIXML("midifilename.mid");
    try {
      SCCXMLWrapper sccxmlWrapper = midixmlWrapper.toSCCXML();
      sccxmlWrapper.println();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
```

Get information of each note

```
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
java -cp cmx_pc.jar;libs\* jp.crestmuse.cmx.commands.[class name] [options]
```

Converts a standard MIDI file to a SCCXML file

```
java -cp cmx_pc.jar;libs\* jp.crestmuse.cmx.commands.SMF2SCC filename.mid
```

Converts to a standard MIDI file to a SCCXML file

```
java -cp cmx_pc.jar;libs\* jp.crestmuse.cmx.commands.SMF2SCC midifilename.mid -o sccfilename.xml
```
