package jp.crestmuse.cmx.commands;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.*;
import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.misc.*;

/*********************************************************************
 *<p>The abstract class <tt>CMXCommand</tt> is the superclass of classes 
 *that wraps commands executed on a command line.</p>
 *
 *<p>コマンドラインから実行するクラスの抽象上位クラスです. 
 *非リアルタイム型のコマンドを作る際には, この抽象クラスを継承して作成します. 
 *このクラスにはファイルの読み書きをはじめとする, 共通に用いられる処理が
 *最初から実装されていますので, ユーザは自分がつくろうとしているコマンド独自の
 *処理だけを実装すればよいことになります.</p>
 *
 *<p>具体的なコマンド作成手順は以下の通りです.</p>
 *
 *<p>CMXCommandクラスを継承してコマンドを作成する上で最低限作る必要があるのは, 
 *runメソッドとmainメソッドです. 
 *runメソッドは, CMXCommandクラスでは抽象クラスとして定義されており, 
 *指定されたファイルを読み込んだ後の処理内容をここで記述します. 
 *本クラスで定義されているstartメソッドを実行することで, 
 *指定された各ファイルに対して, ファイルを読み込んでrunメソッドを実行して
 *結果をファイルに書き出すという処理を行います. 
 *ですので, mainメソッドではstartメソッドを呼ぶことだけすればよいことに
 *なります. 
 *mainメソッドの典型的な記述内容は以下の通りです.
 *<pre>
 *public static void main(String[] args) {
 *  MyCommand c = new MyCommand();
 *  try {
 *    c.start(args);
 *  } catch (Exception e) {
 *    c.showErrorMessage(e);
 *    System.exit(1);
 *  }
 *}
 *</pre>
 *</p>
 *
 *<p>
 *この他に, 前処理を記述するためのpreprocメソッド，後処理を記述するための
 *postprocメソッド，各コマンド独自のオプション処理を追加するための
 *setOptionsLocal, setBoolOptionsLocalメソッドがあります. 
 *</p>
 *
 *@author Tetsuro Kitahara (t.kitahara@ksc.kwansei.ac.jp)
 *@version 0.21
 ********************************************************************/

