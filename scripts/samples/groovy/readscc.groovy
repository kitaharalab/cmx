import jp.crestmuse.cmx.filewrappers.*

FILENAME="sample_scc.xml"

CMXFileWrapper.readfile(FILENAME).eachnote { note, scc ->
  println note
}
