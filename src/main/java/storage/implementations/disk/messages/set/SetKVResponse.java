package storage.implementations.disk.messages.set;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;
import row.Row;

public class SetKVResponse extends BaseActorResponseImpl<Row, Void> {

    public SetKVResponse(ActorRequest<Row, Void> sourceRequest, Void response) {
        super(sourceRequest, response);
    }
}