public abstract class CMXCommand<F1 extends FileWrapperCompatible,
                                 F2 extends FileWrapperCompatible> 
  implements CMXInitializer {

/******* Fields *****************************************************/

  /******************************************************************
   *The object that wraps an input file. <br>
   *入力ファイルを表すオブジェクトです. 
   ******************************************************************/
  private F1 indata;
//  private CMXFileWrapper indata;

  /******************************************************************
   *The object that wraps an output file. <br>
   *出力ファイルを表すオブジェクトです. 
   ******************************************************************/
  private F2 outdata;
//  private CMXFileWrapper outdata;

  private jp.crestmuse.cmx.misc.Queue<String> filenames;
  /** @deprecated */
  private String filename;

  private String outfilename = null;
//  private String dirSource = ".";
  private String dirDest = null;
  private String ext = null;

  //  private static StringBuffer helpMessage = new StringBuffer();
  private static StringBuilder optionHelpMsg = new StringBuilder();

  private boolean isStdOut = false;
  private boolean mkdir = false;
  private boolean gzipped = false;

  private static String configfilename = null;
  private static ConfigXMLWrapper config = null;

    /***********************************************************************
     * @deprecated
     * Set the output filename. <br>
     * 出力ファイル名を代入します.
     **********************************************************************/
    private void setOutfilename(String outfilename) {
	this.outfilename = outfilename;
    }

  /** @deprecated */
    private void setOutFileName(String outfilename) {
      this.outfilename = outfilename;
    }

    protected String getOutFileName() {
      if (outfilename == null)
        return null;
      else if (outfilename.contains("%d")) 
        return outfilename.replace("%d", getDestDir());
      else
        return outfilename;
    }

    //    static {
    //	appendDefaultHelpMessage();
    //    }
    

    
/****** Constructor *************************************************/

  public CMXCommand() {
    filenames = new jp.crestmuse.cmx.misc.Queue<String>();
    //    appendDefaultHelpMessage();
    //    resetAll();
  }

/******* Methods (accessor) *****************************************/

  /******************************************************************
   * @deprecated
   *<p>Returns the input document.</p>
   *<p>入力ドキュメントを返します. 
   *runメソッドをオーバーライドする際に, runメソッド内で読み込んだデータに
   *アクセスする際に用いることを想定しています.</p>
   ******************************************************************/
  public final F1 indata() {
    return indata;
  }
//  public final CMXFileWrapper indata() {
//    return indata;
//  }

  /******************************************************************
   * @deprecated
   *<p>Returns the output document.</p>
   *<p>出力ドキュメントを返します. 
   *runメソッドをオーバーライドする際に, ファイルに書き込むべきデータを
   *CMXFileWrapperオブジェクトに追加する際に用いることを想定しています.
   ******************************************************************/
  public final F2 outdata() {
    return outdata;
  }
//  public final CMXFileWrapper outdata() {
//    return outdata;
//  }

  /******************************************************************
   * @deprecated
   *Creates an empty document with the specified top-tag name 
   *and assigns it to the <tt>outdata</tt> instance.
   *指定された名前のトップタグを持つ空ドキュメントを生成し, 
   *<tt>outdata</tt>インスタンスに代入します. 
   ******************************************************************/
  public final void newOutputData(String toptagname) 
	throws InvalidFileTypeException, ParserConfigurationException, 
		SAXException {
    outdata = (F2)CMXFileWrapper.createDocument(toptagname);
  }

  /******************************************************************
   * @deprecated
   *Assigns the specified CMXFileWrapper object to the output object.
   *指定されたCMXFileWrapperオブジェクトを出力オブジェクトに指定します．
   ******************************************************************/
  public final void setOutputData(F2 outdata) {
    this.outdata = outdata;
  }

/******* Methods ****************************************************/

  /******************************************************************
   *Returns the program version. <br>
   *プログラムのバージョンを返します. 
   ******************************************************************/
  protected String getVersion() {
    return "0.50.000";
  }

/****** Methods (Read the command line) ******************************/

  /*******************************************************************
   *Interprets a command line. 
   *<br>
   *コマンドラインを解釈します. 
   *******************************************************************/
  private final void readCommandLine(String[] args) 
		throws InvalidOptionException, IOException  {
    boolean result;
    String strCurrentOption = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-")) {
        result = setBoolOptions(args[i]);
        if (!result) {
          strCurrentOption = args[i];
        }
      } else {
        if (strCurrentOption == null) {
          filenames.enqueue(args[i]);
        } else {
          setOptions(strCurrentOption, args[i]);
          strCurrentOption = null;
        }
      }
    }
  }

  private boolean setBoolOptions(String option) {
    if (option.equals("-stdout")) {
      isStdOut = true;
      return true;
    } else if (option.equals("-mkdir")) {
      mkdir = true;
      return true;
    } else if (option.equals("-gz")) {
      gzipped = true;
      return true;
    } else {
      return setBoolOptionsLocal(option);
    }
  }

  /*******************************************************************
   *Please override this method to support yes/no options in a 
   *subclass.
   *This method should return true when the option is processed 
   *and false when not processed. <br>
   *サブクラスで独自のYES/NO型オプションをサポートする場合には, 
   *このメソッドをオーバーライドしてください. 
   *このメソッド内でオプションが処理された場合にはtrueを, 
   *処理されなかった場合にはfalseを返さなければなりません. 
   *******************************************************************/
  protected boolean setBoolOptionsLocal(String option) {
    return false;
  }

  private void setOptions(String option, String value) 
			throws InvalidOptionException, IOException {
    if (option.equals("-d")) {
      dirDest = value;
    } else if (option.equals("-o") || option.equals("-out")) {
      outfilename = value;
    } else if (option.equals("-ext")) {
      ext = value;
    } else if (option.equals("-S")) {
      setFileList(value);
    } else if (option.equals("-conf")) {
      configfilename = value;
    } else if (option.equals("-catalog")) {
      CMXFileWrapper.catalogFileName = value;
    } else if (option.equals("-err")) {
      try {
        System.setErr(new PrintStream(new FileOutputStream(value)));
      } catch (FileNotFoundException e) {
        throw new FileNotFoundException("Can't change Standard Error.");
      }
    } else if (option.equals("-property")) {
      Properties p = new Properties(System.getProperties());
      p.load(new FileInputStream(new File(value)));
      System.setProperties(p);
    } else {
	if (!setOptionsLocal(option, value))
	    throw new InvalidOptionException("Invalid option: " + option);
    } 
  }

  /*******************************************************************
   *<p>Please override this method to support non-yes/no options in 
   *your subclass.</p>
   *
   *<p>サブクラスで独自の非YES/NO型オプションをサポートする場合には, 
   *このメソッドをオーバーライドしてください. 
   *このメソッドがオプションを受け入れたらtrue, 
   *受け入れなかったらfalseを返さなければなりません.
   *******************************************************************/
  protected boolean setOptionsLocal(String option, String value) {
    return false;
  }

  private void setFileList(String filelist) throws IOException {
    try {
      String line;
      BufferedReader r = new BufferedReader(new FileReader(filelist));
      while ((line = r.readLine()) != null)
        filenames.enqueue(line);
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException("FileList not found.");
    }
  }

   protected String getDestDir() {
     if (dirDest == null)
       return null;
     else if (dirDest.contains("%_")) 
       return dirDest.replace("%_", 
         (new File(filename)).getAbsoluteFile().getParent());
     else
       return dirDest;
   }

  protected boolean loopEnabled() {
      if (requiredFiles() > 0)
	  return true;
      else 
	  return false;
      //    return true;
  }

  protected int requiredFiles() {
    return 1;
  }


