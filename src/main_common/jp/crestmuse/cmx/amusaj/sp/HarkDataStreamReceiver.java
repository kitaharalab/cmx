package jp.crestmuse.cmx.amusaj.sp;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import jp.crestmuse.cmx.sound.TickTimer;

public class HarkDataStreamReceiver extends SPModule {

  ServerSocket server;
  Socket socket;
  InputStream input;
  TickTimer tt = null;
  
  public HarkDataStreamReceiver(int port) throws IOException {
    server = new ServerSocket(port);
    socket = server.accept();
    input = new BufferedInputStream(socket.getInputStream());
//    input = new DataInputStream(new BufferedInputStream(
//                                  socket.getInputStream()));
  }

  public void setTickTimer(TickTimer tt) {
    this.tt = tt;
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest) 
    throws InterruptedException {
    try {
      HarkObject harkobj = new HarkObject(input);
      if (tt != null) harkobj.music_position = tt.getTickPosition();
      dest[0].add(harkobj);
//      dest[0].add(new HarkObject(input));
    } catch (IOException e) {
      throw new SPException(e);
    }
  }

  public void stop() {
    try {
      System.err.println("HarkDataStreamReceiver stopped.");
      input.close();
      socket.close();
      server.close();
    } catch (IOException e) {
      throw new SPException(e);
    }
  }

  public Class[] getInputClasses() {
    return new Class[0];
  }

  public Class[] getOutputClasses() {
    return new Class[] { HarkObject.class };
  }
}