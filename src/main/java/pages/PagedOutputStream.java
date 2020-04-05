package pages;

import java.io.IOException;

import row.Value;

public class PagedOutputStream {
    private final PageManager pageManager;
    private long position = 0;

    public PagedOutputStream(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    public long writeRecord(Value value) throws IOException {
        pageManager.write(value.asBytes(), position, value.size());
        position += value.size();
        return position - value.size();
    }

}
