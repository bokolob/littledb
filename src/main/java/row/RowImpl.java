package row;

public class RowImpl implements Row {
    private final Key key;
    private final Value value;

    public RowImpl(Key key, Value value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Value getValue() {
        return value;
    }
}
