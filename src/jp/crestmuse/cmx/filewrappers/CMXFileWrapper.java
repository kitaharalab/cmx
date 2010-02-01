package jp.crestmuse.cmx.filewrappers;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import org.xml.sax.*;
import org.apache.xml.serialize.*;
//import org.apache.xpath.*;
import org.apache.xerces.util.*;
import jp.crestmuse.cmx.misc.*;
import jp.crestmuse.cmx.xml.processors.*;
import jp.crestmuse.cmx.math.*;

/**********************************************************************
 *<p>The abstract class <tt>CMXFileWrapper</tt> is the common superclass 
 *of the classes that wrap XML documents; 
 *In other words, all XML documents in CrestMuseXML are handled via 
 *subclasses of this class. 
 *This class holds an XML document as a Document Object Model (DOM), 
 *and processes it using a separate XML parser.
 *This DOM processing is encapsulated by methods provided by this class.</p>
 *
 *<p>This requires an XML parser compliant with JAXP (Xerces is prefered) and 
 *an XSLT processor (only Xalan is accepted at the moment). </p>

 *<p>抽象クラス<tt>CMXFileWrapper</tt>は, XMLドキュメントをラップする各クラスの
 *スーパークラスです.
 *つまり, CrestMuseXMLに含まれるすべてのXMLドキュメントは, このクラスの
 *サブクラスを介して扱われます.
 *このクラスは, XMLドキュメントをDocument Object Model (DOM)として保持し, 
 *別のXML Parserを用いてDOMを処理します. 
 *このDOMの処理は, このクラスで提供されるメソッドがカプセル化します.</p>
 *
 *<p>本APIでは，処理のシンプルさを保つため, 同一のCMXFileWrapperオブジェクトに対して, 
 *要素の追加と要素からの情報の取り出しを同時に行うことは認めていません. 
 *readfileメソッドを用いてファイルから読み込まれたCMXFileWrapperオブジェクトは
 *immutableとなり, 要素を追加することはできません. 
 *また, 要素を追加するために生成した空オブジェクトは, 
 *finalize処理(finalizeDocumentメソッドを用いる)を行うまで, 
 *後述のNodeInterfaceを取得することはできません. 
 *finalize処理後は, 要素の追加はできなくなります.
 *(ただし, 現在は開発中のため, 便宜的にこのimmutablityは不完全なものに
 *なっています.)</p> 
 *
 *<p>XMLドキュメントをファイルから読み込むにはreadfileメソッドを用います. 
 *readfileメソッドはstaticなメソッドで, ファイルを読み込んで
 *それをラップするCMXFileWrapperオブジェクトを生成して返します. 
 *返されるオブジェクトは正確にはCMXFileWrapperのサブクラスのオブジェクトで, 
 *たとえばMusicXMLドキュメントを読み込んだなら, MusicXMLWrapperオブジェクトが
 *返されます. 
 *よって, たとえばMusicXMLドキュメントをファイルから読み込む部分の記述は
 *以下のようになります. 
 *<pre>
 *  MusicXMLWrapper musicxml 
 *    = (MusicXMLWrapper)CMXFileWrapper.readfile(filename);
 *</pre>
 *読み込んだファイルからCMXFileWrapperのどのサブクラスのインスタンスを生成するかは, 
 *読み込んだドキュメントのトップレベルタグを見て判断します. 
 *トップレベルタグと対応するクラスの組が内部のハッシュマップに格納されており, 
 *これを用いて処理がなされます. CMX APIに最初から提供されているクラスはすでに
 *登録されていますが, ユーザが独自にXML形式と対応するクラスを作った場合, 
 *addClassTableメソッドを用いて追加することが可能です. 
 *</p>
 *
 *<p>
 *ファイルを読み込んでCMXFileWrapperオブジェクトを取得した後は, 
 *NodeInterfaceクラスを継承した各種クラスを通して情報を取得します. 
 *これは, (通常は)XMLドキュメントのある1つの要素をラップし, そこから情報を取り出す
 *メソッドを提供します. 
 *通常は, CMXFileWrapperの各サブクラスにおける内部クラスとして定義されます. 
 *たとえば, MusicXMLWrapperには, Part, Measure, Noteなどの内部クラスがあり, 
 *それぞれ, part, measure, note要素からの情報の取り出しをサポートします. 
 *詳細は各サブクラスの説明をご覧ください. 
 *</p>
 *
 *<p>
 *空ドキュメントを生成するには, createDocumentメソッドを使って, 
 *たとえば次のようにします. 
 *<pre>
 *  SCCXMLWrapper sccxml
 *    = (SCCXMLWrapper)CMXFileWrapper.createDocument("scc");
 *</pre>
 *ここでもreadfileメソッドと同様に, トップレベルタグを用いて生成すべきクラスを
 *判断しています. ここの例では, トップレベルタグ名を直接書いていますが, 
 *スペルミスを防ぐため, SCCXMLWrapper.TOP_TAGのような書き方が奨励されます. 
 *</p>
 *
 *<p>要素の追加は, 現在のところ, シーケンシャルに要素を追加する手段が
 *提供されています. ファイルラッパのインスタンスを生成した時点では, 
 *トップレベルタグが「現在の要素」となっており, 「現在の要素」に対して
 *子要素やテキストなどを追加するメソッド(addChild, addText, etc.)を
 *利用して要素を増やしていきます. 
 *子要素を追加すると, 「現在の要素」が追加された要素に移り, 
 *さらなる子要素を追加できます. 
 *returnToParentメソッドで「現在の要素」をその親要素に戻すことができます. 
 *その他にも, addChildAndText, addSibling, setAttributeメソッドを
 *利用できます.
 *たとえば, 
 *<pre>
 *  &lt;note&gt;
 *    &lt;pitch&gt;
 *      &lt;step&gt;C&lt;/step&gt;
 *      &lt;octage&gt;4&lt;/octave&gt;
 *    &lt;/pitch&gt;
 *    &lt;duration&gt;4&lt;/duration&gt;
 *  &lt;/note&gt;
 *</pre>
 *を追加するには, 
 *<pre>
 *  xml.addChild("note");
 *  xml.addChild("pitch");
 *  xml.addChildAndText("step", "C");
 *  xml.addChildAndText("octave", "4");
 *  xml.returnToChild();
 *  xml.addChildAndText("duration", "4");
 *  xml.returnToChild();
 *</pre>
 *とします. 
 *データの種類によっては, このようなシーケンシャルな方法が取れない場合が
 *あると思われますので, その際には追加すべきデータを一時的に格納する
 *クラスを用意しています. たとえばDeviationDataSetなどが該当します. </p>
 *
 *<p>JAXPに対応したXMLパーサ(Xerces)とXSLTプロセッサ
 *(Xalan)が必要です. </p>
 *
 *@author Tetsuro Kitahara (t.kitahara@ksc.kwansei.ac.jp)
 *@version 0.21
 *********************************************************************/
