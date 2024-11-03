package history;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> receivedTasks = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (!(task == null)) {
            remove(task.getId());
            linkLast(task);
        }
    }

    @Override
    public void remove(int id) {
        removeNode(receivedTasks.get(id));
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task element) {
        final Node oldTail = tail;
        final Node newNode = new Node(oldTail, element, null);
        tail = newNode;
        receivedTasks.put(element.getId(), newNode);
        if (oldTail == null) {
            head = newNode;
        } else
            oldTail.setNext(newNode);
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node currentNode = head;
        while (!(currentNode == null)) {
            tasks.add(currentNode.getData());
            currentNode = currentNode.getNext();
        }
        return tasks;
    }

    private void removeNode(Node node) {
        if (!(node == null)) {
            final Node next = node.getNext();
            final Node previous = node.getPrevious();
            node.setData(null);
            if (head == node && tail == node) {
                head = null;
                tail = null;
            } else if (head == node) {
                head = next;
                head.setPrevious(null);
            } else if (tail == node) {
                tail = previous;
                tail.setNext(null);
            } else {
                previous.setNext(next);
                next.setPrevious(previous);
            }
        }
    }
}
