import jp.crestmuse.cmx.processing.*

class MyApp extends CMXApplet {
  void setup() {
    smfread(readfile('sample_scc.xml'))
    setMusicLoop(true)
    playMusic()
  }

  void draw() {

  }

  void keyPressed() {
    if (key == 'u') {
      setTempoInBPM(getTempoInBPM() + 10)
      println "New tempo: ${getTempoInBPM()}"
    } else if (key == 'd') {
      setTempoInBPM(getTempoInBPM() - 10)
      println "New tempo: ${getTempoInBPM()}"
    }
  }
}
MyApp.start("MyApp")