/****** Methods (read and write) *************************************/

  /******************************************************************
   *Reads the specified file and returns a CMXFileWrapper object.</p>
   *<p>指定されたファイル名を読み込んでCMXFileWrapperオブジェクトを
   *返します.
   *具体的には, CMXFileWrapper.readfile(filename)を実行しています. 
   *通常はこのメソッドは使用しませんが, 
   *コマンドの都合により, 上記のメソッドが使えない場合(スタンダードMIDIファイル
   *をMIDI XMLドキュメントとして読み込む場合)にオーバーライドしてください.
   *(通常, ユーザがこのメソッドを呼び出す必要はありません.)
   ******************************************************************/
  protected FileWrapperCompatible readInputData(String filename) 
	throws IOException, ParserConfigurationException, SAXException,
        TransformerException  {
    return CMXFileWrapper.readfile(filename, this);
  }

  /******************************************************************
   *Writes the <tt>outdata</tt> instance to a file.
   *If the <tt>-stdout</tt> option is specified, 
   *the <tt>outdata</tt> is output to the standard output 
   *instead of being written to a file. 
   *<br>
   *<tt>outdata</tt>インスタンスをファイルに書き込みます. 
   *<tt>-stdout</tt>オプションが指定されている場合には, 
   *ファイルに書き込む代わりに, 標準出力へ出力します. 
   *入力ファイルに上書きされることを防ぐため, 
   *出力先ディレクトリ (<tt>-d</tt>オプションで指定) と
   *出力ファイル名 (<tt>-o</tt>オプションで指定) のどちらも
   *していされていない場合には, 強制的にstdoutモードになります. 
   ******************************************************************/
/*
  private final void writeOutputData(String filename) 
    throws IOException, SAXException {
    if (filename != null)
      outdata.writefile(filename);
    else
      outdata.write(System.out); 
  }
*/

  /******************************************************************
   *ファイルを読み込んだ直後にすべき処理がある場合には, 
   *このメソッドをオーバーライドします. 
   ******************************************************************/
  public void init(CMXFileWrapper f) {
    // do nothing
  }

