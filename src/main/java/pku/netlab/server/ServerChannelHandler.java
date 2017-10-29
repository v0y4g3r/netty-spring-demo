package pku.netlab.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lei
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);
    private ChannelGroup clients;

    public ServerChannelHandler(ChannelGroup clients) {this.clients = clients;}

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
        logger.debug("CONN:{}", clients.size());//print the number of currently connected clients
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clients.remove(ctx.channel());
        logger.debug("DISCONN:{}", clients.size());//print the number of currently connected clients
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("MSG from {}: {}",ctx.channel().remoteAddress(),msg.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //exception handling code goes here
        super.exceptionCaught(ctx, cause);
    }

}
