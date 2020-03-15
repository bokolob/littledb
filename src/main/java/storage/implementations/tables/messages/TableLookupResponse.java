package storage.implementations.tables.messages;

import actors.ActorRequest;
import actors.BaseActorResponseImpl;
import storage.implementations.tables.data.ParsedValue;

public class TableLookupResponse extends BaseActorResponseImpl<TableLookupRequest.TableLookupParams, ParsedValue>  {
    public TableLookupResponse(ActorRequest<TableLookupRequest.TableLookupParams, ParsedValue> sourceRequest,
                               ParsedValue response) {
        super(sourceRequest, response);
    }
}
