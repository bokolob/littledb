package storage.implementations.index.btree;

import java.util.Arrays;
import java.util.List;

public class BTreeNode<T> {

    private final boolean isLeaf;
    private BTreeNode parent;
    private int keysCount;
    private final BTree<T> btree;

    List<T> getKeys() {
        return List.of(Arrays.copyOfRange(keys, 0, keysCount));
    }

    List getValues() {
        return List.of(Arrays.copyOfRange(values, 0, keysCount));
    }

    private final T[] keys;
    private final Object[] values;

    //only if is leaf
    private BTreeNode<T> rightSibling;
    private BTreeNode<T> leftSibling;

    @SuppressWarnings("unchecked")
    public BTreeNode(boolean isLeaf, BTreeNode parent, BTree<T> btree, int size) {
        this.isLeaf = isLeaf;
        this.parent = parent;
        this.btree = btree;

        keys = (T[]) new Object[size];
        values = new Object[size + 1];

        keysCount = 0;
    }


    public int firstGreaterOrEqualKeyPosition(T key) {
        if (keysCount == 0) {
            return 0;
        }

        int l = 0;
        int r = keysCount;

        while (l + 1 < r) {
            int m = l + (r - l) / 2;

            if (btree.getComparator().compare(key, keys[m]) <= 0) {
                r = m;
            } else {
                l = m;
            }

        }

        return btree.getComparator().compare(key, keys[l]) <= 0 ? l : r;
    }


    public boolean isLeaf() {
        return isLeaf;
    }

    public T getKey(int pos) {
        if (pos < 0 || pos >= keysCount) {
            throw new IndexOutOfBoundsException();
        }

        return keys[pos];
    }

    public void setKey(int i, T key) {
        keys[i] = key;
    }

    public void setValue(int i, Object value) {
        values[i] = value;
    }

    public Object getValue(int pos) {
        if (!isLeaf()) {
            throw new IllegalStateException();
        }

        if (pos < 0 || pos >= keysCount) {
            throw new IndexOutOfBoundsException();
        }

        return values[pos];
    }

    public BTreeNode<T> getChild(int pos) {
        if (isLeaf()) {
            throw new IllegalStateException();
        }

        return (BTreeNode<T>) values[pos];
    }

    public int keysSize() {
        return keysCount;
    }

    public BTreeNode<T> getParent() {
        return parent;
    }

    public BTreeNode<T> getRightSibling() {
        return rightSibling;
    }

    public BTreeNode<T> setRightSibling(BTreeNode<T> rightSibling) {
        this.rightSibling = rightSibling;
        return this;
    }

    public BTreeNode<T> getLeftSibling() {
        return leftSibling;
    }

    public BTreeNode<T> setLeftSibling(BTreeNode<T> leftSibling) {
        this.leftSibling = leftSibling;
        return this;
    }

    public void setParent(BTreeNode<T> root) {
        parent = root;
    }

    public void setKeyCount(int i) {
        keysCount = i;
    }

    public void copyEntiresToNode(BTreeNode<T> dest, int srcPos, int count, int dstPos) {
        System.arraycopy(keys, srcPos, dest.keys, dstPos, count);
        System.arraycopy(values, srcPos, dest.values, dstPos, count + 1);
        dest.setKeyCount(dstPos + count);
    }

    public void addKeyValue(T key, Object value) {
        int pos = firstGreaterOrEqualKeyPosition(key);

        for (int i = keysCount - 1; i >= pos; i--) {
            keys[i + 1] = keys[i];
        }

        for (int i = keysCount; i >= pos; i--) {
            values[i + 1] = values[i];
        }

        keys[pos] = key;
        values[pos] = value;

        keysCount++;
    }

    @Override
    public String toString() {
        return "BTreeNode{" +
                "isLeaf=" + isLeaf +
                ", keysCount=" + keysCount +
                ", keys=" + getKeys() +
                ", values=" + getValues() +
                '}';
    }
}
