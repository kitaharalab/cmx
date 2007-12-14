package jp.crestmuse.cmx.commands;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

public abstract class CMXCommandForGUI extends CMXCommand {
  int getLeastNumOfArgs() {
    return 0;
  }
  void runAll() throws IOException, ParserConfigurationException, 
    TransformerException, SAXException, InvalidFileTypeException, 
    InvalidOptionException {
    preproc();
    run();
    postproc();
  }
  protected String[] getFileList() {
    return super.getFileList();
  }
}