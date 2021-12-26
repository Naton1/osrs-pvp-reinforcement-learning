package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.content.skill.SkillManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class ResetCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        for (Skill skill : Skill.values()) {
            int level = skill == Skill.HITPOINTS ? 10 : 1;
            player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill,
                    SkillManager.getExperienceForLevel(level));
        }
        WeaponInterfaces.assign(player);
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
