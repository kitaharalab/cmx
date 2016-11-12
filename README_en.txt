CrestMuse Toolkit

-------------
 WHAT'S THIS
-------------
This is an open-source programming library for developing music processing 
sofware.

---------
 INSTALL
---------
For UNIX-style environments (including Mac OS X, Cygwin), you can use 
install.sh.

For other environments, you copy cmx.jar and all jarfiles in lib/ to 
the following directory.
  $JAVA_HOME/jre/lib/ext．

-------
 USAGE
-------
If you install this toolkit with install.sh, two commands ("cmx" and 
"cmxscript") will be added. "cmx" is used for converting music data. 
"cmxscript" is used for executing original scripts written in CMXScript, 
a Groovy-based scripting language. 
Of course, you can use this toolkit by setting CLASSPATH yourself 
because the toolkit is a Java class library.
For details, see readme_ja.pdf.

-----------
 LIBRARIES
-----------
This toolkit uses Apache Xerces 2.9.1, Xalan 2.7.1, Commons Math 1.2 (Apache 
License 2.0), Groovy 1.8.6 (Apach License 2.0) および JLayer 1.0.1 (LGPL) 
by dynamic linking. The source code for these libraries can be available at: 
http://xerces.apache.org/
http://xml.apache.org/xalan-j/
http://commons.apache.org/math/
http://groovy.codehaus.org/
http://www.javazoom.net/javalayer/javalayer.html

---------
 LICENSE
---------
This toolkit is distributed under the modified BSD license. 
For details, see LICENSE.txt.

---------
 AUTHORS
---------
This toolkit is developed by CrestMuse Toolkit Development Team. 
The members are written in AUTHORS.txt.

