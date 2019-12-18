# CrestMuse Toolkit (CMX) Tutorial - Read/Write midi files

## Convert SMF to MIDIXML

```java
import org.xml.sax.SAXException;

import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.processing.CMXController;

public class UsingCMXController {

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

## Convert SMF to SCCXML

```java
import jp.crestmuse.cmx.filewrappers.MIDIXMLWrapper;
import jp.crestmuse.cmx.filewrappers.SCCXMLWrapper;
import jp.crestmuse.cmx.processing.CMXController;

public class UsingCMXController {

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

## CMXController returns a propper wrapper class

| input file format | output subclass of CMXFileWrapper |
| --- | --- |
| MusicXML | MusicXMLWrapper |
| SCCXML | SCCXMLWrapper |
| MIDIXML |  MIDIXMLWrapper |

```java
import jp.crestmuse.cmx.filewrappers.CMXFileWrapper;
import jp.crestmuse.cmx.processing.CMXController;

public class UsingCMXController {

    public static void main(String[] args) {
        try {
            CMXFileWrapper cmxFileWrapper = CMXController.readfile("musicxml.xml");
            System.out.println(cmxFileWrapper.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
```

## SCCDataSet class

File wrappers are classes for reading file contents. Use SCCDataSet to create new contents.

```java
import java.util.Arrays;
import jp.crestmuse.cmx.filewrappers.SCCDataSet;
import jp.crestmuse.cmx.processing.CMXController;

public class UsingCMXController {

    public static void main(String[] args) {
        SCCDataSet sccDataSet = CMXController.readSMFAsMIDIXML("midifilename.mid").toSCC();
        SCCDataSet.Part newPart = 
            sccDataSet.addPart(sccDataSet.getPartList()[0].serial() + 1, 
                sccDataSet.getPartList()[0].channel());
        newPart.addNoteElement(2400, 2880, 67, 100, 100);
        newPart.addNoteElement(2880, 3360, 67, 100, 100);
        newPart.addNoteElement(3360, 3840, 67, 100, 100);

        Arrays.stream(sccDataSet.getPartList()).forEach(part -> {
            Arrays.stream(part.getNoteOnlyList()).forEach(System.out::println);
        });

        CMXController cmx = CMXController.getInstance();
        try {
            cmx.smfread(sccDataSet.getMIDISequence());
        } catch (Exception e) {
            e.printStackTrace();
        }
        cmx.playMusic();
    }

}
```

## Get information of each note

```java
import java.util.Arrays;
import jp.crestmuse.cmx.filewrappers.SCCDataSet;
import jp.crestmuse.cmx.processing.CMXController;

public class UsingCMXController {

    public static void main(String[] args) {
        SCCDataSet sccDataSet = CMXController.readSMFAsMIDIXML("midifilename.mid").toSCC();
        Arrays.stream(sccDataSet.getPartList()).forEach(part -> {
            Arrays.stream(part.getNoteOnlyList()).forEach(System.out::println);
        });
    }

}
```
