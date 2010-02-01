CrestMuseXML Toolkit

-------------
 WHAT'S THIS
-------------
音楽情報処理研究に便利な機能を収めたオープンソースライブラリです．

---------
 INSTALL
---------
libディレクトリに収録されているjarファイルを下記のディレクトリにコピーします．
 - Java Runtime Environment (JRE)がインストールされている場合には
   <JREのディレクトリ>/lib/ext．
 - Java Development Kit (JDK)がインストールされている場合には
   <JDKのディレクトリ>/jre/lib/ext．
UNIX系の環境（Mac OSX，Cygwinを含む）をお使いの方は，setup.sh を実行することで，
自動的に上述のファイルのコピーを行います．
（ただし、単にファイルをコピーするだけですので、同名のファイルがすでに存在する場合、
上書きにより既存のソフトウェアの動作に支障が出る可能性があります。自己責任にて
お使いください。）

-----------
 LIBRARIES
-----------
Apache Xerces 2.9.1, Xalan 2.7.1, Commons Math 1.2 (Apache License 2.0)
およびjahmm 0.6.2 (BSD License) を動的結合により利用しております．
これらのソースコードは，下記のURLより入手可能です．
http://xerces.apache.org/
http://xml.apache.org/xalan-j/
http://commons.apache.org/math/
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
