package actors;

public interface Actor {
    void pushMessage(ActorMessage<?, ?> message);

    void run();
}
