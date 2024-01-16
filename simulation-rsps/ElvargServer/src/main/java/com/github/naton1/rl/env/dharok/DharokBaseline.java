package com.github.naton1.rl.env.dharok;

import static com.elvarg.util.ItemIdentifiers.ANGLERFISH;
import static com.elvarg.util.ItemIdentifiers.SARADOMIN_BREW_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_RESTORE_4_;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatAction;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.elvarg.game.model.Item;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DharokBaseline implements FighterPreset {

    // TODO implement

    private final DharokLoadout loadout;

    @Override
    public Presetable getItemPreset() {
        // Use same preset, but map brews to anglerfish since this doesn't support brews right now
        // Also, replace all but 1 super restore since there's no brews
        final AtomicInteger restoreCount = new AtomicInteger();
        return new Presetable(
                "Baseline",
                Arrays.stream(loadout.getInventory())
                        .map(i -> {
                            if (i.getId() == SARADOMIN_BREW_4_) {
                                return new Item(ANGLERFISH);
                            }
                            if (i.getId() == SUPER_RESTORE_4_ && restoreCount.incrementAndGet() > 2) {
                                return new Item(ANGLERFISH);
                            }
                            return i;
                        })
                        .toArray(Item[]::new),
                loadout.getEquipment(),
                loadout.getCombatStats().toArray(),
                loadout.getMagicSpellbook(),
                true);
    }

    @Override
    public CombatAction[] getCombatActions() {
        return new CombatAction[] {};
    }

    @Override
    public int eatAtPercent() {
        return 65;
    }
}
