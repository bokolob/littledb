package actors;

import java.util.Optional;

public interface ActorResponse<ReqType, RespType> extends ActorMessage<ReqType, RespType> {
    ActorRequest<ReqType, RespType> getSourceRequest();

    Optional<RespType> getResponseObject();
}
