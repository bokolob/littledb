package storage.implementations.index.btree;

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class BTreeTest {

    @Test
    public void insertDupsTest() {
        BTree<Integer> btree = new BTree<>(Integer::compareTo, 1);
        BTreeNode<Integer> root = new BTreeNode<>(true, null, btree, btree.getBeta() + 1);
        btree.setRoot(root);

        btree.insert(10, "ten");
        btree.insert(20, "twenty");
        btree.insert(30, "thirty");
        btree.insert(40, "forty");
        btree.insert(50, "fifty");

        System.out.println(btree);


        btree.insert(30, "thirty");
        btree.insert(30, "thirty2");
        btree.insert(30, "thirty3");
        btree.insert(30, "thirty4");
        btree.insert(30, "thirty5");
        btree.insert(30, "thirty6");

        System.out.println(btree);

        List<Object> thirties = btree.lookup(30);
        assertThat(thirties).containsExactlyInAnyOrder("thirty", "thirty", "thirty2", "thirty3","thirty4","thirty5","thirty6");

        assertThat(btree.lookup(10).get(0)).isEqualTo("ten");
        assertThat(btree.lookup(20).get(0)).isEqualTo("twenty");
        assertThat(btree.lookup(40).get(0)).isEqualTo("forty");
        assertThat(btree.lookup(50).get(0)).isEqualTo("fifty");

    }

    @Test
    public void insertTest() {
        BTree<Integer> btree = new BTree<>(Integer::compareTo, 1);
        BTreeNode<Integer> root = new BTreeNode<>(true, null, btree, btree.getBeta() + 1);
        btree.setRoot(root);

        for (int i = 0; i < 50; i++) {
            btree.insert(i, i + "");
        }

        System.out.println(btree);

        for (int i = 0; i < 50; i++) {
            List<Object> vals = btree.lookup(i);
            assertThat(vals).isNotEmpty();
            assertThat(vals.get(0)).isEqualTo(""+i);
        }

    }

}