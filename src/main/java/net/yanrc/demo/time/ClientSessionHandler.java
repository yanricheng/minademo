package net.yanrc.demo.time;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link IoHandler} for SumUp client.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class ClientSessionHandler extends IoHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientSessionHandler.class);
    CountDownLatch count = null;

    public ClientSessionHandler(final CountDownLatch count){
        this.count = count;
    }

    @Override
    public void sessionOpened(IoSession session) {
        session.write(count.getCount());
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        count.countDown();
        if(count.getCount()==0){
            session.write("quit");
        }else{
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            session.write(count.getCount());

        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        session.closeNow();
    }
}