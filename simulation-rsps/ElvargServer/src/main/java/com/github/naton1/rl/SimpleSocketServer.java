package com.github.naton1.rl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class SimpleSocketServer {

    private final Gson gson;

    private final int port;

    @Singular("route")
    private final Map<String, Function<Context, CompletableFuture<?>>> routing;

    private Channel channel;
    private EventLoopGroup workerGroup;

    public synchronized void start() {
        if (this.channel != null) {
            throw new IllegalStateException();
        }
        this.workerGroup = new NioEventLoopGroup();
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap
                .group(workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new LineBasedFrameDecoder(8192));
                        ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new MessageHandler(workerGroup));
                    }
                })
                .childOption(ChannelOption.SO_REUSEADDR, true);
        try {
            final ChannelFuture future = bootstrap.bind(this.port).sync();
            this.channel = future.channel();
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
            throw new RuntimeException(e);
        }
    }

    public synchronized void close() {
        if (this.channel != null) {
            this.channel.close().syncUninterruptibly();
            this.workerGroup.shutdownGracefully().syncUninterruptibly();
            this.channel = null;
            this.workerGroup = null;
        }
    }

    @RequiredArgsConstructor
    private class MessageHandler extends SimpleChannelInboundHandler<String> {

        private final ExecutorService executor;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            final JsonObject request = JsonParser.parseString(msg).getAsJsonObject();
            final String action = request.get("action").getAsString();

            final Function<Context, CompletableFuture<?>> handler = SimpleSocketServer.this.routing.get(action);
            if (handler == null) {
                throw new IllegalArgumentException("Unknown action: " + action);
            }

            final Context context = new Context(request, SimpleSocketServer.this.gson, ctx);
            try {
                final CompletableFuture<?> future = handler.apply(context);
                future.whenCompleteAsync(
                        (response, err) -> {
                            try {
                                if (err != null) {
                                    context.completeExceptionally(err);
                                } else {
                                    context.completeSuccessfully(response);
                                }
                            } catch (Exception e) {
                                log.error("Error sending response", e);
                            }
                        },
                        executor);
            } catch (Exception e) {
                log.error("Exception handling request", e);
                context.completeExceptionally(e);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught in channel: {}", ctx.channel(), cause);
            ctx.close();
        }
    }

    @RequiredArgsConstructor
    public static class Context {
        private final JsonObject request;
        private final Gson gson;
        private final ChannelHandlerContext ctx;

        private volatile boolean handled;

        public <T> T bodyAsClass(Class<T> klass) {
            return gson.fromJson(request.get("body"), klass);
        }

        public String meta(String key) {
            return request.get("meta").getAsJsonObject().get(key).getAsString();
        }

        private void completeSuccessfully(Object response) {
            if (response == null) {
                sendJson(Map.of());
            } else {
                sendJson(Map.of("body", response));
            }
        }

        private void completeExceptionally(Throwable e) {
            log.error("Request completed with exception", e);
            final StringWriter exceptionStringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(exceptionStringWriter));
            sendJson(Map.of("error", true, "message", exceptionStringWriter.toString()));
        }

        private synchronized void sendJson(Object response) {
            if (this.handled) {
                throw new IllegalStateException("Already responded");
            }
            this.handled = true;
            final String responseJson = this.gson.toJson(response);
            this.ctx.writeAndFlush(responseJson + "\n");
        }
    }
}