public abstract class CMXFileWrapper implements FileWrapperCompatible {

  private static DocumentBuilderFactory builderFactory;
  private static DocumentBuilder builder;
  private static DOMImplementation domImpl;
  private static XPathFactory xpathFactory;
  private static XPath xpath;

//  private static DoubleArrayFactory doubleArrayFactory = null;

  public static String catalogFileName = null;

  private static final String PACKAGE_BASE = "jp.crestmuse.cmx.filewrappers";
  private static final String AMUSA_PACKAGE_BASE = 
    "jp.crestmuse.cmx.amusaj.filewrappers";
  private static final Map<String,Class> CLASS_TABLE;
  private static final Map<String,String> DTD_PUBLIC_ID_TABLE;
  private static final Map<String,String> DTD_SYSTEM_ID_TABLE;

  private Document doc;
  private Node currentNode = null;
  private Node parentNode = null;
  private File file = null;
//  private String filename = null;
    private Node prevCurrentNode = null;
    private Node prevParentNode = null;

  private static List<String> paths = new LinkedList<String>();

  private boolean finalized = false;

  public static final NodeLinkManager linkmanager = new NodeLinkManager();

  static {
    try {
      paths.add(".");
      CLASS_TABLE = new HashMap<String,Class>();
      DTD_PUBLIC_ID_TABLE = new HashMap<String,String>();
      DTD_SYSTEM_ID_TABLE = new HashMap<String,String>();
      addClassTable("config", 
                    PACKAGE_BASE + "." + "ConfigXMLWrapper");
      addDocumentTypeTable("config", 
                           "-//CrestMuse//DTD CrestMuseXML ConfigXML//EN",
                           "http://www.crestmuse.jp/cmx/dtds/config.dtd");
      addClassTable("score-partwise", 
                    PACKAGE_BASE + "." + "MusicXMLWrapper");
      addDocumentTypeTable("score-partwise", 
                           "-//Recordare//DTD MusicXML 1.1 Partwise//EN", 
                           "http://www.musicxml.org/dtds/partwise.dtd");
      addClassTable("MIDIFile", PACKAGE_BASE + "." + "MIDIXMLWrapper");
      addDocumentTypeTable("MIDIFile", 
                           "-//Recordare//DTD MusicXML 1.1 MIDI//EN", 
                           "http://www.musicxml.org/dtds/midixml.dtd");
      addClassTable("scc", PACKAGE_BASE + "." + "SCCXMLWrapper");
      addDocumentTypeTable("scc", 
                           "-//CrestMuse//DTD CrestMuseXML SCCXML//EN",
                           "http://www.crestmuse.jp/cmx/dtds/sccxml.dtd");
      addClassTable("deviation",
                    PACKAGE_BASE+"."+"DeviationInstanceWrapper");
      addDocumentTypeTable("deviation", 
                           "-//CrestMuse//DTD CrestMuseXML " + 
                           "DeviationInstanceXML//EN", 
                           "http://www.crestmuse.jp/cmx/dtds/deviation.dtd");
//      addClassTable("riff-wave", 
//                    AMUSA_PACKAGE_BASE + "." + "WAVXMLWrapper");
//      addDocumentTypeTable("riff-wave", 
//                           "-//CrestMuse//DTD CrestMuseXML 0.30 " + 
//                           "WAVXML//EN", 
//                           "http://www.crestmuse.jp/cmx/dtds/wavxml.dtd");
//      addClassTable("spd", 
//                    AMUSA_PACKAGE_BASE + "." + "SPDXMLWrapper");
//      addDocumentTypeTable("spd", 
//                           "-//CrestMuse//DTD CrestMuseXML 0.40 " + 
//                           "SPDXML//EN", 
//                           "http://www.crestmuse.jp/cmx/dtds/spdxml.dtd");
//      addClassTable("fpd", 
//                    AMUSA_PACKAGE_BASE + "." + "FPDXMLWrapper");
//      addDocumentTypeTable("fpd", 
//                           "-//CrestMuse//DTD CrestMuseXML 0.40 " + 
//                           "FPDXML//EN", 
//                           "http://www.crestmuse.jp/cmx/dtds/fpdxml.dtd");
//      addClassTable("tbd",
//                    AMUSA_PACKAGE_BASE + "." + "TBDXMLWrapper");
//      addDocumentTypeTable("tbd",
//                           "-//CrestMuse//DTD CrestMuseXML 0.40 " +
//                    "TBDXML//EN", 
//                    "http://www.crestmuse.jp/cmx/dtds/tbdxml.dtd");
//      addClassTable("igram", 
//                    AMUSA_PACKAGE_BASE + "." + "IGRAMXMLWrapper");
//      addDocumentTypeTable("igram", 
//                           "-//CrestMuse//DTD CrestMuseXML 0.40 " + 
//                           "IGRAMXML//EN", 
//                           "http://www.crestmuse.jp/cmx/dtds/igramxml.dtd");
      addClassTable("amusaxml", 
                    AMUSA_PACKAGE_BASE + "." + "AmusaXMLWrapper");
      addDocumentTypeTable("amusaxml", 
                           "-//CrestMuse//DTD CrestMuseXML " +
                           "AmusaXML//EN", 
                           "http://www.crestmuse.jp/cmx/dtds/amusaxml.dtd");
      addClassTable("Mpeg7", 
                    AMUSA_PACKAGE_BASE + "." + "MPEG7Wrapper");
      addClassTable("script", 
                    AMUSA_PACKAGE_BASE + "." + "AmusaScriptWrapper");
      addDocumentTypeTable("script", 
                           "-//CrestMuse//DTD CrestMuseXML " + 
                           "AmusaScriptXML//EN", 
                           "http://www.crestmuse.jp/cmx/dtds/amusascript.dtd");
      
      //090313 MusicApex動作テストのため暫定的に追記　ここから
      addClassTable("music-apex", PACKAGE_BASE+"."+"MusicApexWrapper");
      addDocumentTypeTable("music-apex", "-//CrestMuse//DTD CrestMuseXML " +
          "MusicApexXML//EN",
          "music-apex.dtd");      
      //ここまで
      
    } catch (ClassNotFoundException e) {
      throw new ExternalLibraryException(e.toString());
    }
  }

