package com.runescape.hashtable;


public final class HashTable {
    private final int size;
    private final Node[] cache;

    public HashTable() {
        int i = 1024;//was parameter
        size = i;
        cache = new Node[i];
        for (int k = 0; k < i; k++) {
            Node node = cache[k] = new Node();
            node.prev = node;
            node.next = node;
        }
    }

    public Node findNodeByID(long l) {
        Node node = cache[(int) (l & (long) (size - 1))];
        for (Node node_1 = node.prev; node_1 != node; node_1 = node_1.prev) {
            if (node_1.nodeId == l) {
                return node_1;
            }
        }

        return null;
    }

    public void removeFromCache(Node node, long l) {
        if (node.next != null)
            node.unlink();
        Node node_1 = cache[(int) (l & (long) (size - 1))];
        node.next = node_1.next;
        node.prev = node_1;
        node.next.prev = node;
        node.prev.next = node;
        node.nodeId = l;
    }
}
