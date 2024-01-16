package com.elvarg.net.channel;

import java.io.IOException;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.NetworkConstants;
import com.elvarg.net.PlayerSession;
import com.elvarg.net.login.LoginDetailsMessage;
import com.elvarg.net.packet.Packet;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * An implementation of netty's {@link SimpleChannelInboundHandler} to handle
 * all of netty's incoming events..
 *
 * @author Professor Oak
 */
@Sharable
public final class ChannelEventHandler extends SimpleChannelInboundHandler<Object> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {

			PlayerSession session = ctx.channel().attr(NetworkConstants.SESSION_KEY).get();

			if (session == null) {
				return;
			}

			if (msg instanceof LoginDetailsMessage) {
				session.finalizeLogin((LoginDetailsMessage) msg);
			} else if (msg instanceof Packet) {
				session.queuePacket((Packet) msg);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		PlayerSession session = ctx.channel().attr(NetworkConstants.SESSION_KEY).get();

		if (session == null || session.getPlayer() == null) {
			return;
		}

		Player player = session.getPlayer();

		if (player.isRegistered()) {
			if (!World.getRemovePlayerQueue().contains(player)) {

				// Close all open interfaces..
				if (player.busy()) {
					player.getPacketSender().sendInterfaceRemoval();
				}

				// After 60 seconds, force a logout.
				player.getForcedLogoutTimer().start(60);

				// Add player to logout queue.
				World.getRemovePlayerQueue().add(player);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		if (!(t instanceof IOException)) {
			t.printStackTrace();
		}

		try {
			ctx.channel().close();
		} catch (Exception e) {
		}
	}
}
