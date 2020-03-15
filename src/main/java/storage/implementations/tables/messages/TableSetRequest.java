package storage.implementations.tables.messages;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import row.Key;

public class TableSetRequest extends BaseActorRequestImpl<TableSetRequest.TableSetParams, Boolean> {
    public TableSetRequest(Actor sourceActor, TableSetParams requestObject,
                           Consumer<Optional<Boolean>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }

    public static class TableSetParams {
        private final String table;
        private final Key key;
        private final Map<String, String> fields;

        public TableSetParams(String table, Key key, Map<String, String> fields) {
            this.table = table;
            this.key = key;
            this.fields = fields;
        }

        public String getTable() {
            return table;
        }

        public Key getKey() {
            return key;
        }

        public Map<String, String> getFields() {
            return fields;
        }
    }

}
