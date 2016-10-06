package net.yanrc.demo.nio;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TCPClient {

  public static void main(String args[]) throws Exception {

    String server = "localhost"; // Server name or IP address
    // Convert input String to bytes using the default charset
    byte[] argument = "hello,world!".getBytes();

    int servPort = 80;

    // Create channel and set to nonblocking
    SocketChannel clntChan = SocketChannel.open();
    clntChan.configureBlocking(false);

    // Initiate connection to server and repeatedly poll until complete
    if (!clntChan.connect(new InetSocketAddress(server, servPort))) {
      while (!clntChan.finishConnect()) {
        System.out.print(".");  // Do something else
      }
    }
    ByteBuffer writeBuf = ByteBuffer.wrap(argument);
    ByteBuffer readBuf = ByteBuffer.allocate(argument.length);
    int totalBytesRcvd = 0; // Total bytes received so far
    int bytesRcvd; // Bytes received in last read
    while (totalBytesRcvd < argument.length) {
      if (writeBuf.hasRemaining()) {
        clntChan.write(writeBuf);
        System.out.println("Send:"+new String(writeBuf.array()));
      }
      if ((bytesRcvd = clntChan.read(readBuf)) == -1) {
        throw new SocketException("Connection closed prematurely");
      }
      totalBytesRcvd += bytesRcvd;
      System.out.print(".");   // Do something else
    }

    System.out.println("Received: " +  // convert to String per default charset
         new String(readBuf.array(), 0, totalBytesRcvd));
    clntChan.close();
  }
}
