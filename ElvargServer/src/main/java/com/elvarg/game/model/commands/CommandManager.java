package com.elvarg.game.model.commands;

import java.util.HashMap;
import java.util.Map;

import com.elvarg.Server;
import com.elvarg.game.model.commands.impl.AnimationCommand;
import com.elvarg.game.model.commands.impl.AreaDebug;
import com.elvarg.game.model.commands.impl.BanPlayer;
import com.elvarg.game.model.commands.impl.Bank;
import com.elvarg.game.model.commands.impl.BarrageCommand;
import com.elvarg.game.model.commands.impl.ChangePassword;
import com.elvarg.game.model.commands.impl.ChatboxInterfaceCommand;
import com.elvarg.game.model.commands.impl.Claim;
import com.elvarg.game.model.commands.impl.ConfigCommand;
import com.elvarg.game.model.commands.impl.CopyBank;
import com.elvarg.game.model.commands.impl.CreationDate;
import com.elvarg.game.model.commands.impl.DebugCommand;
import com.elvarg.game.model.commands.impl.DialogueCommand;
import com.elvarg.game.model.commands.impl.Down;
import com.elvarg.game.model.commands.impl.Empty;
import com.elvarg.game.model.commands.impl.ExitClient;
import com.elvarg.game.model.commands.impl.FloodCommand;
import com.elvarg.game.model.commands.impl.GFXCommand;
import com.elvarg.game.model.commands.impl.InfiniteHealth;
import com.elvarg.game.model.commands.impl.InterfaceCommand;
import com.elvarg.game.model.commands.impl.IpBanPlayer;
import com.elvarg.game.model.commands.impl.IpMutePlayer;
import com.elvarg.game.model.commands.impl.ItemSpawn;
import com.elvarg.game.model.commands.impl.Kdr;
import com.elvarg.game.model.commands.impl.KickPlayer;
import com.elvarg.game.model.commands.impl.ListSizesCommand;
import com.elvarg.game.model.commands.impl.LockExperience;
import com.elvarg.game.model.commands.impl.MasterCommand;
import com.elvarg.game.model.commands.impl.MutePlayer;
import com.elvarg.game.model.commands.impl.Noclip;
import com.elvarg.game.model.commands.impl.OpenThread;
import com.elvarg.game.model.commands.impl.PNPCCommand;
import com.elvarg.game.model.commands.impl.Players;
import com.elvarg.game.model.commands.impl.PositionDebug;
import com.elvarg.game.model.commands.impl.ReloadCommands;
import com.elvarg.game.model.commands.impl.ReloadDrops;
import com.elvarg.game.model.commands.impl.ReloadItems;
import com.elvarg.game.model.commands.impl.ReloadNPCDefinitions;
import com.elvarg.game.model.commands.impl.ReloadNPCSpawns;
import com.elvarg.game.model.commands.impl.ReloadPunishments;
import com.elvarg.game.model.commands.impl.ReloadShops;
import com.elvarg.game.model.commands.impl.ResetCommand;
import com.elvarg.game.model.commands.impl.Runes;
import com.elvarg.game.model.commands.impl.Save;
import com.elvarg.game.model.commands.impl.SaveAll;
import com.elvarg.game.model.commands.impl.Skull;
import com.elvarg.game.model.commands.impl.SpawnNPCCommand;
import com.elvarg.game.model.commands.impl.SpawnObjectCommand;
import com.elvarg.game.model.commands.impl.SpecCommand;
import com.elvarg.game.model.commands.impl.Store;
import com.elvarg.game.model.commands.impl.TaskDebug;
import com.elvarg.game.model.commands.impl.TeleTo;
import com.elvarg.game.model.commands.impl.TeleToMe;
import com.elvarg.game.model.commands.impl.TeleToPlayer;
import com.elvarg.game.model.commands.impl.TimePlayed;
import com.elvarg.game.model.commands.impl.Title;
import com.elvarg.game.model.commands.impl.UnBanPlayer;
import com.elvarg.game.model.commands.impl.UnIpMutePlayer;
import com.elvarg.game.model.commands.impl.UnMutePlayer;
import com.elvarg.game.model.commands.impl.UnlockPrayers;
import com.elvarg.game.model.commands.impl.Up;
import com.elvarg.game.model.commands.impl.UpdateServer;
import com.elvarg.game.model.commands.impl.Yell;

