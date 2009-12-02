package jp.crestmuse.cmx.amusaj.filewrappers;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.amusaj.sp.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class AmusaDataSet<D extends TimeSeriesCompatible>
  implements AmusaDataSetCompatible<D> {
//  private Map<String,String> header;
  private AmusaParameterSet params;
  private List<D> data;
//  private AmusaXMLWrapper wrapper;
  String fmt;

  AmusaDataSet() {
//    header = new HashMap<String,String>();
    params = AmusaParameterSet.getInstance();
    data = new ArrayList<D>();
  }

  public AmusaDataSet(String fmt) {
//    this.wrapper = wrapper;
    this.fmt = fmt;
    params = AmusaParameterSet.getInstance();
//    header = new HashMap<String,String>();
    data = new ArrayList<D>();
  }

/*
  public AmusaDataSet(String fmt, AmusaParameterSet params) {
    this.fmt = fmt;
    this.params = params;
//    this.header = header;
    data = new ArrayList<D>();
  }
*/

/*
  public AmusaDataSet(String fmt, Map<String,String> header) {
    this.fmt = fmt;
    this.header = header;
    data = new ArrayList<D>();
  }
*/

/*********************************************************************
 *Returns the item corresponding to the specified key in the header. 
 *<br>
 *ヘッダ内の指定されたキーに対応する項目を返します. 
 *********************************************************************/
  public String getHeader(String category, String key) {
    return params.getParam(category, key);
//    return header.get(category + ":" + key);
  }

/*********************************************************************
 *Returns in integer the item corresponding to the specified key 
 *in the header. <br>
 *ヘッダ内の指定されたキーに対応する項目をinteger型で返します. 
 *********************************************************************/
  public int getHeaderInt(String category, String key) {
    return Integer.parseInt(getHeader(category, key));
  }

/*********************************************************************
 *Returns in double the item corresponding to the specified key 
 *in the header. <br>
 *ヘッダ内の指定されたキーに対応する項目をdouble型で返します. 
 *********************************************************************/
  public double getHeaderDouble(String category, String key) {
    return Double.parseDouble(getHeader(category, key));
  }

/*********************************************************************
 *Tests if the specified key is contained in the header. <br>
 *指定されたキーがヘッダに含まれているか調べます. 
 *********************************************************************/
  public boolean containsHeaderKey(String category, String key) {
    return params.containsParam(category, key);
//    return header.containsKey(key);
  }

  void setHeader(String key, String value) {
    params.setParam(key, value);
  }

  
/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String category, String key, String value) {
    params.setParam(category, key, value);
//    header.put(category + ":" + key, value);
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String category, String key, int value) {
    setHeader(category, key, String.valueOf(value));
//    header.put(key, String.valueOf(value));
  }

/********************************************************************
 *Maps the specfied key to the specifed value in the header. <br>
 *ヘッダにおいて, 指定されたキーに指定された値をマッピングします. 
 ********************************************************************/
  public void setHeader(String category, String key, double value) {
    setHeader(category, key, String.valueOf(value));
//    header.put(key, String.valueOf(value));
  }

