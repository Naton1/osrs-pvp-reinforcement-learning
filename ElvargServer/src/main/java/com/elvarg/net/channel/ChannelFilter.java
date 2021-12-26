package com.elvarg.net.channel;

import com.elvarg.net.ByteBufUtils;
import com.elvarg.net.NetworkConstants;
import com.elvarg.net.codec.LoginDecoder;
import com.elvarg.net.login.LoginResponses;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * The {@link ChannelInboundHandlerAdapter} implementation that will filter out unwanted connections
 * from propagating down the pipeline.
 *
 * @author Seven
 */
@Sharable
public class ChannelFilter extends ChannelInboundHandlerAdapter {

    /**
     * The {@link Multiset} of connections currently active within the server.
     */
    private final Multiset<String> connections = ConcurrentHashMultiset.create();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String host = ByteBufUtils.getHost(ctx.channel());

        // if this local then, do nothing and proceed to next handler in the pipeline.
        if (host.equalsIgnoreCase("127.0.0.1")) {
            return;
        }

        // add the host
        connections.add(host);

        // evaluate the amount of connections from this host.
        if (connections.count(host) > NetworkConstants.CONNECTION_LIMIT) {
            LoginDecoder.sendLoginResponse(ctx, LoginResponses.LOGIN_CONNECTION_LIMIT);
            return;
        }

        //CHECK BANS

        // Nothing went wrong, so register the channel and forward the event to next handler in the
        // pipeline.

        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        String host = ByteBufUtils.getHost(ctx.channel());

        // if this is local, do nothing and proceed to next handler in the pipeline.
        if (host.equalsIgnoreCase("127.0.0.1")) {
            return;
        }

        // remove the host from the connection list
        connections.remove(host);

        // the connection is unregistered so forward the event to the next handler in the pipeline.
        ctx.fireChannelUnregistered();
    }

}
