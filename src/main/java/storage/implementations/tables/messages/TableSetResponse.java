package storage.implementations.tables.messages;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;

public class TableSetResponse extends BaseActorResponseImpl<TableSetRequest.TableSetParams, Boolean> {
    public TableSetResponse(ActorRequest<TableSetRequest.TableSetParams, Boolean> sourceRequest, Boolean response) {
        super(sourceRequest, response);
    }
}