  static String getDefaultPackageName() {
    return PACKAGE_BASE;
  }

  public static void addClassTable(String toptagname, String classname) 
    throws ClassNotFoundException {
    CLASS_TABLE.put(toptagname, Class.forName(classname));
  }

  public static void addDocumentTypeTable(String toptagname, 
                                          String publicId, 
                                          String systemId) {
    DTD_PUBLIC_ID_TABLE.put(toptagname, publicId);
    DTD_SYSTEM_ID_TABLE.put(toptagname, systemId);
  }
				

  private static void initXMLProcessors()
			throws ParserConfigurationException, SAXException {
    if (builderFactory == null) {
      builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(true);
      builderFactory.setIgnoringComments(true);
      builderFactory.setIgnoringElementContentWhitespace(true);
      //      builderFactory.setValidating(true);
      builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", 
				 false);
    }
    if (builder == null) {
      builder = builderFactory.newDocumentBuilder();
      if (catalogFileName != null) {
        XMLCatalogResolver resolver = new XMLCatalogResolver(
          new String[]{catalogFileName});
        builder.setEntityResolver(resolver);
      }
    }
    if (domImpl == null)
      domImpl = builder.getDOMImplementation();
      if (xpathFactory == null)
        xpathFactory = XPathFactory.newInstance();
      if (xpath == null)
        xpath = xpathFactory.newXPath();
   }

