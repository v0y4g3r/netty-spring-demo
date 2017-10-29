package pku.netlab;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import pku.netlab.client.Client;
import pku.netlab.server.Server;

@Component
public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        ctx.getBean(Server.class).run().addListener(f -> {
            if (!f.isSuccess()) return;
            //Now we can start client
            new Thread(ctx.getBean(Client.class)::run).start();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(ctx::close));
    }
}
