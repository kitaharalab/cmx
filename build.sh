#!/bin/zsh

release=`basename $PWD`
JAR_FILE=cmx.jar

mkdir classes
javac -d classes -sourcepath src src/**/*.java
( cd classes ; jar cvf ../${JAR_FILE} * )
rm -r classes

if [ -e doc ] ; then
  rm -r doc/*
else
  mkdir doc
fi
javadoc -d doc -protected -version -author src/**/*.java

( cd ../ ; if [ -e ${release}.zip ] ; then rm ${release}.zip ; fi ; zip -r ${release}.zip $release -x ${release}/**/.svn/**/{*,.*} ${release}**/*~ )

