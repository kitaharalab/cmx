#!/bin/zsh

setopt null_glob

release=`basename $PWD`
JAR_FILE=cmx.jar

for jarfile in lib/*.jar
do
  CLASSPATH=$jarfile:$CLASSPATH
done
for jarfile in ../lib/*.jar
do
  CLASSPATH=$jarfile:$CLASSPATH
done
export CLASSPATH

rm $JAR_FILE
mkdir classes
javac -d classes -target 1.5 -source 1.5 -sourcepath src src/**/*.java
( cd classes ; jar cvf ../${JAR_FILE} * )
rm -r classes

if [ -e doc ] ; then
  rm -r doc/*
else
  mkdir doc
fi
javadoc -d doc -protected -version -author -nodeprecated src/**/*.java

( cd ../ ; if [ -e ${release}.zip ] ; then rm ${release}.zip ; fi ; zip -r ${release}.zip $release -x ${release}/**/.svn/**/{*,.*} ${release}**/*~ )

