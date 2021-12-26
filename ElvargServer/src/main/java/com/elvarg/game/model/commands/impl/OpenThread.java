package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenThread implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {

        if (parts.length > 2 || parts.length < 2) {
            player.getPacketSender().sendMessage("Please enter a valid command.");
            return;
        }

        int ID = Integer.valueOf(parts[1]);

        try {

            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://www.deadlypkers.net/server_data/fetch_thread_link.php?ID=" + ID).openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String link = in.readLine();

            if (link != null)
                player.getPacketSender().sendURL(link);

            in.close();

        } catch (IOException ex) {
        }

    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
