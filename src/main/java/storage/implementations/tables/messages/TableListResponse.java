package storage.implementations.tables.messages;

import java.util.List;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;
import storage.implementations.tables.data.TableInfo;

public class TableListResponse extends BaseActorResponseImpl<Void, List<TableInfo>> {

    public TableListResponse(ActorRequest<Void, List<TableInfo>> sourceRequest, List<TableInfo> response) {
        super(sourceRequest, response);
    }
}
