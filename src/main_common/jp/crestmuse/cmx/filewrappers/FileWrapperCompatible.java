package jp.crestmuse.cmx.filewrappers;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public interface FileWrapperCompatible {
  String getFileName();
  void write(OutputStream out) throws IOException, SAXException;
  void write(Writer writer)throws IOException, SAXException;
  void writefile(File file) throws IOException, SAXException;
  void writeGZippedFile(File file) throws IOException, SAXException;
}