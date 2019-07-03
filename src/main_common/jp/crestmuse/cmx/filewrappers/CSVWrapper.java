package jp.crestmuse.cmx.filewrappers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import org.xml.sax.SAXException;

public class CSVWrapper implements FileWrapperCompatible,Iterable<List<String>> {

  private String fileName;
  private List<List<String>> sheet;

  public CSVWrapper() {
    fileName = "";
    sheet = new ArrayList<List<String>>();
  }

  public CSVWrapper(String fileName) {
    this.fileName = fileName;
    sheet = new ArrayList<List<String>>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(fileName));
      String line;
      while ((line = in.readLine()) != null) {
        List<String> row = new ArrayList<String>();
        for (String s : line.split(","))
          row.add(s);
        sheet.add(row);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addRow() {
    sheet.add(new ArrayList<String>());
  }

  public List<String> getRow(int row) {
    return sheet.get(row);
  }

    public Iterator<List<String>> iterator() {
	return sheet.iterator();
    }

  public void addValue(int row, String value) {
    getRow(row).add(value);
  }

  public String getFileName() {
    return fileName;
  }

  public void write(OutputStream out) throws IOException, SAXException {
    for (List<String> row : sheet) {
      for (String s : row)
        out.write((s + ",").getBytes());
      out.write(System.getProperty("line.separator").getBytes());
    }
    out.close();
  }

  public void write(Writer writer) throws IOException, SAXException {
    for (List<String> row : sheet) {
      for (String s : row)
        writer.write(s + ",");
      writer.write(System.getProperty("line.separator"));
    }
    writer.close();
  }

  public void writeGZippedFile(File file) throws IOException, SAXException {
    write(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(
        file))));
  }

  public void writefile(File file) throws IOException, SAXException {
    write(new FileWriter(file));
  }

  public static void main(String[] args) {
    try {
      CSVWrapper w = new CSVWrapper("a.csv");
      w.write(System.out);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
