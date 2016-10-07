package net.yanrc.demo.time;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2016/10/7.
 */
public class TimeClient {
    private static final CountDownLatch count = new CountDownLatch(99);
    private static long CONNECT_TIMEOUT = 1000;
    private static String HOSTNAME = "localhost";
    private static int PORT = 9123;

    public static void main(String[] args) throws Throwable {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);

        connector.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));

//        connector.getFilterChain().addLast("codec",
//                new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.setHandler(new ClientSessionHandler(count));
        IoSession session;

        for (; ; )
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(HOSTNAME, PORT));
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeIoException e) {
                System.err.println("Failed to connect. ");
                e.printStackTrace();
                Thread.sleep(5000);
            }

        // wait until the summation is done
        session.getCloseFuture().awaitUninterruptibly();
        connector.dispose();
    }
}