/****** Methods (main process) ***************************************/

  /******************************************************************
   *以下の処理を行います. 
   *<ol>
   *<li>コマンドラインを解釈し, ファイル名であれば内部のキューに蓄積し, 
   *オプションであればその処理を行います. 
   *<li>preprocメソッドを呼び出します. 
   *<li>指定されたファイル名の各々に対して, 
   *ファイルの読み込み, runメソッドの実行, 結果のファイルへの書き込みを行います.
   *<li>postprocメソッドを呼び出します.
   *</ol>
   *ユーザが定義したコマンドクラスのmainメソッドから呼び出されることを
   *想定しています.
   ******************************************************************/
  public final void start(String[] args) 
	throws IOException, SAXException, ParserConfigurationException, 
		TransformerException, InvalidOptionException, 
		InvalidFileTypeException {
    showHelpIfNeeded(args);
    readCommandLine(args);
    runAll();
  }

  /*******************************************************************
   *Executes the following processses: 
   *<ol>
   *<li>pre-processing defined in the <tt>preproc</tt> method, 
   *<li>for each file specified in the command line, 
   *doing everything defined in the <tt>run</tt> method 
   *(usually reading the file, analyzing it, and writing the results), 
   *<li>post-processing defined in the <tt>postproc</tt> method.
   *</ol>
   *<br>
   *次の処理を行います: 
   *<ol>
   *<li>まず<tt>preproc</tt>メソッドで定義された前処理を行います. 
   *<li>コマンドラインで指定されたファイル名のそれぞれに対して, 
   *<tt>run</tt>メソッドで定義された処理を行います. 
   *(通常は, ファイルを読み込んで, 何らかの処理をして, その結果を
   *ファイルに書き込みます.)
   *<li>最後に<tt>postproc</tt>メソッドで定義された後処理を行います.
   *</ol>
   *なお, runメソッドが呼び出される直前に indata().analyze() が実行されます.
   *runメソッド内で処理を始める前に前処理として行いたいことは, 
   *CMXFileWrapperの各サブクラスのanalyzeメソッドに記述しておくことができます.
   *******************************************************************/
  void runAll() throws IOException, ParserConfigurationException, 
    TransformerException, SAXException, InvalidFileTypeException,
    InvalidOptionException {
//    ini = new INIWrapper(Misc.getFullPath(inifilename, "ini"));
//    ini.readfile();
    int nFiles = requiredFiles();
    if (nFiles > 0 && loopEnabled() && filenames.size() % nFiles != 0)
      throw new InvalidNumberOfFilesException();
    if (nFiles > 0 && !loopEnabled() && filenames.size() != nFiles)
      throw new InvalidNumberOfFilesException();
    if (nFiles == 0 && filenames.size() > 0)
      throw new InvalidNumberOfFilesException();
    preproc();
    F1[] files = (F1[])new FileWrapperCompatible[nFiles];
    try {
      do {
        for (int i = 0; i < nFiles; i++) {
          String filename = filenames.dequeue();
          System.err.println("[" + filename + "]");
          files[i] = (F1)readInputData(filename);
        }
        if (files.length == 1)
          this.filename = files[0].getFileName();  // kari
	outdata = null;
        String destdir = getDestDir();
        if (mkdir && (destdir != null)) {
          File dirobj = new File(destdir);
          if (!dirobj.exists())
            dirobj.mkdirs();
        }
////	indata.analyze();
        outdata = run(files);
//        if (nFiles == 1) {
//          try {
//            outdata = run(files[0]);
//          } catch (NotOverridenException e) {
//            try {
//              outdata = run(files);
//            } catch (NotOverridenException e2) {
//              indata = files[0];
//              run();
//            }
//          }
//        } else {
//          outdata = run(files);
//        }
	if (outdata != null) {
          String outfilename;
          if (isStdOut) {
            outdata.write(System.out);
          } else {
            File f = null;
            if ((outfilename = getOutFileName()) != null) {
              f = new File(outfilename);
            } else if (destdir != null) {
              if (ext != null)
                f = new File(destdir, getBaseName(filename) + "." + ext);
              else
                f = new File(destdir, removeDirName(filename));
            } else {
              if (ext != null)
                f = new File(removeExt(filename) + "." + ext);
            }
            if (f == null)
              outdata.write(System.out);
            else if (gzipped)
              outdata.writeGZippedFile(f);
            else
              outdata.writefile(f);
          }
        } 
      } while (loopEnabled());
    } catch (EmptyQueueException e) {
      postproc();
    }
  }

  /*******************************************************************
   *Resets both <tt>indata</tt> and <tt>outdata</tt> instances. <br>
   *<tt>indata</tt>インスタンスと<tt>outdata</tt>インスタンスを
   *リセットします. 
   *******************************************************************/
    //  private void resetAll() {
    //    indata = null;
    //    outdata = null;
    //  }

  /*******************************************************************
   * @deprecated
   *<p>Please to override this method to define the main processing.</p>
   *<p>このメソッドをオーバーライドして, コマンドのメイン処理を記述してください.</p>
   *******************************************************************/
  protected void run() 
    throws IOException, ParserConfigurationException, SAXException, 
    TransformerException, InvalidFileTypeException {
    throw new NotOverridenException();
  }
