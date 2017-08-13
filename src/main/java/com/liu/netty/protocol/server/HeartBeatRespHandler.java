package com.liu.netty.protocol.server;

import com.liu.netty.protocol.MessageType;
import com.liu.netty.protocol.struct.Header;
import com.liu.netty.protocol.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import sun.nio.ch.Net;

/**
 * 接受心跳包应答
 */
public class HeartBeatRespHandler extends ChannelHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        if(message.getHeader() != null &&
                message.getHeader().getType() == MessageType.HEARTBEAT_REQ.getValue()){
            System.out.println("Server reciver heart beat message : ----> " + message);
            NettyMessage heart = buildHeartMessage();
            System.out.println("Server send heart beat message : ----> " + heart);
            ctx.writeAndFlush(heart);
        }else{
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildHeartMessage(){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.HEARTBEAT_RESP.getValue());
        message.setHeader(header);
        return message;
    }
}
