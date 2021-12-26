package com.runescape.cache.graphics;

public class DropdownMenu {

    private final int height;
    private final int width;
    private final String[] options;
    private final Dropdown dropdown;
    private final boolean split;
    private boolean open;
    private String optionSelected;

    public DropdownMenu(int width, boolean split, int defaultOption, String[] options, Dropdown d) {
        this.width = width;
        this.height = split ? ((14 * options.length) / 2) + 3 : (14 * options.length) + 3;
        this.options = options;
        this.optionSelected = defaultOption == -1 ? "Select an option" : options[defaultOption];
        this.open = false;
        this.dropdown = d;
        this.split = split;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public String[] getOptions() {
        return this.options;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean b) {
        this.open = b;
    }

    public String getSelected() {
        return this.optionSelected;
    }

    public void setSelected(String s) {
        this.optionSelected = s;
    }

    public Dropdown getDrop() {
        return this.dropdown;
    }

    public boolean doesSplit() {
        return this.split;
    }
}
