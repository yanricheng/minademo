package net.yanrc.demo.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * 简单tcp server
 */
public class TcpServer extends IoHandlerAdapter {
    /**
     * The listening port (check that it's not already in use)
     */
    public static final int PORT = 18567;

    /**
     * The number of message to receive
     */
    public static final int MAX_RECEIVED = 100000;

    /**
     * The starting point, set when we receive the first message
     */
    private static long t0;

    /**
     * A counter incremented for every recieved message
     */
    private AtomicInteger nbReceived = new AtomicInteger(0);

    /**
     * Create the TCP server
     *
     * @throws IOException If something went wrong
     */
    public TcpServer() throws IOException {
        //accept 事件监听器
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        //io时间处理器
        acceptor.setHandler(this);
        //编解码器
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.WINDOWS, LineDelimiter.WINDOWS)));

        //绑定端口，开始监听
        acceptor.bind(new InetSocketAddress(PORT));

        System.out.println("Server started...");
    }

    public static void main(String[] args) throws IOException {
        new TcpServer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        session.closeNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        int nb = nbReceived.incrementAndGet();

        if (nb == 1) {
            t0 = System.currentTimeMillis();
        }

        if (nb == MAX_RECEIVED) {
            long t1 = System.currentTimeMillis();
            System.out.println("-------------> end " + (t1 - t0));
        }

        if (nb % 10000 == 0) {
            System.out.println("Received " + nb + " messages:"+message.toString());
        }


        // 将收到的消息写回给客户端
        session.write(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("Session closed...");

        // Reinitialize the counter and expose the number of received messages
        System.out.println("Nb message received : " + nbReceived.get());
        nbReceived.set(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("Session created...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Session idle...");
    }

    /**
     * {@inheritDoc}
     *
     * @param session the current seession
     * @throws Exception If something went wrong
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("Session Opened...");
    }
}
