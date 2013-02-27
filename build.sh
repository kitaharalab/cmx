#!/bin/zsh

setopt null_glob

release=`basename $PWD`
JAR_FILE=cmx.jar

mkdir classes
javac -d classes -target 1.5 -source 1.5 -sourcepath src src/**/*.java
( cd classes ; jar cf ../${JAR_FILE} * )
rm -r classes

if [ -e doc ] ; then
  rm -r doc/*
else
  mkdir doc
fi
javadoc -d doc -protected -version -author -quiet src/**/*.java

( cd ../ ; [ -e ${release}.zip ] && rm ${release}.zip ; zip -q -r ${release}.zip $release -x ${release}/**/.svn/**/{*,.*} ${release}**/*~ )

