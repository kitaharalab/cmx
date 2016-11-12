#!/bin/zsh

##### To build the source code, Processing and Groovy are necessary.
##### The home directories for these software should be set to
##### $PROCESSING_HOME and $GROOVY_HOME, respectively.
##### In addition, weka.jar is necessary, which is typically 
##### put to /usr/share/java/ through the apt-get tool. 

setopt null_glob

release=`basename $PWD`
JAR_FILE=cmx.jar

for jarfile in lib/*.jar
do
  CLASSPATH=$CLASSPATH:$jarfile
done
for jarfile in /usr/share/java/*.jar
do
    CLASSPATH=$CLASSPATH:$jarfile
done
for jarfile in $PROCESSING_HOME/core/library/*.jar
do
  CLASSPATH=$CLASSPATH:$jarfile
done  
for jarfile in $GROOVY_HOME/core/library/*.jar
do
  CLASSPATH=$CLASSPATH:$jarfile
done  
export CLASSPATH

rm $JAR_FILE
mkdir classes
#javac -d classes -sourcepath src src/**/*.java
javac -d classes -target 1.6 -source 1.6 -encoding utf-8 -sourcepath src src/**/*.java
( cd classes ; jar cvf ../${JAR_FILE} * )
rm -r classes

if [ -e doc ] ; then
  rm -r doc/*
else
  mkdir doc
fi
#javadoc -d doc -protected -version -author -nodeprecated -encoding utf-8 src/**/*.java

( cd ../ ; if [ -e ${release}.zip ] ; then rm ${release}.zip ; fi ; zip -r ${release}.zip $release )
