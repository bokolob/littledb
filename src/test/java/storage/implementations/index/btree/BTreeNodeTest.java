package storage.implementations.index.btree;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BTreeNodeTest {
    private static BTree<Integer> btree = new BTree<>(Integer::compareTo, 10);

    @Test
    public void firstGreaterOrEqualKeyPositionTest() {
        BTreeNode<Integer> testNode = new BTreeNode<>(false, null, btree, 10);
        testNode.addKeyValue(10, null);
        testNode.addKeyValue(60, null);
        testNode.addKeyValue(50, null);
        testNode.addKeyValue(30, null);
        testNode.addKeyValue(20, null);
        testNode.addKeyValue(40, null);

        int pos = testNode.firstGreaterOrEqualKeyPosition(30);
        assertThat(pos, CoreMatchers.is(2));

        pos = testNode.firstGreaterOrEqualKeyPosition(-100);
        assertThat(pos, CoreMatchers.is(0));

        pos = testNode.firstGreaterOrEqualKeyPosition(1000);
        assertThat(pos, CoreMatchers.is(6));
    }

    @Test
    public void firstGreaterOrEqualKeyPosition_emptyNodeTest() {
        BTreeNode<Integer> testNode = new BTreeNode<>(false, null, btree, 10);

        int pos = testNode.firstGreaterOrEqualKeyPosition(10);
        assertThat(pos, CoreMatchers.is(0));
    }

    @Test
    public void copyEntiresToNodeTest() {
        BTreeNode<Integer> testNode = new BTreeNode<>(false, null, btree,10);
        testNode.addKeyValue(10, null);
        testNode.addKeyValue(60, null);
        testNode.addKeyValue(50, null);
        testNode.addKeyValue(30, null);
        testNode.addKeyValue(20, null);
        testNode.addKeyValue(40, null);

        BTreeNode<Integer> emptyNode = new BTreeNode<>(false, null, btree, 10);

        testNode.copyEntiresToNode(emptyNode, 0, 3, 0);
        assertThat(emptyNode.keysSize(), CoreMatchers.is(3));
        assertThat(emptyNode.getKeys(), equalTo(List.of(10, 20, 30)));
    }

    @Test
    public void copyEntiresToSameNodeTest() {
        BTreeNode<Integer> testNode = new BTreeNode<>(false, null, btree, 10);
        testNode.addKeyValue(10, null);
        testNode.addKeyValue(60, null);
        testNode.addKeyValue(50, null);
        testNode.addKeyValue(30, null);
        testNode.addKeyValue(20, null);
        testNode.addKeyValue(40, null);

        testNode.copyEntiresToNode(testNode, 3, 3, 0);
        assertThat(testNode.keysSize(), CoreMatchers.is(3));

        assertThat(testNode.getKeys(), equalTo(List.of(40, 50, 60)));
    }


    @Test
    public void addKeyValueTest() {
        BTreeNode<Integer> testNode = new BTreeNode<>(true, null, btree, 10);
        testNode.addKeyValue(10, null);
        testNode.addKeyValue(60, null);
        testNode.addKeyValue(50, null);
        testNode.addKeyValue(30, null);
        testNode.addKeyValue(20, null);
        testNode.addKeyValue(40, null);

        testNode.addKeyValue(80, "Test");
        assertThat(testNode.keysSize(), CoreMatchers.is(7));
        assertThat(testNode.getKey(6), equalTo(80));
        assertThat(testNode.getValue(6), equalTo("Test"));
    }

    @Test
    public void addKeyValue_inTheMiddleTest() {
        BTreeNode<Integer> testNode = new BTreeNode<>(true, null, btree, 10);
        testNode.addKeyValue(10, null);
        testNode.addKeyValue(60, null);
        testNode.addKeyValue(50, null);
        testNode.addKeyValue(30, null);
        testNode.addKeyValue(20, null);
        testNode.addKeyValue(40, null);

        testNode.addKeyValue(33, "Test");
        assertThat(testNode.keysSize(), CoreMatchers.is(7));
        assertThat(testNode.getKey(3), equalTo(33));
        assertThat(testNode.getValue(3), equalTo("Test"));
        assertThat(testNode.getKeys(), equalTo(List.of(10, 20, 30, 33, 40, 50, 60)));
    }

    @Test
    public void addKeyValue_inEmptyTest() {
        BTreeNode<Integer> testNode = new BTreeNode<>(true, null, btree, 10);
        testNode.addKeyValue(33, "Test");
        assertThat(testNode.keysSize(), CoreMatchers.is(1));
        assertThat(testNode.getKey(0), equalTo(33));
        assertThat(testNode.getValue(0), equalTo("Test"));
        assertThat(testNode.getKeys(), equalTo(List.of(33)));
    }

}