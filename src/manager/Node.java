package manager;

class Node<Task> {

    public Task data;
    public Node<Task> next;
    public Node<Task> previous;

    public Node(Node<Task> previous, Task data, Node<Task> next) {
        this.data = data;
        this.next = next;
        this.previous = previous;
    }
}
