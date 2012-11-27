#!/bin/sh

#################################################################
# This installer copies jar files to /usr/local/ in default.    #
# Execute this as superuser (e.g., with the sudo command).      #
#################################################################

usr=/usr/local
branch=cmx-0.62

srcdir=$(cd $(dirname $0) && pwd)

mkdir -p $usr/cmx/$branch
cp -r $srcdir/* $usr/cmx/$branch/

cat <<EOF > $usr/cmx/$branch/cmxscript
#!/bin/sh

groovy $usr/cmx/$branch/scripts/cmxscript.groovy \$*
EOF

cat <<EOF > $usr/cmx/$branch/cmx
#!/bin/sh

CMX_HOME=$usr/cmx/$branch

myclasspath=\$CMX_HOME/cmx.jar
for jarfile in \$CMX_HOME/lib/*.jar
do
  myclasspath=\$myclasspath:\$jarfile
done
CLASSPATH=\$myclasspath:\$CLASSPATH
export CLASSPATH

#if [ -z \$1 ]; then
#  echo No command specified. Type \"$cmd help\" for help.
#  exit 1
#fi

options=

while [ \$# -ge 1 ]
do
  cmd=\$1
  shift
  echo \$cmd
  echo \$options
  if [ "\$cmd" = "help" -o "\$cmd" = "-help" ]; then 
    cat \$CMX_HOME/help
    break
  elif [ "\$cmd" = "dev2midi" ]; then 
    java \$options jp.crestmuse.cmx.commands.ApplyDeviationInstance \$*
    break
  elif [ "\$cmd" = "addtempo" ]; then
    java \$options jp.crestmuse.cmx.commands.ApplyCSVTempoDeviation \$*
    break
  elif [ "\$cmd" = "mkdev" ]; then
    java \$options jp.crestmuse.cmx.commands.DeviationInstanceExtractor \$*
    break
  elif [ "\$cmd" = "midi2scc" ]; then
    java \$options jp.crestmuse.cmx.commands.MIDIXML2SCC \$*
    break
  elif [ "\$cmd" = "midi2smf" ]; then
    java \$options jp.crestmuse.cmx.commands.MIDIXML2SMF \$*
    break
  elif [ "\$cmd" = "mkdeadpan" ]; then
    java \$options jp.crestmuse.cmx.commands.MakeDeadpanSCC \$*
    break
  elif [ "\$cmd" = "scc2midi" ]; then
    java \$options jp.crestmuse.cmx.commands.SCC2MIDI \$*
    break
  elif [ "\$cmd" = "smf2midi" ]; then
    java \$options jp.crestmuse.cmx.commands.SMF2MIDIXML \$*
    break
  elif [ "\$cmd" = "smf2scc" ]; then
    java \$options jp.crestmuse.cmx.commands.SMF2SCC \$*
    break
  elif [ "\$cmd" = "smfoverlap-check" ]; then
    java \$options jp.crestmuse.cmx.commands.SMFOverlapChecker \$*
    break
  elif [ "\$cmd" = "smfoverlap-remove" ]; then
    java \$options jp.crestmuse.cmx.commands.SMFOverlapRemover \$*
    break
  elif [ "\$cmd" = "smfoverlap-remove2" ]; then
    java \$options jp.crestmuse.cmx.commands.SMFOverlapRemover2 \$*
    break
  elif [ "\$cmd" = "wav2chroma" ]; then
    java \$options jp.crestmuse.cmx.amusaj.commands.ChromaExtractor \$*
    break
  elif [ "\$cmd" = "wav2fpd" ]; then
    java \$options jp.crestmuse.cmx.amusaj.commands.WAV2FPD \$*
    braek
  elif [ "\$cmd" = "wav2spd" ]; then
    java $options jp.crestmuse.cmx.amusaj.commands.WAV2SPD \$*
    break
  else
    if [ \$# -eq 1 ]; then
      echo No command or invalid command specified. 
      echo Please type \"cmx help\" for help.
      exit 1
    fi
  fi
  options="\$options \$cmd"
done
EOF

cp ~/.bashrc ~/.bashrc.backup
cat <<EOF >> ~/.bashrc

  ##### The following is automatically added by $branch
  ##### Please remove this when uninstalling $branch
  CLASSPATH=$usr/cmx/$branch/cmx.jar:\$CLASSPATH
  for jarfile in $usr/cmx/$branch/lib/*.jar
  do
    CLASSPATH=\$jarfile:\$CLASSPATH
  done
  export CLASSPATH

EOF

chmod +x $usr/cmx/$branch/cmx
chmod +x $usr/cmx/$branch/cmxscript
mkdir -p $usr/bin
ln -sf $usr/cmx/$branch/cmx $usr/bin/cmx
ln -sf $usr/cmx/$branch/cmxscript $usr/bin/cmxscript

mkdir -p ~/.groovy/lib
ln -sf $usr/cmx/$branch/cmx.jar ~/.groovy/lib/
ln -sf $usr/cmx/$branch/lib/*.jar ~/.groovy/lib/

if [ -e ~/sketchbook/ ]
then
  mkdir -p ~/sketchbook/libraries/cmx/library
  ln -sf $usr/cmx/$branch/cmx.jar ~/sketchbook/libraries/cmx/library/
  ln -sf $usr/cmx/$branch/lib/*.jar ~/sketchbook/libraries/cmx/library/
fi

if [ -e ~/Documents/Processing/ ]
then
  mkdir -p ~/Documents/Processing/libraries/cmx/library
  ln -sf $usr/cmx/$branch/cmx.jar ~/Documents/Processing/libraries/cmx/library/
  ln -sf $usr/cmx/$branch/lib/*.jar ~/Documents/Processing/libraries/cmx/library/
fi

