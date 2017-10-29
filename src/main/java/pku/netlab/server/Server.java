package pku.netlab.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@PropertySource("classpath:config.properties")
public class Server {
    private @Value("${SERVER.PORT}") int port;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ServerBootstrap b;
    private ChannelGroup clients;

    public Server(@Autowired @Qualifier("serverWorkers") List<EventLoopGroup> workers) {
        boss = workers.get(0);
        worker = workers.get(1);
        b = new ServerBootstrap();
        b.group(boss, worker)
                .channel(EpollServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(new ChannelInitializer<EpollSocketChannel>() {
                    private StringDecoder stringDecoder = new StringDecoder();

                    @Override
                    protected void initChannel(EpollSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LineBasedFrameDecoder(80));   // LineBasedFrameDecoder is not sharable
                        pipeline.addLast(stringDecoder);                            // while string decoder is
                        pipeline.addLast(new ServerChannelHandler(clients));
                    }
                });

        worker.next().scheduleAtFixedRate(() -> {
            /**
             *  it's not recommended to iterate the clients set and send heartbeat
             *  try use {@link io.netty.handler.timeout.IdleStateHandler}
             */
            for (Channel ch : clients) {
                ch.writeAndFlush(Unpooled.copiedBuffer("Server alive!".getBytes()));
            }
        }, 5, 2, TimeUnit.SECONDS);
    }

    @PostConstruct
    public void init() {}

    @PreDestroy
    public void destroy() {
        worker.shutdownGracefully();
        boss.shutdownGracefully();
    }

    /**
     * Bind to port and listen for incoming connections
     *
     * @return {@link Future} that indicates the result of bind
     */
    public Future run() {
        try {
            return b.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return worker.next().newFailedFuture(e);
        }
    }

    @Autowired
    public void setClients(ChannelGroup clients) {this.clients = clients;}
}
