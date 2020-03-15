package storage.implementations.disk.messages.lookup;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;
import row.Key;
import row.Value;

public class LookupResponse extends BaseActorResponseImpl<Key, Value> {

    public LookupResponse(ActorRequest<Key, Value> sourceRequest, Value response) {
        super(sourceRequest, response);
    }
}
