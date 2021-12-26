package com.runescape.hashtable;

final class NodeQueue {
    private final NodeChild head;
    private NodeChild current;

    public NodeQueue() {
        head = new NodeChild();
        head.prevNodeChild = head;
        head.nextNodeChild = head;
    }

    public void insertHead(NodeChild nodeChild) {
        if (nodeChild.nextNodeChild != null)
            nodeChild.unlinkSub();
        nodeChild.nextNodeChild = head.nextNodeChild;
        nodeChild.prevNodeChild = head;
        nodeChild.nextNodeChild.prevNodeChild = nodeChild;
        nodeChild.prevNodeChild.nextNodeChild = nodeChild;
    }

    public NodeChild popTail() {
        NodeChild nodeChild = head.prevNodeChild;
        if (nodeChild == head) {
            return null;
        } else {
            nodeChild.unlinkSub();
            return nodeChild;
        }
    }

    public NodeChild reverseGetFirst() {
        NodeChild nodeChild = head.prevNodeChild;
        if (nodeChild == head) {
            current = null;
            return null;
        } else {
            current = nodeChild.prevNodeChild;
            return nodeChild;
        }
    }

    public NodeChild reverseGetNext() {
        NodeChild nodeChild = current;
        if (nodeChild == head) {
            current = null;
            return null;
        } else {
            current = nodeChild.prevNodeChild;
            return nodeChild;
        }
    }

    public int getNodeCount() {
        int i = 0;
        for (NodeChild nodeChild = head.prevNodeChild; nodeChild != head; nodeChild = nodeChild.prevNodeChild)
            i++;

        return i;
    }
}