/*
  public void setHeaders(Map<String,Object> map) {
    Set<Map.Entry<String,Object>> entrySet = map.entrySet();
    for (Map.Entry<String,Object> e : entrySet)
      header.put(e.getKey(), e.getValue().toString());
  }
       
  public void setHeaders(Map<String,Object> map, String... keys) {
    for (String key : keys) 
      header.put(key, map.get(key).toString());
  }       
*/

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

  public AmusaXMLWrapper toWrapper() throws IOException,InterruptedException {
//    try {
      AmusaXMLWrapper wrapper = 
        (AmusaXMLWrapper)CMXFileWrapper.createDocument("amusaxml");
      wrapper.setAttribute("format", fmt);
      wrapper.addChild("header");
      Set<String> headerKeys = params.keySet();
//      Set<Map.Entry<String,String>> header = this.header.entrySet();
      for (String key : headerKeys) {
        wrapper.addChild("meta");
        wrapper.setAttribute("name", key);
        wrapper.setAttribute("content", params.getParam(key));
        wrapper.returnToParent();
      }
      wrapper.returnToParent();
      for (D d : data) {
        int nFrames = 0;
        QueueReader<? extends SPElement> queue = d.getQueueReader();
        StringBuilder sb = new StringBuilder();
	while (true) {
	    SPElement elem = queue.take();
	    if (elem instanceof SPTerminator) 
		break;
	    else if (elem instanceof SPElementEncodable) {
		if (sb.length() > 0) sb.append("\n");
		sb.append(((SPElementEncodable)elem).encode());
		nFrames++;
	    } else 
		throw new UnsupportedOperationException("The objects should be SPElementEncodable to be written in an XML format.");
	}
	//        SPElement elem = queue.take();
	//        sb.append(elem.encode());
	//        //while (elem.hasNext()) {
	//        while (!(elem instanceof SPTerminator)) {
	//          elem = queue.take();
	//          sb.append("\n").append(elem.encode());
	//        }
        wrapper.addChild("data");
        Iterator<Map.Entry<String,String>> it = d.getAttributeIterator();
        while (it.hasNext()) {
          Map.Entry<String,String> e = it.next();
          wrapper.setAttribute(e.getKey(), e.getValue());
        }
        wrapper.setAttribute("frames", nFrames);
        if (d.dim() > 0) wrapper.setAttribute("dim", d.dim());
//      if (d.timeunit() > 0) wrapper.setAttribute("timeunit", d.timeunit());
        wrapper.addText(sb.toString());
        wrapper.returnToParent();
      }
      wrapper.finalizeDocument();
      return wrapper;
//    } catch (InterruptedException e) {}
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
      write(new PrintStreamWrapper(new PrintStream(out)));
  }

  public void write(Writer writer)throws IOException {
      write(new PrintWriterWrapper(new PrintWriter(writer)));
  }

  public void writefile(File file) throws IOException {
      write(new PrintStreamWrapper(new PrintStream(file)));
  }

  public void writeGZippedFile(File file) throws IOException {
    write(new GZIPOutputStream(new BufferedOutputStream(
                                 new FileOutputStream(file))));
  }

  private void write(Printable p) throws IOException {
    try {
      p.println("<amusaxml format=\"" + fmt + "\">");
      p.println("  <header>");
//      Set<Map.Entry<String,String>> entrySet = header.entrySet();
      Set<String> headerKeys = params.keySet();
      for (String key : headerKeys)
        p.println("    <meta name=\"" + key
                  + "\" content=\"" + params.getParam(key) + "\" />");
      p.println("  </header>");
      for (D d : data) {
        QueueReader<? extends SPElement> queue = d.getQueueReader();
        List<SPElement> l = new LinkedList<SPElement>();
        SPElement elem;
        do {
          elem = queue.take();
          l.add(elem);
        } while (!(elem instanceof SPTerminator));
        StringBuilder sbAttr = new StringBuilder();
        d.setAttribute("frames", l.size()-1);
        if (d.dim() > 0) d.setAttribute("dim", d.dim());
        Iterator<Map.Entry<String,String>> it = d.getAttributeIterator();
        while (it.hasNext()) {
          Map.Entry<String,String> e = it.next();
          sbAttr.append(" ").append(e.getKey()).append("=\"").append(e.getValue()).append("\"");
        }
        p.println("  <data" + sbAttr.toString() + ">");
        for (SPElement e : l) 
	    if (e instanceof SPTerminator)
		break;
	    else if (e instanceof SPElementEncodable)
		p.println(((SPElementEncodable)e).encode());
	    else
		throw new UnsupportedOperationException("The objects should be SPElementEncodable to be written in an XML format.");
        p.println("  </data>");
      }
      p.println("</amusaxml>");
    } catch (InterruptedException e) {}
  }

  /*
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
        QueueReader<? extends SPElementEncodable> queue = d.getQueueReader();
        List<SPElementEncodable> l =new LinkedList<SPElementEncodable>();
        SPElementEncodable elem;
        do {
	    elem = queue.take();
          l.add(elem);
        //} while (elem.hasNext());
        } while (!(elem instanceof SPTerminator));
        StringBuilder sbAttr = new StringBuilder();
        d.setAttribute("frames", l.size());
        if (d.dim() > 0) d.setAttribute("dim", d.dim());
//        if (d.timeunit() > 0) d.setAttribute("timeunit", d.timeunit());
        Iterator<Map.Entry<String,String>> it = d.getAttributeIterator();
        while (it.hasNext()) {
          Map.Entry<String,String> e = it.next();
          sbAttr.append(" ").append(e.getKey()).append("=\"").append(e.getValue()).append("\"");
        }
        p.println("  <data" + sbAttr.toString() + ">");
        for (SPElementEncodable e : l)
          p.println(e.encode());
        p.println("  </data>");
      }
      p.println("</amusaxml>");
    } catch (InterruptedException e) {}
  }
  */


}
