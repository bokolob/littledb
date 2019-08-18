package storage.implementations;

import java.util.Iterator;
import java.util.Map;

import row.Key;
import row.Value;

public class PersistentGenerationMergingIterator implements Iterator<Map.Entry<Key, Value>> {
    private Iterator<Map.Entry<Key, Value>> first;
    private Iterator<Map.Entry<Key, Value>> second;

    private Map.Entry<Key, Value> firstRow = null;
    private Map.Entry<Key, Value> secondRow = null;

    public PersistentGenerationMergingIterator(Iterator<Map.Entry<Key, Value>> first,
            Iterator<Map.Entry<Key, Value>> second)
    {
        this.first = first;
        this.second = second;
    }

    public Map.Entry<Key, Value> next() {
        Map.Entry<Key, Value> rc;

        if (first.hasNext() && firstRow == null) {
            firstRow = first.next();
        }

        if (second.hasNext() && secondRow == null) {
            secondRow = second.next();
        }

        if (firstRow != null && secondRow != null) {

            int ds = firstRow.getKey().compareTo(secondRow.getKey());

            if (ds > 0) { //firstRow > secondRow
                rc = secondRow;
                secondRow = null;
            } else if (ds < 0) {
                rc = firstRow;
                firstRow = null;
            } else {
                if (firstRow.getValue().getTimeStamp().compareTo(secondRow.getValue().getTimeStamp()) > 0) {
                    rc = firstRow;
                } else {
                    rc = secondRow;
                }

                firstRow = null;
                secondRow = null;
            }
        } else if (firstRow != null) {
            rc = firstRow;
            firstRow = null;
        } else if (secondRow != null) {
            rc = secondRow;
            secondRow = null;
        } else {
            throw new IllegalStateException("Something went wrong");
        }

        return rc;
    }

    @Override
    public boolean hasNext() {
        return first.hasNext() || second.hasNext() || firstRow != null || secondRow != null;
    }

}
