package jp.crestmuse.cmx.filewrappers;
import java.io.*;
import org.xml.sax.*;

public interface FileWrapperCompatible {
  String getFileName();
  void write(OutputStream out) throws IOException, SAXException;
  void write(Writer writer)throws IOException, SAXException;
  void writefile(File file) throws IOException, SAXException;
  void writeGZippedFile(File file) throws IOException, SAXException;
}