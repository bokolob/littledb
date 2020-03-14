package actors;

import java.util.Optional;
import java.util.function.Consumer;

public interface ActorRequest<ReqType, RespType> extends ActorMessage<ReqType, RespType> {
    Actor getSourceActor();

    Consumer<Optional<RespType>> getResponseHandler();

    ReqType getRequestObject();
}