//  protected void setDoubleArrayFactory(DoubleArrayFactory factory) {
//    doubleArrayFactory = factory;
//  }

/*
  protected DoubleArrayFactory getDoubleArrayFactory() {
    if (doubleArrayFactory == null) {
      try {
        doubleArrayFactory = DoubleArrayFactory.getFactory();
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(e.toString());
      } catch (InstantiationException e) {
        throw new IllegalStateException(e.toString());
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e.toString());
      }
    }
    return doubleArrayFactory;
  }
*/

  private static CMXFileWrapper createInstance(String toptagname)
			throws InvalidFileTypeException { 
    try {
      initXMLProcessors();
      CMXFileWrapper f = 
	(CMXFileWrapper)(CLASS_TABLE.get(toptagname)).newInstance();
      return f;
    } catch (InstantiationException e) {
      throw new InvalidFileTypeException(e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ProgramBugException(e.getMessage());
    } catch (NullPointerException e) {
      throw new InvalidFileTypeException();
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    } catch (SAXException e) {
      throw new XMLException(e);
    }
  }

  /**********************************************************************
   *<p>Returns the document wrapped by this object
   *as an <tt>org.w3c.dom.Document</tt> object. </p>
   *<p>このオブジェクトが保持しているドキュメントを
   *<tt>org.w3c.dom.Document</tt>オブジェクトとして返します.</p>
   *********************************************************************/
  protected final Document getDocument() {
    return doc;
  }

  /**********************************************************************
   *<p>Creates an empty document with the specified top-tag name.</p>
   *<p>指定された名前のトップタグを持つ空のドキュメントを生成します.</p>
   *@exception jp.crestmuse.cmx.filewrappers.InvalidFileTypeException ...
   *@exception javax.xml.parsers.ParserConfigurationException ...
   *@exception org.xml.sax.SAXException ...
   *********************************************************************/
  public static CMXFileWrapper createDocument(String toptagname) 
    throws InvalidFileTypeException {
  //			ParserConfigurationException, SAXException {
    try {
      initXMLProcessors();
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    } catch (SAXException e) {
      throw new XMLException(e);
    }
    DocumentType dt;
    if (DTD_PUBLIC_ID_TABLE.containsKey(toptagname))
      dt = domImpl.createDocumentType(toptagname, 
                                      DTD_PUBLIC_ID_TABLE.get(toptagname), 
                                      DTD_SYSTEM_ID_TABLE.get(toptagname));
    else 
      dt = null;
    Document doc = domImpl.createDocument(null, toptagname, dt);
    CMXFileWrapper f = createInstance(toptagname);
    f.doc = doc;
    f.currentNode = doc.getDocumentElement();
    f.init();
    return f;
  }

  /**********************************************************************
   *<p>Forbids further element addition and prepares for getting 
   *information</p>
   *<p>さらなる要素の追加をできなくし, 情報の取り出しのための準備をします. </p>
   *********************************************************************/
  public final void finalizeDocument() throws IOException {
    finalized = true;
    analyze();
  }

  public static final void addPathFirst(String s) {
    paths.add(0, s);
  }

  public static final void addPathLast(String s) {
    paths.add(s);
  }

//  public final void read(InputStream in) throws IOException, SAXException {
//    doc = builder.parse(in);
//  }

  public static CMXFileWrapper read(InputStream in) throws IOException {
    try {
      initXMLProcessors();
      return wrap(builder.parse(in), null, null);
    } catch (SAXException e) {
      throw new XMLException(e);
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    }
  }

  /**********************************************************************
   *<p>Reads the specified file. After that, the analyze method is 
   *automatically called.</p>
   *<p>指定された名前のファイルを読み込みます. その後はanalyzeメソッドが
   *自動的に呼ばれます. </p>
   *@exception java.io.IOException ...
   *@exception javax.xml.parsers.ParserConfigurationException ...
   *@exception org.xml.sax.SAXException ...
   *********************************************************************/
  public static CMXFileWrapper readfile(String filename) 
    throws IOException {
    return readfile(filename, null);
  }

  /**********************************************************************
   *<p>Reads the specified file. </p>
   *<p>指定された名前のファイルを読み込みます. 
   *readfile(String)と同様にファイル読み込み後に, analyzeメソッドが呼ばれますが, 
   *その直前に指定されたイニシャライザが実行されます. </p>
   *@exception java.io.IOException ...
   *@exception javax.xml.parsers.ParserConfigurationException ...
   *@exception org.xml.sax.SAXException ...
   *********************************************************************/
  public static CMXFileWrapper readfile(String filename, CMXInitializer init) 
    throws IOException {
    return readfile(new File(filename), init);
  }

  public static CMXFileWrapper readfile(File file, CMXInitializer init) 
    throws IOException {
    try {
      initXMLProcessors();
      Document doc;
      String filename = file.getName();
      if (!(file.exists())) {
        File f;
        for (String path : paths) {
          if ((f = new File(path + File.separator + filename)).exists()) {
            file = f;
            break;
          }
          throw new FileNotFoundException("File not found: " + file.getPath());
        }
      }
      if (filename.endsWith("z") || filename.endsWith("Z")) {
        InputStream in = new GZIPInputStream
          (new BufferedInputStream(new FileInputStream(file)));
        doc = builder.parse(in);
        in.close();
      } else {
        doc = builder.parse(file);
      }
      return wrap(doc, file, init);
    } catch (ParserConfigurationException e) {
      throw new XMLException(e);
    } catch (SAXException e) {
      throw new XMLException(e);
    }
//    String toptagname = doc.getDocumentElement().getTagName();
//    CMXFileWrapper f = createInstance(toptagname);
//    f.filename = file.getPath();
//    f.doc = doc;
//    f.currentNode = doc.getDocumentElement();
//    f.removeBlankTextNodes();
//    //    f.init();
//    f.finalized = true;
//    if (init != null) init.init(f);
//    f.analyze();
//    return f;
  }

  private static CMXFileWrapper wrap(Document doc, File file, 
                                     CMXInitializer init) 
                                     throws IOException {
    String toptagname = doc.getDocumentElement().getTagName();
    CMXFileWrapper f = createInstance(toptagname);
    f.file = file;
    f.doc = doc;
    f.currentNode = doc.getDocumentElement();
    f.removeBlankTextNodes();
    f.finalized = true;
    if (init != null) init.init(f);
    f.analyze();
    return f;
  }

  public static CMXFileWrapper wrap(Document doc) throws IOException {
    return wrap(doc, null, null);
  }

    protected void init() {
	// do nothing
    }

  /**********************************************************************
   *obsolete?
   *<p>Returns the current file name.</p>
   *<p>現在のファイル名を返します, </p>
   *********************************************************************/
  public final String getFileName() {
    return file == null ? null : file.getName();
  }

  public final String getAbsolutePath() {
    return file.getAbsolutePath();
  }

  public final String getParentPath() {
    return file.getParent();
  }

  public final String getPath() {
    return file.getPath();
  }

  public final String getURI() {
    return doc.getDocumentURI();
  }

    public final void println() throws SAXException {
	try {
	    write(System.out);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

  /**********************************************************************
   *<p>Writes the document wrapped by this object 
   *to the specified stream.</p>
   *<p>このオブジェクトが保持するドキュメントを
   *指定されたストリームに書き込みます.</p>
   *@exception java.io.IOException ...
   *@exception org.xml.sax.SAXException ...
   *********************************************************************/
  public final void write(OutputStream out) throws IOException, SAXException {
    OutputFormat fmt = new OutputFormat();
    fmt.setIndent(2);
    XMLSerializer serializer = new XMLSerializer(out, fmt);
    serializer.serialize(doc);
    //    out.close();
  }

  /**********************************************************************
   *<p>Writes the document wrapped by this <tt>CMXFileWrapper</tt> object 
   *to the specified writer.</p>
   *<p>このオブジェクトが保持するドキュメントを指定されたライタに書き込みます.</p>
   *@exception java.io.IOException ...
   *@exception org.xml.sax.SAXException ...
   *********************************************************************/
  public final void write(Writer writer) throws IOException, SAXException {
    OutputFormat fmt = new OutputFormat();
    fmt.setIndent(2);
    XMLSerializer serializer = new XMLSerializer(writer, fmt);
    serializer.serialize(doc);
    //    writer.close();
  }

  /**********************************************************************
   *<p>Writes the document wrapped by this object 
   *to the specified file.</p>
   *<p>この<tt>CMXFileWrapper</tt>オブジェクトが保持するドキュメントを
   *指定された名前のファイルに書き込みます.</p>
   *@exception java.io.IOException ...
   *@exception org.xml.sax.SAXException ...
   *********************************************************************/
  public final void writefile(String filename) 
    throws IOException, SAXException {
      writefile(new File(filename));
      //      write(new FileWriter(new File(filename)));
  }

  public final void writefile(File file)
    throws IOException, SAXException {
      Writer w = new FileWriter(file);
      write(w);
      w.close();
      //    write(new FileWriter(file));
  }

  public final void writeGZippedFile(String filename)
    throws IOException, SAXException {
    writeGZippedFile(new File(filename));
  }

  public final void writeGZippedFile(File file)
    throws IOException, SAXException {
      OutputStream out = new GZIPOutputStream(new BufferedOutputStream
					      (new FileOutputStream(file)));
      write(out);
      out.close();
    //    write(new GZIPOutputStream(new BufferedOutputStream(
    //                                 new FileOutputStream(file))));
  }

  /**********************************************************************
   *<p>Selects nodes based on the specified XPath expression and returns 
   *them as an <tt>org.w3c.dom.NodeList</tt> object.</p>
   *<p>指定されたXPath表現に基づいてノードをセレクトし, 
   *<tt>org.w3c.dom.NodeList</tt>オブジェクトとして返します.</p>
   *********************************************************************/
  protected final NodeList selectNodeList(String xpath) {
    return selectNodeList(doc, xpath);
  }

  /**********************************************************************
   *<p>Selects nodes based on the specified XPath expression with 
   *the specified context node and returns them 
   *as an <tt>org.w3c.dom.NodeList</tt> object.</p>
   *<p>指定されたノードをコンテキストノードとして
   *指定されたXPath表現に基づいてノードをセレクトし, 
   *<tt>org.w3c.dom.NodeList</tt>オブジェクトとして返します.</p>
   *********************************************************************/
  protected final NodeList selectNodeList(Node node, String expr) {
    try {
      checkFinalized();
      return (NodeList)xpath.evaluate(expr, node, XPathConstants.NODESET);
//      return XPathAPI.selectNodeList(node, expr);
    } catch (XPathExpressionException e) {
      throw new XMLException(e);
    }
  }

  protected final void resetXPath() {
    xpath.reset();
  }

  protected final void setNamespaceContext() {
    xpath.setNamespaceContext(new NamespaceContext() {
        public String getNamespaceURI(String prefix) {
          Element e = doc.getDocumentElement();
          if (prefix == null) throw new NullPointerException("Null prefix");
          else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
          else if ("xmlns".equals(prefix)) 
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
          else if ("".equals(prefix) && e.hasAttribute("xmlns"))
            return e.getAttribute("xmlns");
          else if (e.hasAttribute("xmlns:"+prefix)) 
            return e.getAttribute("xmlns:"+prefix);
          else
            return XMLConstants.NULL_NS_URI;
        }
        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
          throw new UnsupportedOperationException();
        }
        // This method isn't necessary for XPath processing either.
        public Iterator getPrefixes(String uri) {
          throw new UnsupportedOperationException();
        }
      });
  }
  
  

  /**********************************************************************
   *<p>Selects a single node based on the specified XPath expression. 
   *The first node that matches the XPath is returned.</p>
   *<p>指定されたXPath表現に基づいてノードを1つセレクトします. 
   *マッチした最初のノードが返されます.</p>
   *********************************************************************/
  protected final Node selectSingleNode(String xpath) {
    return selectSingleNode(doc, xpath);
  }

  /**********************************************************************
   *<p>Selects a single node based on the specified XPath expression 
   *with the specified context node. 
   *The first node that matches the XPath is returned.</p>
   *<p>指定されたノードをコンテキストノードとして
   *指定されたXPath表現に基づいてノードを1つセレクトします. 
   *マッチした最初のノードが返されます.</p>
   *********************************************************************/
  protected final Node selectSingleNode(Node node, String expr) {
    try {
      checkFinalized();
      return (Node)xpath.evaluate(expr, node, XPathConstants.NODE);
//      return XPathAPI.selectSingleNode(node, xpath);
    } catch (XPathExpressionException e) {
      throw new XMLException(e);
    }
  }

  /**********************************************************************
   *<p>Removes all the blank text nodes.</p>
   *<p>すべての空テキストノードを除去します.</p>
   *********************************************************************/
  private final void removeBlankTextNodes() {
    removeBlankTextNodes(doc, null);
  }
  
  private final boolean removeBlankTextNodes(Node node, Node parent) {
    if (node.getNodeType() == Node.TEXT_NODE) {
      if (node.getNodeValue().trim().length() == 0) {
        parent.removeChild(node);
        return true;
      } else {
        return false;
      }
    } else if (node.getNodeType() == Node.ELEMENT_NODE) {
      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) 
        if (removeBlankTextNodes(children.item(i), node))
          i--;
      return false;
    } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
      removeBlankTextNodes(((Document)node).getDocumentElement(), null);
      return false;
    } else {
      return false;
    }
  }

  protected final void addLinks(String xpath, CMXFileWrapper target) 
					throws TransformerException {
    addLinks(xpath, target.getDocument());
  }

  protected final void addLinks(String xpath, Document target) 
					throws TransformerException {
    NodeList nl = selectNodeList(xpath);
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      NodeList linkednodes = 
          SimplifiedXPointerProcessor.getRemoteResource(node, target);
      linkmanager.addLink(node, linkednodes);
    }
  }

  /**********************************************************************
   *<p>Adds a child element with the specified tag name to the current node.  
   *After that, the current node is changed to the added element.</p>
   *<p>現在のノードに指定されたタグ名のエレメントを追加します.
   *追加後は, 追加されたエレメントが現在のノードに設定されます. </p>
   *********************************************************************/
  public final void addChild(String tagname) {
    addChild2(doc.createElement(tagname));
  }

  /**********************************************************************
   *<p>Adds the specified node to the current node. 
   *After that, the current node is changed to the added node.</p>
   *<p>現在のノードに指定されたノードを追加します.
   *追加後は, 追加されたノードが現在のノードに設定されます.</p>
   *********************************************************************/
  public final void addChild(Node child) {
    addChild2(doc.importNode(child, true));
  }

  private void addChild2(Node child) {
    if (!finalized) {
      currentNode.appendChild(child);
      parentNode = currentNode;
      currentNode = child;
    } else {
      throw new IllegalStateException("Document is read only.");
    }
  }

  /**********************************************************************
   *<p>Adds a text node with the specified text to the current node.</p>
   *<p>現在のノードに指定されたテキストを持つテキストノードを追加します.</p>
   *********************************************************************/
  public final void addText(String text) {
    if (!finalized) {
      Text textnode = doc.createTextNode(text);
      currentNode.appendChild(textnode);
    } else {
      throw new IllegalStateException("Document is read only");
    }
//    addChild(textnode);
  }

  /**********************************************************************
   *<p>Adds a text node with the specified integer to the current node.</p>
   *<p>現在のノードに指定された整数値を持つテキストノードを追加します.</p>
   *********************************************************************/
  public final void addText(int text) {
    addText(String.valueOf(text));
  }  

  /**********************************************************************
   *<p>Adds a text node with the specified real number integer to 
   *the current node.</p>
   *<p>現在のノードに指定された実数値を持つテキストノードを追加します.</p>
   *********************************************************************/
  public final void addText(double text) {
    addText(String.valueOf(text));
  }

  /**********************************************************************
   *<p>Adds a child element with the specified tag name and with the 
   *specified text as its child to the current node. 
   *This is equivalent to 
   *"addChild(tagname); addText(text); returnToParent();"</p>
   *<p>現在のノードに, 指定されたテキストを子に持つ指定されたタグ名のエレメントを
   *追加します. 
   *これは, "addChild(tagname); addText(text); returnToParent()"に等価です.</p>
   *********************************************************************/
  public final void addChildAndText(String tagname, String text) {
    addChild(tagname);
    addText(text);
    returnToParent();
  }

  /**********************************************************************
   *<p>Adds a child element with the specified tag name and with the 
   *specified integer as its child to the current node. 
   *This is equivalent to 
   *"addChild(tagname); addText(text); returnToParent();"</p>
   *<p>現在のノードに, 指定された整数値を子に持つ指定されたタグ名のエレメントを
   *追加します. 
   *これは, "addChild(tagname); addText(text); returnToParent()"に等価です.</p>
   *********************************************************************/
  public final void addChildAndText(String tagname, int text) {
    addChild(tagname);
    addText(text);
    returnToParent();
  }

  /**********************************************************************
   *<p>Adds a child element with the specified tag name and with the 
   *specified real number as its child to the current node. 
   *This is equivalent to 
   *"addChild(tagname); addText(text); returnToParent();"</p>
   *<p>現在のノードに, 指定された実数値を子に持つ指定されたタグ名のエレメントを
   *追加します. 
   *これは, "addChild(tagname); addText(text); returnToParent()"に等価です.</p>
   *********************************************************************/
  public final void addChildAndText(String tagname, double text) {
    addChild(tagname);
    addText(text);
    returnToParent();
  }

  /**********************************************************************
   *<p>Adds an attribute to the current node.
   *The current node should be an element node.</p>
   *<p>現在のノードに属性を追加します. 
   *現在のノードはエレメントでなければなりません.</p>
   *********************************************************************/
  public final void setAttribute(String name, String value) {
    if (!finalized)
      ((Element)currentNode).setAttribute(name, value);
    else
      throw new IllegalStateException("Document is read only.");
  }

  /**********************************************************************
   *<p>Adds an attribute to the current node.
   *The current node should be an element node.</p>
   *<p>現在のノードに属性を追加します. 
   *現在のノードはエレメントでなければなりません.</p>
   *********************************************************************/
  public final void setAttribute(String name, int value) {
    setAttribute(name, String.valueOf(value));
  }

  /**********************************************************************
   *<p>Adds an attribute to the current node.
   *The current node should be an element node.</p>
   *<p>現在のノードに属性を追加します. 
   *現在のノードはエレメントでなければなりません.</p>
   *********************************************************************/
  public final void setAttribute(String name, double value) {
    setAttribute(name, String.valueOf(value));
  }
  
  /**********************************************************************
   *<p>Adds an attribute to the current node with the specified name space.
   *The current node should be an element node.</p>
   *<p>指定された名前空間で, 現在のノードに属性を追加します. 
   *現在のノードはエレメントでなければなりません.</p>
   *********************************************************************/
  public final void setAttributeNS(String ns, String name, String value) {
    if (!finalized)
      ((Element)currentNode).setAttributeNS(ns, name, value);
    else
      throw new IllegalStateException("Document is read only.");
  }
  
  /**********************************************************************
   *<p>Adds an attribute to the current node with the specified name space.
   *The current node should be an element node.</p>
   *<p>指定された名前空間で, 現在のノードに属性を追加します. 
   *現在のノードはエレメントでなければなりません.</p>
   *********************************************************************/
  public final void setAttributeNS(String ns, String name, int value) {
    setAttributeNS(ns, name, String.valueOf(value));
  }
  
  /**********************************************************************
   *<p>Adds an attribute to the current node with the specified name space.
   *The current node should be an element node.</p>
   *<p>指定された名前空間で, 現在のノードに属性を追加します. 
   *現在のノードはエレメントでなければなりません.</p>
   *********************************************************************/
  public final void setAttributeNS(String ns, String name, double value) {
    setAttributeNS(ns, name, String.valueOf(value));
  }

  /**********************************************************************
   *<p>Adds a sibling node of the current node.</p>
   *現在のノードの弟ノードを追加します.
   *********************************************************************/
  public final void addSibling(String tagname) {
    addSibling2(doc.createElement(tagname));
  }

  /**********************************************************************
   *<p>Adds a sibling node of the current node.</p>
   *現在のノードの弟ノードを追加します.
   *********************************************************************/
  public final void addSibling(Node sibling) {
    addSibling2(doc.importNode(sibling, true));
  }

  private void addSibling2(Node sibling) {
    if (!finalized) {
      parentNode.appendChild(sibling);
      currentNode = sibling;
    } else {
      throw new IllegalStateException("Document is read only.");
    }
  }

  /**********************************************************************
   *<p>Sets a new current node to the parent node of the current node.</p>
   *<p>現在のノードの親ノードを新たな現在のノードに設定します.</p>
   *********************************************************************/
  public final void returnToParent() {
    currentNode = parentNode;
    parentNode = currentNode.getParentNode();
  }

