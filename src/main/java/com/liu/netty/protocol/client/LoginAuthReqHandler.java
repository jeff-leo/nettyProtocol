package com.liu.netty.protocol.client;

import com.liu.netty.protocol.MessageType;
import com.liu.netty.protocol.struct.Header;
import com.liu.netty.protocol.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import sun.nio.ch.Net;

/**
 * Created by Administrator on 2017/3/31.
 */
public class LoginAuthReqHandler extends ChannelHandlerAdapter{

    //三次握手成功，请求方发送请求消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        if(message.getHeader() != null &&
                message.getHeader().getType() == MessageType.LOGIN_RESP.getValue()){
            byte loginResult = (Byte) message.getBody();
            //握手失败,body为0表示成功
            if(loginResult != (byte) 0){
                ctx.close();
            }else{
                System.out.println(" login is ok : "+message);
                //channel接受到消息
                ctx.fireChannelRead(msg);
            }
        }else{
            //传给下一个Headler，之前这里没有写上，导致心跳handler收不到包
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildLoginReq(){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_REQ.getValue());
        message.setHeader(header);
        return message;
    }

    //异常捕获
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
