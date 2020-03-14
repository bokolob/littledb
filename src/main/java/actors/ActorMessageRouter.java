package actors;

public interface ActorMessageRouter {

    void registerRequestHandler(Class<? extends ActorRequest<?, ?>> reqType, Actor actor);

    void unregister(Actor actor);

    boolean sendRequest(ActorRequest<?, ?> request);

    boolean sendResponse(ActorResponse<?, ?> response);
}