//  final void checkReadOnly() {
//    if (readonly)
//      throw new IllegalStateException("Read only.");
//  }

    public final void changeCurrentNode(String xpath) {
	prevCurrentNode = currentNode;
	prevParentNode = parentNode;
	currentNode = selectSingleNode(xpath);
	parentNode = null;
    }

    public final void returnToPrevCurrentNode() {
	currentNode = prevCurrentNode;
	parentNode = prevParentNode;
	prevCurrentNode = null;
	prevParentNode = null;
    }

    public final void addChildOf(String xpath, String name) {
	changeCurrentNode(xpath);
	addChild(name);
    }

  protected final boolean isFinalized() {
    return finalized;
  }

    public void setTopTagAttribute(String name, String value) {
	if (!finalized)
	    doc.getDocumentElement().setAttribute(name, value);
	else
	    throw new IllegalStateException("read only");
    }

    public void setTopTagAttributeNS(String ns, String name, String value) {
	if (!finalized)
	    doc.getDocumentElement().setAttributeNS(ns, name, value);
	else
	    throw new IllegalStateException("read only");
    }

    public String getTopTagAttribute(String name) {
	return doc.getDocumentElement().getAttribute(name);
    }

    public String getTopTagAttributeNS(String ns, String name) {
	    return doc.getDocumentElement().getAttributeNS(ns, name);
    }

  /**********************************************************************
   *<p>Throws an exception if the document wrapped by this object is not 
   *finalized.</p>
   *<p>このオブジェクトがラップするドキュメントがfinalizedされていなかったら, 
   *例外をスローします.</p> 
   *********************************************************************/
  protected void checkFinalized() {
    if (!isFinalized())
      throw new IllegalStateException("Document has not been finalized.");
  }
  
  final void checkElementAddition(boolean flag, 
                                  String message) {
    if (!flag) throw new IllegalStateException(message);
  }

  final void checkElementAddition(boolean flag) {
    checkElementAddition(flag, "Tried to add an invalid element.");
  }
	

  /**********************************************************************
   *<p>This method defines the initial processing required for extracting 
   *information from the XML document. 
   *This is called by the runAll() method in the CMXCommand class 
   *after readfile() method before run() method.
   *The default implementation does not do anything.</p>
   *<p>このメソッドでは, XMLドキュメントから情報を取り出す上で必要な初期の処理を
   *記述します. これは, CMXCommandクラスのrunAll()メソッドから, 
   *readfile()メソッドの後, run()メソッドの前に呼び出されます. 
   *デフォルトの実装では何もしません.
   *********************************************************************/
  protected void analyze() throws IOException {
    // do nothing
  }
//  protected void analyze() throws TransformerException,IOException,
//    ParserConfigurationException,SAXException {
//    // do nothing
//  }

}