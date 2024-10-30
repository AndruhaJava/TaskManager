package manager;

class Node <Task> {

    private Task data;
    public Node<Task> next;
    public Node<Task> previous;

    public Node(Node<Task> previous, Task data, Node<Task> next) {
        this.setData(data);
        this.next = next;
        this.previous = previous;
    }

    public Task getData() {
        return data;
    }

    public void setData(Task data) {
        this.data = data;
    }
}