//  protected abstract void run() 
//	throws IOException, ParserConfigurationException, SAXException, 
//		TransformerException, InvalidFileTypeException; 

  protected F2 run(F1 f) 
    throws IOException, ParserConfigurationException, SAXException, 
    TransformerException, InvalidFileTypeException {
    if (f != null) 
      indata = f;
    run();
    return outdata;
  }

  protected F2 run(F1[] f) 
    throws IOException, ParserConfigurationException, SAXException, 
    TransformerException, InvalidFileTypeException {
    if (f.length == 0) 
      return run((F1)null);
    else if (f.length == 1) 
      return run(f[0]);
    else
      throw new NotOverridenException();
  }

  /*******************************************************************
   *<p>Please override this method if your command require pre-processing. 
   *The default implementation does nothing.</p>
   *
   *<p>前処理が必要な場合は, このメソッドをオーバーライドしてください. 
   *デフォルトの実装では何もしません.</p>
   *******************************************************************/
  protected void preproc() throws IOException, InvalidOptionException, 
    ParserConfigurationException, SAXException, TransformerException   {
        // do nothing
  }

  /*******************************************************************
   *<p>Please override this method if your command require post-processing. 
   *The default implementation does nothing.</p>
   *
   *<p>後処理が必要な場合は, このメソッドをオーバーライドしてください. 
   *デフォルトの実装では何もしません.</p>
   *******************************************************************/
  protected void postproc() throws IOException, 
    ParserConfigurationException, SAXException, TransformerException {
        // do nothing
  }

  /*******************************************************************
   * @deprecated
   *<p>Returns the name of the currently processing file.</p>
   *<p>現在処理中のファイル名を返します.
   *runメソッド内で現在処理中のファイル名が必要になったとき(たとえば拡張子だけが
   *異なるファイルを追加で読み込む場合など)に用いることを想定しています. 
   *******************************************************************/
  protected final String getFileName() {
    return filename;
  }

  /** @deprecated */
  String[] getFileList() {
    return filenames.toArray();
  }

  public static ConfigXMLWrapper getConfigXMLWrapper() {
    try {
      if (config == null)
        config = (ConfigXMLWrapper)CMXFileWrapper.readfile(configfilename);
      return config;
    } catch (NullPointerException e) {
      throw new ConfigXMLException("ConfigXML file is not specfied.");
    } catch (IOException e) {
      throw new ConfigXMLException(e.toString());
//    } catch (ParserConfigurationException e) {
//      throw new ConfigXMLException(e.toString());
//    } catch (TransformerException e) {
//      throw new ConfigXMLException(e.toString());
//    } catch (SAXException e) {
//      throw new ConfigXMLException(e.toString());
    }
  }

/****** Methods (show help and error messages) ***********************/

  /*******************************************************************
   *Checks the command line and shows the help message if needed. <br>
   *コマンドラインをチェックし, 必要であればヘルプメッセージを表示します.
   *******************************************************************/
  private void showHelpIfNeeded(String[] args) {
    if (args.length < requiredFiles() || 
        (args.length >= 1 && (args[0].equals("-h") || 
        args[0].equals("-help")))) {
	//      System.err.println(helpMessage);
	System.err.println(getHelpMessage());
      System.exit(1);
    }
  }

