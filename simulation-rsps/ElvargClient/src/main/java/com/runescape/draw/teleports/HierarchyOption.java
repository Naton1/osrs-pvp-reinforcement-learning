package com.runescape.draw.teleports;

import java.awt.Dimension;

/**
 * @author relex lawl
 */
public interface HierarchyOption {

    Dimension getDimension();

    String getName();

    int getShortcutKey();

    String getDescription();

    int[] getIndex();

    HierarchyOption[] getOptions();
}
