#!/bin/sh

INSTALL_DIR=/usr/local
PACKAGE_NAME=cmx
RELEASE_NAME=cmx-trunk
BIN_NAME=cmx


srcdir=$(cd $(dirname $0) && pwd)

mkdir -p $INSTALL_DIR/$PACKAGE_NAME/$RELEASE_NAME
cp -r $srcdir/* $INSTALL_DIR/$PACKAGE_NAME/$RELEASE_NAME/

cat <<EOF > $INSTALL_DIR/$PACKAGE_NAME/$RELEASE_NAME/$BIN_NAME
#!/bin/sh

CMX_HOME=$INSTALL_DIR/$PACKAGE_NAME/$RELEASE_NAME

myclasspath=\$CMX_HOME/cmx.jar
for jarfile in \$CMX_HOME/lib/*.jar
do
  myclasspath=\$myclasspath:\$jarfile
done
CLASSPATH=\$CLASSPATH:\$myclasspath

if [ -z \$1 ]; then
  echo No command specified. Type \"$BIN_NAME help\" for help.
  exit 1
fi

cmd=\$1
shift 1

if [ "\$cmd" = "help" ]; then 
  cat \$CMX_HOME/help
elif [ "\$cmd" = "dev2midi" ]; then 
  java jp.crestmuse.cmx.commands.ApplyDeviationInstance \$*
elif [ "\$cmd" = "addtempo" ]; then
  java jp.crestmuse.cmx.commands.ApplyCSVTempoDeviation \$*
elif [ "\$cmd" = "mkdev" ]; then
  java jp.crestmuse.cmx.commands.DeviationInstanceExtractor \$*
elif [ "\$cmd" = "midi2scc" ]; then
  java jp.crestmuse.cmx.commands.MIDIXML2SCC \$*
elif [ "\$cmd" = "midi2smf" ]; then
  java jp.crestmuse.cmx.commands.MIDIXML2SMF \$*
elif [ "\$cmd" = "mkdeadpan" ]; then
  java jp.crestmuse.cmx.commands.MakeDeadpanSCC \$*
elif [ "\$cmd" = "scc2midi" ]; then
  java jp.crestmuse.cmx.commands.SCC2MIDIXML \$*
elif [ "\$cmd" = "smf2midi" ]; then
  java jp.crestmuse.cmx.commands.SMF2MIDIXML \$*
elif [ "\$cmd" = "smf2scc" ]; then
  java jp.crestmuse.cmx.commands.SMF2SCC \$*
elif [ "\$cmd" = "smfoverlap-check" ]; then
  java jp.crestmuse.cmx.commands.SMFOverlapChecker \$*
elif [ "\$cmd" = "smfoverlap-remove" ]; then
  java jp.crestmuse.cmx.commands.SMFOverlapRemover \$*
elif [ "\$cmd" = "smfoverlap-remove2" ]; then
  java jp.crestmuse.cmx.commands.SMFOverlapRemover2 \$*
elif [ "\$cmd" = "wav2chroma" ]; then
  java jp.crestmuse.cmx.amusaj.commands.ChromaExtractor \$*
elif [ "\$cmd" = "wav2fpd" ]; then
  java jp.crestmuse.cmx.amusaj.commands.WAV2FPD \$*
elif [ "\$cmd" = "wav2spd" ]; then
  java jp.crestmuse.cmx.amusaj.commands.WAV2SPD \$*
else
  echo Invalid command. Please type \"$BIN_NAME help\" for help.
  exit 1
fi
EOF

chmod +x $INSTALL_DIR/$PACKAGE_NAME/$RELEASE_NAME/$BIN_NAME
mkdir -p $INSTALL_DIR/bin
ln -s $INSTALL_DIR/$PACKAGE_NAME/$RELEASE_NAME/$BIN_NAME \
  $INSTALL_DIR/bin/$BIN_NAME