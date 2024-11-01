package history;

import tasks.Task;

class Node {
    private Task data;
    private Node next;
    private Node previous;

    public Node(Node previous, Task data, Node next) {
        this.data = data;
        this.next = next;
        this.previous = previous;
    }
    public Task getData() {
        return data;
    }

    public void setData(Task data) {
        this.data = data;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrevious() {
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }
}
