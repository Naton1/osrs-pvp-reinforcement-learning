package com.runescape.hashtable;

public final class NodeCache {
    private final NodeChild emptyNodeChild;
    private final int initialCount;
    private final HashTable hashTable;
    private final NodeQueue nodeQueue;
    private int spaceLeft;

    public NodeCache(int i) {
        emptyNodeChild = new NodeChild();
        nodeQueue = new NodeQueue();
        initialCount = i;
        spaceLeft = i;
        hashTable = new HashTable();
    }

    public NodeChild get(long l) {
        NodeChild nodeChild = (NodeChild) hashTable.findNodeByID(l);
        if (nodeChild != null) {
            nodeQueue.insertHead(nodeChild);
        }
        return nodeChild;
    }

    public void put(NodeChild nodeChild, long l) {
        if (spaceLeft == 0) {
            NodeChild nodeChild_1 = nodeQueue.popTail();
            nodeChild_1.unlink();
            nodeChild_1.unlinkSub();
            if (nodeChild_1 == emptyNodeChild) {
                NodeChild nodeChild_2 = nodeQueue.popTail();
                nodeChild_2.unlink();
                nodeChild_2.unlinkSub();
            }
        } else {
            spaceLeft--;
        }
        hashTable.removeFromCache(nodeChild, l);
        nodeQueue.insertHead(nodeChild);
    }

    public void clear() {
        do {
            NodeChild nodeChild = nodeQueue.popTail();
            if (nodeChild != null) {
                nodeChild.unlink();
                nodeChild.unlinkSub();
            } else {
                spaceLeft = initialCount;
                return;
            }
        }
        while (true);
    }
}
