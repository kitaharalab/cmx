package jp.crestmuse.cmx.amusaj.sp;
import java.io.*;
import java.net.*;

public class HarkDataStreamReceiver extends SPModule {

  ServerSocket server;
  Socket socket;
  InputStream input;
  
  public HarkDataStreamReceiver(int port) throws IOException {
    server = new ServerSocket(port);
    socket = server.accept();
    input = new BufferedInputStream(socket.getInputStream());
//    input = new DataInputStream(new BufferedInputStream(
//                                  socket.getInputStream()));
  }

  public void execute(Object[] src, TimeSeriesCompatible[] dest) 
    throws InterruptedException {
    try {
      dest[0].add(new HarkObject(input));
    } catch (IOException e) {
      throw new SPException(e);
    }
  }

  public void stop() {
    try {
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