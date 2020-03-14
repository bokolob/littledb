package actors.implementations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import actors.Actor;
import actors.ActorMessageRouter;
import actors.ActorRequest;
import actors.ActorResponse;

public class ActorMessageRouterImpl implements ActorMessageRouter {
    public static final ActorMessageRouter INSTANCE = new ActorMessageRouterImpl();

    private final Map<Class<? extends ActorRequest<?, ?>>, Actor> actorMap = new ConcurrentHashMap<>();

    private ActorMessageRouterImpl() {
    }

    @Override
    public void registerRequestHandler(Class<? extends ActorRequest<?, ?>> reqType, Actor actor) {
        actorMap.put(reqType, actor);
    }

    @Override
    public void unregister(Actor actor) {
        actorMap.entrySet().stream().filter(e -> e.getValue().equals(actor)).forEach(e -> actorMap.remove(e.getKey()));
    }

    @Override
    public boolean sendRequest(ActorRequest<?, ?> request) {
        Actor actor = actorMap.get(request.getClass());

        if (actor != null) {
            actor.pushMessage(request);
            return true;
        }

        return false;
    }

    @Override
    public boolean sendResponse(ActorResponse<?, ?> response) {
        Actor actor = response.getSourceRequest().getSourceActor();
        actor.pushMessage(response);
        return true;
    }
}