public class CommandManager {

    public static final Map<String, Command> commands = new HashMap<String, Command>();
    
    private static void put(Command command, String... keys) {
        for (String key : keys) {
            commands.put(key, command);
        }
    }
    
    public static void loadCommands() {
        commands.clear();
        
        /**
         * Players Command
         */
        put(new ChangePassword(), "changepassword");
        put(new LockExperience(), "lockxp");
        put(new Claim(), "claim");
        put(new CreationDate(), "creationdate");
        put(new Kdr(), "kdr");
        put(new Players(), "players");
        put(new OpenThread(), "thread");
        put(new TimePlayed(), "timeplayed");
        put(new Store(), "store", "donate");

        /**
         * Donators Command
         */
        put(new Yell(), "yell");
        put(new Skull(), "skull", "redskull");

        /**
         * Moderators Commands
         */
        put(new MutePlayer(), "mute");
        put(new UnMutePlayer(), "unmute");
        put(new IpMutePlayer(), "ipmute");
        put(new BanPlayer(), "ban");
        put(new IpBanPlayer(), "ipban");
        put(new UnBanPlayer(), "unban");
        put(new UnIpMutePlayer(), "unipmute");
        put(new TeleToPlayer(), "teleto");
        put(new ExitClient(), "exit");
        put(new KickPlayer(), "kick");

        /**
         * Administrator Commands
         */
        put(new ReloadItems(), "reloaditemdefs");
        put(new ReloadNPCDefinitions(), "reloadnpcdefs");
        put(new ReloadNPCSpawns(), "reloadnpcspawns");
        put(new ReloadDrops(), "reloaddrops");
        put(new ReloadShops(), "reloadshops");
        put(new ReloadPunishments(), "reloadpunishments");
        put(new ReloadCommands(), "reloadcommands");
        put(new TeleToMe(), "teletome");
        put(new TeleTo(), "tele");
        put(new ItemSpawn(), "item", "pickup");
        put(new Empty(), "empty");
        put(new UnlockPrayers(), "unlockprayers");
        put(new SaveAll(), "saveall");

        /**
         * Owner Commands
         */
        put(new CopyBank(), "copybank");
        put(new Bank(), "bank");
        put(new Title(), "title");
        put(new Runes(), "runes");
        put(new BarrageCommand(), "barrage");

        /**
         * Developer Commands
         */
        put(new DialogueCommand(), "dialogue");
        put(new FloodCommand(), "flood");
        put(new MasterCommand(), "master");
        put(new ResetCommand(), "reset");
        put(new PNPCCommand(), "pnpc");
        put(new SpawnNPCCommand(), "npc");
        put(new SpawnObjectCommand(), "object");
        put(new PositionDebug(), "mypos");
        put(new ConfigCommand(), "config");
        put(new SpecCommand(), "spec");
        put(new GFXCommand(), "gfx");
        put(new AnimationCommand(), "anim");
        put(new InterfaceCommand(), "interface");
        put(new ChatboxInterfaceCommand(), "chatboxinterface");
        put(new UpdateServer(), "update");
        put(new AreaDebug(), "area");
        put(new InfiniteHealth(), "infhp");
        put(new TaskDebug(), "taskdebug");
        put(new Noclip(), "noclip");
        put(new Up(), "up");
        put(new Down(), "down");
        put(new Save(), "save");
        put(new ListSizesCommand(), "listsizes");
        
        if (!Server.PRODUCTION) {
            put(new DebugCommand(), "t");
        }
    }

    static {
        loadCommands();
    }
}
