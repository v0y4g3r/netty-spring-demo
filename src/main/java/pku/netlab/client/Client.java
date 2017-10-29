package pku.netlab.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
@PropertySource("classpath:config.properties")
public class Client {
    private EventLoopGroup workers;
    public @Value("${SERVER.PORT}") int serverPort;
    public @Value("${SERVER.HOST}") String serverAddr;

    private Bootstrap b;
    private Channel channel;

    public Client(@Autowired @Qualifier("clientWorker") EventLoopGroup workers) {
        this.workers = workers;
        b = new Bootstrap();
        b.channel(NioSocketChannel.class)
                .group(workers)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new StringEncoder());
                        p.addLast(new LineEncoder());
                        p.addLast(new ClientChannelInboundHandler());
                    }
                });
    }


    public void run() {
        try {
            this.channel = b.connect(serverAddr, serverPort).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.shutdown();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            for (; ; ) {
                //read user input
                final String input = in.readLine();
                final String inputLine = input != null ? input.trim() : null;
                if (inputLine == null || "quit".equalsIgnoreCase(inputLine)) {
                    this.channel.close().sync();
                    shutdown();
                    return;
                }
                if (inputLine.isEmpty()) continue;
                //send to server
                channel.writeAndFlush(inputLine);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {this.workers.shutdownGracefully();}

}
