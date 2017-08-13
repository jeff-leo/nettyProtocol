package com.liu.netty.protocol.client;

import com.liu.netty.protocol.NettyConstant;
import com.liu.netty.protocol.coder.NettyMessageDecoder;
import com.liu.netty.protocol.coder.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.AbstractNioByteChannel;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import jdk.internal.org.objectweb.asm.tree.InnerClassNode;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/3/31.
 */
public class NettyClient {

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    EventLoopGroup group = new NioEventLoopGroup();

    public void connect(int port, String host) throws Exception{
        try {
            Bootstrap bs = new Bootstrap();
            bs.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            System.out.println("Handler");
                            p.addLast(new NettyMessageDecoder(1024 * 1024, 4,  4));
                            p.addLast("messageEncoder", new NettyMessageEncoder());
                            //用来实现心跳超时
                            p.addLast("readTimeOutHandler", new ReadTimeoutHandler(50));
                            p.addLast("loginAuthHandler", new LoginAuthReqHandler());
                            p.addLast("HeartBeatHandler", new HeartBeatReqHandler());
                        }
                    });

            //发起异步连接操作
            ChannelFuture future = bs.connect(new InetSocketAddress("127.0.0.1", port),
                    new InetSocketAddress(NettyConstant.LOCALIP, NettyConstant.LOCAL_PORT)).sync();
            future.channel().closeFuture().sync();//这一步会阻塞住
        } finally {
            //所有资源释放完成之后，清空资源，再次发起连接
            executor.execute(new Runnable() {
                public void run() {
                    try{
                        System.out.println("Client 尝试重新连接-->>>>>>");
                        //等待InterVAl时间，重连
                        TimeUnit.SECONDS.sleep(1);
                        //发起重连
                        connect(NettyConstant.PORT, NettyConstant.REMOTEIP);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) throws Exception{
        new NettyClient().connect(NettyConstant.PORT, NettyConstant.REMOTEIP);
    }
}
