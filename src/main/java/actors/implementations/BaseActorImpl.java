package actors.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import actors.Actor;
import actors.ActorMessage;
import actors.ActorMessageRouter;
import actors.ActorRequest;
import actors.ActorResponse;

public abstract class BaseActorImpl implements Actor {
    private final int threadCount;
    private final BlockingQueue<ActorMessage> incoming = new ArrayBlockingQueue<>(1000);
    private final List<Thread> threads = new ArrayList<>();
    private final Map<Class<? extends ActorMessage>, Consumer> handlers;
    private final ActorMessageRouter router;

    protected BaseActorImpl(int threadCount, ActorMessageRouter router) {
        this.threadCount = threadCount;
        this.router = router;
        handlers = new HashMap<>();
    }

    @Override
    public void pushMessage(ActorMessage<?, ?> message) {
        incoming.add(message);
    }

    public <T extends ActorRequest<?, ?>> void registerMessageHandler(Class<T> messageClass,
                                                                      Consumer<? super T> handler) {
        router.registerRequestHandler(messageClass, this);
        handlers.put(messageClass, handler);
    }

    protected <T extends ActorMessage<?, ?>> void processMessage(T message) {
        System.err.println(this+": processing "+ message);
        try {
            if (message instanceof ActorResponse) {
                ((ActorResponse) message).getSourceRequest().getResponseHandler().accept(((ActorResponse) message).getResponseObject());
            } else {
                if (handlers.containsKey(message.getClass())) {
                    handlers.get(message.getClass()).accept(message);
                } else {
                    throw new IllegalArgumentException("Unregistered message type " + message.getClass().toString());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void mainLoop() {
        while (true) {
            try {
                ActorMessage message = incoming.take();
                processMessage(message);
            } catch (Throwable e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(this::mainLoop);
            thread.start();
            threads.add(thread);
        }
    }
}
