package actors;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class BaseActorRequestImpl<ReqType, RespType> implements ActorRequest<ReqType, RespType> {
    private final Actor sourceActor;
    private final Consumer<Optional<RespType>> onResponseHandler;
    private final ReqType requestObject;

    public BaseActorRequestImpl(Actor sourceActor, Consumer<Optional<RespType>> onResponseHandler,
                                ReqType requestObject) {
        this.sourceActor = sourceActor;
        this.onResponseHandler = onResponseHandler;
        this.requestObject = requestObject;
    }

    @Override
    public Actor getSourceActor() {
        return sourceActor;
    }

    @Override
    public Consumer<Optional<RespType>> getResponseHandler() {
        return onResponseHandler;
    }

    @Override
    public ReqType getRequestObject() {
        return requestObject;
    }
}
