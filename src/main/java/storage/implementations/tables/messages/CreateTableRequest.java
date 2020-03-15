package storage.implementations.tables.messages;

import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import storage.implementations.tables.data.TableSchema;

public class CreateTableRequest extends BaseActorRequestImpl<CreateTableRequest.CreateParams, Boolean> {

    public CreateTableRequest(Actor sourceActor,
                              CreateParams requestObject, Consumer<Optional<Boolean>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }

    public static class CreateParams {
        private final String name;
        private final TableSchema schema;

        public CreateParams(String name, TableSchema schema) {
            this.name = name;
            this.schema = schema;
        }

        public String getName() {
            return name;
        }

        public TableSchema getSchema() {
            return schema;
        }
    }
}
