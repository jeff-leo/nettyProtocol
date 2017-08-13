package com.liu.netty.protocol.server;

import com.liu.netty.protocol.NettyConstant;
import com.liu.netty.protocol.coder.NettyMessageDecoder;
import com.liu.netty.protocol.coder.NettyMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * Created by Administrator on 2017/3/31.
 */
public class NettyServer {

    public void bind() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap sbs = new ServerBootstrap();
        sbs.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)//设置tcp参数，backlog指套接字排队的最大连接个数
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        //事件责任链
                        ChannelPipeline p = channel.pipeline();
                        System.out.println("childHandler");
                        p.addLast(new NettyMessageDecoder(1024*1024, 4, 4));
                        p.addLast(new NettyMessageEncoder());
                        p.addLast("readTimeOut", new ReadTimeoutHandler(50));
                        p.addLast("loginAuthHandler", new LoginAuthRespHandler());
                        p.addLast("heartBeatHandler", new HeartBeatRespHandler());
                    }
                });
        sbs.bind(NettyConstant.PORT).sync();
        System.out.println("Netty Server start ok : " + NettyConstant.REMOTEIP + " : " + NettyConstant.PORT);
    }

    public static void main(String[] args) throws Exception{
        new NettyServer().bind();
    }
}