//  /** @deprecated */
//  int getLeastNumOfArgs() {
//    return 1;
//  }

  /*******************************************************************
   *OBSOLETE!
   *<p>Shows the help message.</p>
   *<p>ヘルプメッセージを表示します.
   *独自のヘルプメッセージに変更したい場合は, ここをオーバーライドしてください.
   *(通常はユーザが呼び出す必要はありません.)
   *******************************************************************/
  protected String getHelpMessage() {
      return defaultHelpMessage().append(optionHelpMsg).toString();
  }

  private StringBuilder defaultHelpMessage() {
      StringBuilder s = new StringBuilder();
    s.append("[" + getClass().getName() 
                      + " version " + getVersion() + "]\n");
    s.append("Usage: ");
    s.append("java [<VM options>] " + getClass().getName() 
                      + " [<options>] <filename>...\n");
    s.append("Options :\n");
    s.append("-h: show this help message\n");
    s.append("-d <dirname>: specify the output directory\n");
    s.append("-o <filename>: specioutput file name\n");
    s.append("-stdout: output the generated object to the standard output\n");
    s.append("-S <filelist>: specify target files from a file listing them instead of listing them on the command line\n");
    return s;
  }

  protected static void addOptionHelpMessage(String option, String message) {
      optionHelpMsg.append(option + ": " + message + "\n");
  }


  /** @deprecated */
  protected void appendHelpMessage(String s) {
    optionHelpMsg.append(s);
    optionHelpMsg.append("\n");
  }

  /*******************************************************************
   *<p>Shows the error message.</p>
   *<p>エラーメッセージを表示します.
   *独自のエラーメッセージに変更したい場合は, ここをオーバーライドしてください.
   *(通常はユーザが呼び出す必要はありません.)
   *******************************************************************/
  protected void showErrorMessage(Exception e) {
    e.printStackTrace();
  }

  protected void exitWithMessage(String s) {
    System.err.println(s);
    System.exit(1);
  }

  /*******************************************************************
   *Returns the filename from which the extension is removed. <br>
   *指定されたファイル名から拡張子を取り除いたものを返します. 
   *******************************************************************/
  protected static String removeExt(String filename) {
    return removeExt(filename, ".");
  }


  /*******************************************************************
   *Returns the filename from which the extension is removed. <br>
   *指定されたファイル名から拡張子を取り除いたものを返します. 
   *******************************************************************/
  protected static String removeExt(String filename, String ext) {
    int idx = filename.lastIndexOf(ext);
    if (idx < 0) 
      return filename;
    else
      return filename.substring(0, idx);
  }

  /*******************************************************************
   *Returns the filename from which both the directory and extension are 
   *removed. <br>
   *指定されたファイル名からディレクトリ名と拡張子を取り除いたものを返します. 
   *******************************************************************/
  protected static String getBaseName(String filename) {
    return getBaseName(filename, ".");
  }

  /*******************************************************************
   *Returns the filename from which both the directory and extension are 
   *removed. <br>
   *指定されたファイル名からディレクトリ名と拡張子を取り除いたものを返します. 
   *******************************************************************/
  protected static String getBaseName(String filename, String ext) {
    int idx1 = filename.lastIndexOf(File.separator);
    int idx2 = filename.lastIndexOf(ext);
    if (idx2 < 0) 
      idx2 = filename.length();
    return filename.substring(idx1 + 1, idx2);
  }

  /*******************************************************************
   *Returns the filename from which the directory is 
   *removed. <br>
   *指定されたファイル名からディレクトリ名を取り除いたものを返します. 
   *******************************************************************/
  protected static String removeDirName(String filename) {
    int idx = filename.lastIndexOf(File.separator);
    return filename.substring(idx+1);
  }
}

/**********************************************************************/

  class NotOverridenException extends RuntimeException {
    NotOverridenException() {
      super();
    }
    NotOverridenException(String s) {
      super(s);
    }
  }

/**********************************************************************/
