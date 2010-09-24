CrestMuseXML Toolkit

-------------
 WHAT'S THIS
-------------
音楽情報処理研究に便利な機能を収めたオープンソースライブラリです．

---------
 INSTALL
---------
UNIX系の環境（Mac OS X, Cygwinを含む）をお使いの方は、install.shを実行します。

それ以外の環境の方は、libディレクトリに収録されているjarファイルを下記のディレクトリ
にコピーします．
 - Java Runtime Environment (JRE)がインストールされている場合には
   <JREのディレクトリ>/lib/ext．
 - Java Development Kit (JDK)がインストールされている場合には
   <JDKのディレクトリ>/jre/lib/ext．

-------
 USAGE
-------
install.shを使用してインストールした場合、「cmx」というコマンドが追加されます。
このコマンドの利用方法は、
$cmx help
でご確認ください。

-----------
 LIBRARIES
-----------
Apache Xerces 2.9.1, Xalan 2.7.1, Commons Math 1.2 (Apache License 2.0), 
groovy 1.7.1 (Apach License 2.0)およびjahmm 0.6.2 (BSD License)を動的結合に
より利用しております．
これらのソースコードは，下記のURLより入手可能です．
http://xerces.apache.org/
http://xml.apache.org/xalan-j/
http://commons.apache.org/math/
http://groovy.codehaus.org/
http://code.google.com/p/jahmm/

---------
 LICENSE
---------
本ライブラリは，いわゆる「修正BSDライセンス」を採用しております．
詳しくは「LICENSE」ファイルをご覧ください．

---------
 AUTHORS
---------
本ライブラリは，CrestMuseXML Development Projectによって
開発されています．本プロジェクトのメンバーは，「AUTHORS」ファイルを
ご覧ください．
