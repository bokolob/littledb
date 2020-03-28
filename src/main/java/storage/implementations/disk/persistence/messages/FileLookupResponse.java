package storage.implementations.disk.persistence.messages;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;
import row.Key;
import row.Value;

public class FileLookupResponse extends BaseActorResponseImpl<Key, Value> {
    public FileLookupResponse(ActorRequest<Key, Value> sourceRequest, Value response) {
        super(sourceRequest, response);
    }
}
