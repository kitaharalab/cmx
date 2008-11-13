package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class AmusaDataSet<D extends TimeSeriesCompatible>
  implements AmusaDataSetCompatible<D> {
  private Map<String,String> header;
  private List<D> data;
//  private AmusaXMLWrapper wrapper;
  String fmt;

  AmusaDataSet() {
    header = new HashMap<String,String>();
    data = new ArrayList<D>();
  }
  
  public AmusaDataSet(String fmt) {
//    this.wrapper = wrapper;
    this.fmt = fmt;
    header = new HashMap<String,String>();
    data = new ArrayList<D>();
  }

  public AmusaDataSet(String fmt, Map<String,String> header) {
    this.fmt = fmt;
    this.header = header;
    data = new ArrayList<D>();
  }

/*********************************************************************
 *Returns the item corresponding to the specified key in the header. 
 *<br>
 *ヘッダ内の指定されたキーに対応する項目を返します. 
 *********************************************************************/
  public String getHeader(String key) {
    return header.get(key);
  }

/*********************************************************************
 *Returns in integer the item corresponding to the specified key 
 *in the header. <br>
 *ヘッダ内の指定されたキーに対応する項目をinteger型で返します. 
 *********************************************************************/
  public int getHeaderInt(String key) {
    return Integer.parseInt(getHeader(key));
  }

/*********************************************************************
 *Returns in double the item corresponding to the specified key 
 *in the header. <br>
 *ヘッダ内の指定されたキーに対応する項目をdouble型で返します. 
 *********************************************************************/
  public double getHeaderDouble(String key) {
    return Double.parseDouble(getHeader(key));
  }

/*********************************************************************
 *Tests if the specified key is contained in the header. <br>
 *指定されたキーがヘッダに含まれているか調べます. 
 *********************************************************************/
  public boolean containsHeaderKey(String key) {
    return header.containsKey(key);
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String key, String value) {
    header.put(key, value);
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String key, int value) {
    header.put(key, String.valueOf(value));
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String key, double value) {
    header.put(key, String.valueOf(value));
  }

  public void setHeaders(Map<String,Object> map) {
    Set<Map.Entry<String,Object>> entrySet = map.entrySet();
    for (Map.Entry<String,Object> e : entrySet)
      header.put(e.getKey(), e.getValue().toString());
  }
       
  public void setHeaders(Map<String,Object> map, String... keys) {
    for (String key : keys) 
      header.put(key, map.get(key).toString());
  }       

/********************************************************************
 *Adds data. <br>
 *データを追加します. 
 ********************************************************************/
  public void add(D d) {
    data.add(d);
  }

  public List<D> getDataList() {
    return data;
  }

  public AmusaXMLWrapper toWrapper() throws IOException {
    AmusaXMLWrapper wrapper = 
    (AmusaXMLWrapper)CMXFileWrapper.createDocument("amusaxml");
    wrapper.setAttribute("format", fmt);
    wrapper.addChild("header");
    Set<Map.Entry<String,String>> header = this.header.entrySet();
    for (Map.Entry<String,String> e : header) {
      wrapper.addChild("meta");
      wrapper.setAttribute("name", e.getKey());
      wrapper.setAttribute("content", e.getValue());
      wrapper.returnToParent();
    }
    wrapper.returnToParent();
    for (D d : data) {
      int nFrames = d.frames();
      QueueReader<? extends Encodable> queue = d.getQueueReader();
      StringBuilder sb = new StringBuilder();
      try {
        for (int n = 0; n < nFrames-1; n++)
          sb.append(queue.take().encode()).append("\n");
        sb.append(queue.take().encode());
      } catch (InterruptedException e) {}
      wrapper.addChild("data");
      Iterator<Map.Entry<String,String>> it = d.getAttributeIterator();
      while (it.hasNext()) {
        Map.Entry<String,String> e = it.next();
        wrapper.setAttribute(e.getKey(), e.getValue());
      }
      wrapper.setAttribute("frames", nFrames);
      if (d.dim() > 0) wrapper.setAttribute("dim", d.dim());
      if (d.timeunit() > 0) wrapper.setAttribute("timeunit", d.timeunit());
      wrapper.addText(sb.toString());
      wrapper.returnToParent();
    }
    wrapper.finalizeDocument();
    return wrapper;
  }

/*
  public void addElementsToWrapper() {
    wrapper.addChild("header");
    Set<Map.Entry<String,String>> header = this.header.entrySet();
    for (Map.Entry<String,String> e : header) {
      wrapper.addChild("meta");
      wrapper.setAttribute("name", e.getKey());
      wrapper.setAttribute("content", e.getValue());
      wrapper.returnToParent();
    }
    wrapper.returnToParent();
    for (D d : data) 
      wrapper.addDataElement(d);
  }
*/

  public String getFileName() {
    throw new UnsupportedOperationException();
  }

  public void write(OutputStream out) throws IOException {
      write(new PrintStream(out));
  }

  public void write(Writer writer)throws IOException {
    write(new PrintWriter(writer));
  }

  public void writefile(File file) throws IOException {
    write(new PrintStream(file));
  }

  public void writeGZippedFile(File file) throws IOException {
    write(new GZIPOutputStream(new BufferedOutputStream(
                                 new FileOutputStream(file))));
  }

  private void write(PrintStream p) throws IOException {
    try {
      p.println("<amusaxml format=\"" + fmt + "\">");
      p.println("  <header>");
      Set<Map.Entry<String,String>> entrySet = header.entrySet();
      for (Map.Entry<String,String> e : entrySet)
        p.println("    <meta name=\"" + e.getKey() 
                   + "\" content=\"" + e.getValue() + "\" />");
      p.println("  </header>");
      for (D d : data) {
        QueueReader<? extends Encodable> queue = d.getQueueReader();
        Encodable first = queue.take();
        StringBuilder sbAttr = new StringBuilder();
        int nFrames = d.frames();
        d.setAttribute("frames", nFrames);
        if (d.dim() > 0) d.setAttribute("dim", d.dim());
        if (d.timeunit() > 0) d.setAttribute("timeunit", d.timeunit());
        Iterator<Map.Entry<String,String>> it = d.getAttributeIterator();
        while (it.hasNext()) {
          Map.Entry<String,String> e = it.next();
          sbAttr.append(" ").append(e.getKey()).append("=\"").append(e.getValue()).append("\"");
        }
        p.println("  <data" + sbAttr.toString() + ">");
        p.println(first.encode());
        for (int n = 1; n < nFrames; n++)
          p.println(queue.take().encode());
        p.println("  </data>");
      }
      p.println("</amusaxml>");
    } catch (InterruptedException e) {}
  }

  private void write(PrintWriter p) throws IOException {
    try {
      p.println("<amusaxml format=\"" + fmt + "\">");
      p.println("  <header>");
      Set<Map.Entry<String,String>> entrySet = header.entrySet();
      for (Map.Entry<String,String> e : entrySet)
        p.println("    <meta name=\"" + e.getKey() 
                   + "\" content=\"" + e.getValue() + "\" />");
      p.println("  </header>");
      for (D d : data) {
        QueueReader<? extends Encodable> queue = d.getQueueReader();
        Encodable first = queue.take();
        StringBuilder sbAttr = new StringBuilder();
        int nFrames = d.frames();
        d.setAttribute("frames", nFrames);
        if (d.dim() > 0) d.setAttribute("dim", d.dim());
        if (d.timeunit() > 0) d.setAttribute("timeunit", d.timeunit());
        Iterator<Map.Entry<String,String>> it = d.getAttributeIterator();
        while (it.hasNext()) {
          Map.Entry<String,String> e = it.next();
          sbAttr.append(" ").append(e.getKey()).append("=\"").append(e.getValue()).append("\"");
        }
        p.println("  <data" + sbAttr.toString() + ">");
        p.println(first.encode());
        for (int n = 1; n < nFrames; n++)
          p.println(queue.take().encode());
        p.println("  </data>");
      }
      p.println("</amusaxml>");
    } catch (InterruptedException e) {}
  }



}
