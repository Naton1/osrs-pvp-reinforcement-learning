package com.github.naton1.rl.util;

import java.util.LinkedList;

public class CircularList<T> extends LinkedList<T> {

    private final int maxSize;

    public CircularList(int size) {
        this.maxSize = size;
    }

    public boolean add(T k) {
        final boolean r = super.add(k);
        if (size() > maxSize) {
            removeRange(0, size() - maxSize);
        }
        return r;
    }
}
