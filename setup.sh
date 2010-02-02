#!/bin/bash

echo "Thank you for downloading CrestMuseXML Toolkit."
echo "This program uses Apache Xerces, Xalan, Commons Math etc. distributed "
echo "under Apache License 2.0."
echo "For details, read separate text files."
echo ""

if [ -d "/Library/Java/Extensions" ]
then
  lib_dir="/Library/Java/Extensions"
else
  java_cmd=`which java`
  while [ -L $java_cmd ]
  do
    ls=(`ls -l $java_cmd`)
    java_cmd=${ls[${#ls[@]}-1]}
  done
  java_home=`dirname $java_cmd`/../
  if [ -e $java_home/jre/lib/ext/ ]
  then
    lib_dir=$java_home/jre/lib/ext/
  elif [ -e $java_home/lib/ext/ ]
  then
    lib_dir=$java_home/lib/ext/
  else
    echo [ERROR] No Java JRE or JDK found.
    exit 1
  fi
fi

echo "Now I'll be starting to install the libraries. "
echo "The following files are copying to ${lib_dir}."
echo ./lib/*.jar
echo "In general, it is not necessary to copy these files "
echo "if the same files have already been copied."
echo "Be carefully if you overwrite existing files."
echo "It may unable other software to run."

cp -i ./lib/*.jar $lib_dir
cp -i ./cmx.jar $lib_dir

echo "Finished."

