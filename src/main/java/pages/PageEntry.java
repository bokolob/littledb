package pages;

public class PageEntry {
    private final byte[] data;

    public PageEntry(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
