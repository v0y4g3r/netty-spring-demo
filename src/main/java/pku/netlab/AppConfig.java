package pku.netlab;

import com.google.common.collect.Lists;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ComponentScan(basePackages = "pku.netlab")
@PropertySource("classpath:config.properties")
public class AppConfig {
    //EventLoopGroup for server
    @Bean(name = "serverWorkers")
    public List<EventLoopGroup> getServerEl() {return Lists.newArrayList(new EpollEventLoopGroup(1), new EpollEventLoopGroup());}

    //EvenLoopGroup for client
    @Bean(name = "clientWorker")
    public EventLoopGroup clientWorker(){return new NioEventLoopGroup();}

    //Container for all clients connected to server
    @Bean
    public ChannelGroup clients() {return new DefaultChannelGroup(new EpollEventLoopGroup().next());}

}
