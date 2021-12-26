package com.runescape.hashtable;

public final class NodeDeque {
    private final Node head;
    private Node current;

    public NodeDeque() {
        head = new Node();
        head.prev = head;
        head.next = head;
    }

    public void insertHead(Node node) {
        if (node.next != null)
            node.unlink();
        node.next = head.next;
        node.prev = head;
        node.next.prev = node;
        node.prev.next = node;
    }

    public void insertTail(Node node) {
        if (node.next != null)
            node.unlink();
        node.next = head;
        node.prev = head.prev;
        node.next.prev = node;
        node.prev.next = node;
    }

    public Node popHead() {
        Node node = head.prev;
        if (node == head) {
            return null;
        } else {
            node.unlink();
            return node;
        }
    }

    public Node reverseGetFirst() {
        Node node = head.prev;
        if (node == head) {
            current = null;
            return null;
        } else {
            current = node.prev;
            return node;
        }
    }

    public Node getFirst() {
        Node node = head.next;
        if (node == head) {
            current = null;
            return null;
        } else {
            current = node.next;
            return node;
        }
    }

    public Node reverseGetNext() {
        Node node = current;
        if (node == head) {
            current = null;
            return null;
        } else {
            current = node.prev;
            return node;
        }
    }

    public Node getNext() {
        Node node = current;
        if (node == head) {
            current = null;
            return null;
        }
        current = node.next;
        return node;
    }

    public void removeAll() {
        if (head.prev == head)
            return;
        do {
            Node node = head.prev;
            if (node == head)
                return;
            node.unlink();
        }
        while (true);
    }

    public boolean empty() {
        return head == head.prev;
    }

    public int size() {
        int size = 0;
        for (Node node = head.next; node != head; node = node.next)
            ++size;

        return size;
    }
}
