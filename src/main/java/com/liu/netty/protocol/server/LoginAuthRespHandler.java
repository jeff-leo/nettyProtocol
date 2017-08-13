package com.liu.netty.protocol.server;

import com.liu.netty.protocol.MessageType;
import com.liu.netty.protocol.struct.Header;
import com.liu.netty.protocol.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/3/31.
 * 服务端的握手接入和安全认证
 */
public class LoginAuthRespHandler extends ChannelHandlerAdapter{

    //缓存列表
    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<String, Boolean>();
    //ip白名单
    private String[] whiteList = {"127.0.0.1"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        //如果是握手消息
        if(message.getHeader() != null &&
                message.getHeader().getType() == MessageType.LOGIN_REQ.getValue()){
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage response = null;
            if(nodeCheck.containsKey(nodeIndex)){
                response = buildResponse((byte)-1);
            }else{
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOK = false;
                //判断此ip是否是白名单ip
                for(String WIP : whiteList){
                    if(WIP.equals(ip)){
                        isOK = true;
                        break;
                    }
                }
                response = isOK ? buildResponse((byte) 0) : buildResponse((byte) -1);
                //放入缓存中
                if(isOK){
                    nodeCheck.put(nodeIndex, true);
                }
            }
            System.out.println("The login response is : " + response
                    + " body [" + response.getBody() + "]");

            //返回给客户端
            ctx.writeAndFlush(response);
        }else{
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildResponse(byte result){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.getValue());
        message.setHeader(header);
        message.setBody(result);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
