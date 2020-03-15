package storage.implementations.tables.messages;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import actors.Actor;
import actors.BaseActorRequestImpl;
import storage.implementations.tables.data.TableInfo;

public class TableListRequest extends BaseActorRequestImpl<Void, List<TableInfo>> {
    public TableListRequest(Actor sourceActor,
                            Void requestObject, Consumer<Optional<List<TableInfo>>> onResponseHandler) {
        super(sourceActor, onResponseHandler, requestObject);
    }
}
