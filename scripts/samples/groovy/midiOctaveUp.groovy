import jp.crestmuse.cmx.processing.*
import jp.crestmuse.cmx.amusaj.sp.*

/***********************************************************************
   midiOctaveUp.groovy 
     recieves MIDI messages input by the user through a virtual keyboard, 
     moves up an octave, and transmits them to Java's software synthesizer.
 ***********************************************************************/

class MyApplet extends CMXApplet {
  void setup() {
    def vk = createVirtualKeyboard()
    def octaveUp = newSPModule(
      execute: { src, dest ->
        def (status, data1, data2) = src[0].getMessageInByteArray()
        def newevent = createShortMessageEvent([status, data1+12, data2], 0, 
					       src[0].music_position)
	dest[0].add(newevent)
			       },
      inputs: [MidiEventWithTicktime.class],
      outputs: [MidiEventWithTicktime.class]
    )
    def midiout = createMidiOut()
    connect(vk, 0, octaveUp, 0)
    connect(octaveUp, 0, midiout, 0)
  }

  void draw() {

  }
}

MyApplet.start("MyApplet")
