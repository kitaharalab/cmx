//import jp.crestmuse.cmx.sound.*
import jp.crestmuse.cmx.amusaj.sp.*
//import jp.crestmuse.cmx.amusaj.filewrappers.*
import jp.crestmuse.cmx.processing.*

/************************************************************
   midiinput.groovy 
     prints MIDI messages input by the user through 
     a virtual keyboard displayed on the screen. 
 ************************************************************/

class MyApp extends CMXApplet {
  void setup() {
    def vk = createVirtualKeyboard()
    def midiprint = newSPModule(execute: { src, dest ->
        def (status, data1, data2) = src[0].getMessageInByteArray()
        println("MIDI Message: ${status}\t${data1}\t${data2}")
      }, 
      inputs: [MidiEventWithTicktime.class],
      outputs: []
    )
    connect(vk, 0, midiprint, 0)
  }
  void draw() {
  }
}
MyApp.start("MyApp")
