/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package net.yanrc.demo.tcp;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * 一个简单tcp client 程序
 */
public class TcpClient extends IoHandlerAdapter {
    /**
     * The session
     */
    private static IoSession session;
    /**
     * The connector
     */
    private IoConnector connector;

    /**
     * Timers
     **/
    private long t0;
    private long t1;

    /**
     * the counter used for the sent messages
     */
    private CountDownLatch counter;

    /**
     * 创建tcp客户端实例
     */
    public TcpClient() {

        //新建一个连接器
        connector = new NioSocketConnector();
        //设置时间监听程序
        connector.setHandler(this);
        //设置编解码器
        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.WINDOWS, LineDelimiter.WINDOWS)));
        //连接服务器
        ConnectFuture connFuture = connector.connect(new InetSocketAddress("localhost", TcpServer.PORT));
        //监听io事件
        connFuture.awaitUninterruptibly();
        //获取连接会话
        session = connFuture.getSession();
    }

    public static void main(String[] args) throws Exception {
        TcpClient client = new TcpClient();

        client.t0 = System.currentTimeMillis();

        client.counter = new CountDownLatch(TcpServer.MAX_RECEIVED);

        session.write("hello_" + client.counter.getCount());

        client.counter.await();//计数器等待

        client.connector.dispose(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String value = message.toString();

        if (counter.getCount() % 10000 == 0) {
            System.out.println("<<receive:" + value);
        }


        long received = Long.parseLong(value.split("_")[1]);

        if (received != counter.getCount()) {
            System.out.println("Error !");
            session.closeNow();
        } else {
            if (counter.getCount() == 0L) {
                t1 = System.currentTimeMillis();
                System.out.println("------------->  end " + (t1 - t0));
                session.closeNow();
            } else {
                counter.countDown();
                session.write("hello_" + counter.getCount());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        if (counter.getCount() % 10000 == 0) {
            System.out.println("Sent " + message.toString() + " messages");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
    }
}
