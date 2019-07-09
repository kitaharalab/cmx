package jp.crestmuse.cmx.handlers;

import jp.crestmuse.cmx.filewrappers.MusicXMLWrapper;

/**********************************************************************
 *The <tt>NoteHandlerPartwise</tt> interface provides a general interface 
 *for processing each note in the partwise manner. 
 *These methods are called by the <tt>processNotePartwise</tt> method 
 *defined in the <tt>MusicXMLWrapper</tt> class. 
 *<br>
 *<tt>NoteHandlerPartwise</tt>$B%$%s%?!<%U%'!<%9$O(B, 
 *partwise$B$K3F%N!<%H$r=hM}$9$k$?$a$NHFMQE*$J%$%s%?!<%U%'!<%9$rDs6!$7$^$9(B. 
 *$B$3$l$i$N%a%=%C%I$O(B, <tt>MusicXMLWrapper</tt>$B%/%i%9Fb$GDj5A$5$l$F$$$k(B
 *<tt>processNotePartwise</tt>$B%a%=%C%I$+$i8F=P$5$l$^$9(B. 
 *@author Tetsuro Kitahara <kitahara@kuis.kyoto-u.ac.jp>
 *@version 0.10.000
 *********************************************************************/
public interface NoteHandlerPartwise {

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *begins to process a part node. <br>
   *$B$3$N%a%=%C%I$O(B, <tt>MusicXMLWrapper.processNotePartwise</tt>$B%a%=%C%I$,(B
   *part$B%N!<%I$N=hM}$r;O$a$k;~$K8F=P$5$l$^$9(B. 
   *********************************************************************/
  public void beginPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *ends to process a part node. <br>
   *$B$3$N%a%=%C%I$O(B, <tt>MusicXMLWrapper.processNotePartwise</tt>$B%a%=%C%I$,(B
   *part$B%N!<%I$N=hM}$r=*$($k;~$K8F=P$5$l$^$9(B. 
   *********************************************************************/
  public void endPart(MusicXMLWrapper.Part part, MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *begins to process a measure node. <br>
   *$B$3$N%a%=%C%I$O(B, <tt>MusicXMLWrapper.processNotePartwise</tt>$B%a%=%C%I$,(B
   *measure$B%N!<%I$N=hM}$r;O$a$k;~$K8F=P$5$l$^$9(B. 
   *********************************************************************/
  public void beginMeasure(MusicXMLWrapper.Measure measure, 
                           MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *ends to process a measure node. <br>
   *$B$3$N%a%=%C%I$O(B, <tt>MusicXMLWrapper.processNotePartwise</tt>$B%a%=%C%I$,(B
   *measure$B%N!<%I$N=hM}$r=*$($k;~$K8F=P$5$l$^$9(B. 
   *********************************************************************/
    public void endMeasure(MusicXMLWrapper.Measure measure, 
                           MusicXMLWrapper wrapper);

  /**********************************************************************
   *This method is called 
   *when the <tt>MusicXMLWrapper.processNotePartwise</tt> method 
   *finds a note node. 
   *Please implement the processing for each note in this method. 
   *$B$3$N%a%=%C%I$O(B, <tt>MusicXMLWrapper.processNotePartwise</tt>$B%a%=%C%I$,(B
   *note$B%N!<%I$r8+$D$1$?$H$-$K8F=P$5$l$^$9(B. 
   *$B$3$N%a%=%C%I$K3F(Bnote$B$KBP$7$F9T$&=hM}$r<BAu$7$F$/$@$5$$(B. 
   *********************************************************************/
    public void processMusicData(MusicXMLWrapper.MusicData md, 
                                 MusicXMLWrapper wrapper);

}