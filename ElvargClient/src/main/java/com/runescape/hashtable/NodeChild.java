package com.runescape.hashtable;

public class NodeChild extends Node {


    public NodeChild prevNodeChild;
    NodeChild nextNodeChild;

    public NodeChild() {
    }

    public final void unlinkSub() {
        if (nextNodeChild == null) {
        } else {
            nextNodeChild.prevNodeChild = prevNodeChild;
            prevNodeChild.nextNodeChild = nextNodeChild;
            prevNodeChild = null;
            nextNodeChild = null;
        }
    }
}
