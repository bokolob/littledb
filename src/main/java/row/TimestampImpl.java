package row;

public class TimestampImpl implements Timestamp {
    final long ts;

    public TimestampImpl() {
        this.ts = System.currentTimeMillis();
    }

    public TimestampImpl(long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "TimestampImpl{" +
                "ts=" + ts +
                '}';
    }

    @Override
    public long asLong() {
        return ts;
    }

    @Override
    public int compareTo(Timestamp o) {
        return Long.compare(ts, o.asLong());
    }
}
