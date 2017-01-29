package com.transformice.network;

import com.transformice.network.codecs.MessageDecoder;
import com.transformice.network.codecs.MessageEncoder;
import com.transformice.server.Server;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Bootstrap {
    private final Server server;

    public Bootstrap(Server server) {
        this.server = server;
    }

    public ServerBootstrap boot() {
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        ChannelPipeline pipeline = bootstrap.getPipeline();
        pipeline.addLast("encoder", new MessageEncoder());
        pipeline.addLast("decoder", new MessageDecoder());
        pipeline.addLast("handler", new ClientHandler(this.server));
        pipeline.addLast("pipelineExecutor", new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(900, 1048576, 1073741824, 100, TimeUnit.MILLISECONDS, Executors.defaultThreadFactory())));
        return bootstrap;
    }
}
