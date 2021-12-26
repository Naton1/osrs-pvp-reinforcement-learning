package com.runescape.draw.teleports;

import java.awt.*;

/**
 * @author relex lawl
 */
public interface HierarchyOption {

    public Dimension getDimension();

    public String getName();

    public int getShortcutKey();

    public String getDescription();

    public int[] getIndex();

    public HierarchyOption[] getOptions();
}
