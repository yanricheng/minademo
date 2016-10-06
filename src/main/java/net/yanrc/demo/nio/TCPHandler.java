package net.yanrc.demo.nio;

import java.nio.channels.SelectionKey;
import java.io.IOException;

public interface TCPHandler {
  void handleAccept(SelectionKey key) throws IOException;
  void handleRead(SelectionKey key) throws IOException;
  void handleWrite(SelectionKey key) throws IOException;
}
