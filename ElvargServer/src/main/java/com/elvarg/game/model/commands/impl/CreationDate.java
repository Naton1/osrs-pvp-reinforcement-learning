package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class CreationDate implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(player.getCreationDate().getTime());

        String dateSuffix;

        switch (calendar.get(Calendar.DATE) % 10) {
            case 1:
                dateSuffix = "st";
                break;
            case 2:
                dateSuffix = "nd";
                break;
            case 3:
                dateSuffix = "rd";
                break;
            default:
                dateSuffix = "th";
                break;
        }

        player.forceChat("I started playing on the " + calendar.get(Calendar.DATE) + dateSuffix + " of "
                + new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + ", "
                + calendar.get(Calendar.YEAR) + "!");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
