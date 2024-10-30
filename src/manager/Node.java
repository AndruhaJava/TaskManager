package manager;

import tasks.Task;

class Node {

    public Task data;
    public Node next;
    public Node previous;

    public Node(Node previous, Task data, Node next) {
        this.data = data;
        this.next = next;
        this.previous = previous;
    }
}
