package actors;

import java.util.Optional;

public class BaseActorResponseImpl<ReqType, RespType> implements ActorResponse<ReqType, RespType> {
    private final ActorRequest<ReqType, RespType> sourceRequest;
    private final RespType response;

    public BaseActorResponseImpl(ActorRequest<ReqType, RespType> sourceRequest, RespType response) {
        this.sourceRequest = sourceRequest;
        this.response = response;
    }

    @Override
    public ActorRequest<ReqType, RespType> getSourceRequest() {
        return sourceRequest;
    }

    @Override
    public Optional<RespType> getResponseObject() {
        return Optional.ofNullable(response);
    }
}
