package storage.implementations.index.btree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BTree<T> {
    private BTreeNode<T> root;
    private final Comparator<T> comparator;
    private final int alpha;
    private final int beta;

    public BTree(Comparator<T> comparator, int t) {
        this.comparator = comparator;
        alpha = 2 * t;
        beta = 4 * t;
    }

    public void setRoot(BTreeNode<T> root) {
        this.root = root;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public List<Object> lookup(T key) {
        BTreeNode<T> current = findLeafNode(key);
        int pos = current.firstGreaterOrEqualKeyPosition(key);

        List<Object> result = new ArrayList<>();

        while (current != null) {

            while (pos < current.keysSize() && pos >= 0 && comparator.compare(current.getKey(pos), key) == 0) {
                result.add(current.getValue(pos));
                pos++;
            }

            current = current.getRightSibling();
            pos = 0;
        }

        return result;
    }

    public BTreeNode<T> findLeafNode(T key) {

        BTreeNode<T> current = root;

        while (current != null && !current.isLeaf()) {
            int pos = current.firstGreaterOrEqualKeyPosition(key);
            current = current.getChild(pos);
        }

        return current;
    }

    //TODO remove method
    public void insert(T key, Object value) {
        BTreeNode<T> current = findLeafNode(key);

        while (current != null) {
            current.addKeyValue(key, value);

            if (current.keysSize() <= getBeta()) {
                break;
            }

            BTreeNode<T> newNode = new BTreeNode<>(current.isLeaf(), current.getParent(), this, getBeta() + 1);

            current.copyEntiresToNode(newNode, 0, getBeta() / 2 + 1, 0);
            current.copyEntiresToNode(current, getBeta() / 2 + 1, getBeta() / 2, 0);

            newNode.setRightSibling(current);
            newNode.setLeftSibling(current.getLeftSibling());

            if (current.getLeftSibling() != null) {
                current.getLeftSibling().setRightSibling(newNode);
            }

            current.setLeftSibling(newNode);

            key = newNode.getKey(getBeta() / 2);
            value = newNode;

            if (current == root) {
                root = new BTreeNode<>(false, null, this, getBeta() + 1);
                current.setParent(root);
                newNode.setParent(root);

                root.setKey(0, newNode.getKey(getBeta() / 2));
                root.setValue(0, newNode);
                root.setValue(1, current);
                root.setKeyCount(1);

                break;
            }

            current = current.getParent();


        }
    }

    @Override
    public String toString() {
        return "BTree{" +
                "root=" + root +
                '}';
    }
}
