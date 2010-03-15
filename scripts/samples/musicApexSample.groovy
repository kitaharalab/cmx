import jp.crestmuse.cmx.filewrappers.*

MusicXMLWrapper musicxml = CMXFileWrapper.readfile("renconsample-s001.xml")
MusicApexDataSet mads = new MusicApexDataSet(musicxml)
mads.createTopLevelGroup(true)
mads.setAspect("hoge")
NoteGroup topgroup = mads.topgroup()
List allnotes = topgroup.getAllNotes()
topgroup.setApex(allnotes[4])
topgroup.makeSubgroup([allnotes[0], allnotes[1], allnotes[2], allnotes[3]])
topgroup.makeSubgroup([allnotes[4], allnotes[5], allnotes[6]])
mads.toWrapper().write(System.out)
