package storage.implementations.disk;

import row.Key;
import row.Timestamp;

public interface PrimaryIndex extends Iterable<PrimaryIndex.IndexEntry> {
    IndexEntry search(Key key);

    long size();

    class IndexEntry {
        private final Key key;
        private final int offset;
        private final int length;
        private final Timestamp timestamp;

        public IndexEntry(Key key, int offset, int length, Timestamp timestamp) {
            this.key = key;
            this.offset = offset;
            this.length = length;
            this.timestamp = timestamp;
        }

        public Key getKey() {
            return key;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }
    }

}
