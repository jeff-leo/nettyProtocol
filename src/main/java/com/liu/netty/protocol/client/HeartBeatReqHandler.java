package com.liu.netty.protocol.client;

import com.liu.netty.protocol.MessageType;
import com.liu.netty.protocol.struct.Header;
import com.liu.netty.protocol.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/3/31.
 * 建立连接，客户端发送心跳
 */
public class HeartBeatReqHandler extends ChannelHandlerAdapter{

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        //如果是握手成功的消息，则启动心跳定时器，定时发送心跳包
        if(message.getHeader() != null &&
                message.getHeader().getType() == MessageType.LOGIN_RESP.getValue()){
            //5秒钟发一个心跳
            heartBeat = ctx.executor().scheduleAtFixedRate(
                    new HeartBeatTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
        }else if(message.getHeader() != null &&
                message.getHeader().getType() == MessageType.HEARTBEAT_RESP.getValue()){
            System.out.println("Client reciver heart beat message : ----> " + message);
        }else{
            //编码好的Message传递给下一个Handler
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(heartBeat != null){
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }

    private class HeartBeatTask implements Runnable{

        private ChannelHandlerContext ctx;

        public HeartBeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void run() {
            NettyMessage message = buildHeartMessage();
            System.out.println("Client send heart beat message : ----> " + message);
            ctx.writeAndFlush(message);
        }

        private NettyMessage buildHeartMessage(){
            NettyMessage message = new NettyMessage();
            Header header = new Header();
            header.setType(MessageType.HEARTBEAT_REQ.getValue());
            message.setHeader(header);
            return message;
        }
    }
}
