package storage.implementations.tables.messages;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import row.Key;
import storage.implementations.tables.data.ParsedValue;

public class TableLookupRequest extends BaseActorRequestImpl<TableLookupRequest.TableLookupParams, ParsedValue> {

    public TableLookupRequest(Actor sourceActor,
                              TableLookupParams requestObject, Consumer<Optional<ParsedValue>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }

    public static class TableLookupParams {
        private final String table;
        private final Key key;

        public TableLookupParams(String table, Key key) {
            this.table = table;
            this.key = key;
        }

        public String getTable() {
            return table;
        }

        public Key getKey() {
            return key;
        }
    }
}
