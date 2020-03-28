import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import actors.Actor;
import actors.ActorMessageRouter;
import actors.implementations.ActorMessageRouterImpl;
import actors.implementations.BaseActorImpl;
import events.EventLoop;
import events.implementations.EventLoopImpl;
import storage.implementations.AsyncServerImpl;
import storage.implementations.commands.CommandParserImpl;
import storage.implementations.commands.messages.ClientRequest;
import storage.implementations.disk.persistence.FileService;
import storage.implementations.tables.TablesListServiceImpl;
import storage.implementations.tables.data.ValueParser;
import storage.implementations.tables.data.ValueSerializer;

public class Main {

    //static byte[] value = new byte[4096];

    public static class QA extends BaseActorImpl {
        protected QA(int threadCount, ActorMessageRouter router) {
            super(threadCount, router);
        }
    }

    public static void test(Actor me, ActorMessageRouter router, int offset) {
        ClientRequest createTableReq = new ClientRequest(me, "create_table test id=int,value=string", v -> {
        });
        router.sendRequest(createTableReq);

        while (true) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 10000; i++) {
                    String key = Thread.currentThread().getId() * i + "";
                    String value = "" + (i * j + offset);

                    ClientRequest setReq = new ClientRequest(me,
                            "set test " + key + " id=" + key + ",value=" + value, v -> {
                    });
                    router.sendRequest(setReq);

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    ClientRequest checkReq = new ClientRequest(me, "get test " + key, v -> {
                       // System.out.println("ANSWER " + v.orElse("null"));
                        assert v.isPresent() && v.get().contains("\"" + value + "\"");
                    });

                    router.sendRequest(checkReq);
                }
            }
        }
    }

    public static void main(String[] argv) throws IOException {
        Scanner input = new Scanner(System.in);

        ActorMessageRouter router = ActorMessageRouterImpl.INSTANCE;

        TablesListServiceImpl tablesListService = new TablesListServiceImpl(
                10, router, "/Users/oxid/development/.littledb/tables/",
                new ValueSerializer(), new ValueParser());

        tablesListService.run();

        CommandParserImpl commandParser = new CommandParserImpl(5, null, "node-1", router);
        commandParser.run();

        EventLoop eventLoop = new EventLoopImpl();
        eventLoop.start();

        AsyncServerImpl tcpServer = new AsyncServerImpl(2, 9090, eventLoop, router);
        tcpServer.run();

        System.out.println("Started");

        //QA qa = new QA(1, router);
        //qa.run();


        /*
        ReplicationService replicationService = new ReplicationServiceImpl(
                Map.of("host1", new ReplicationServiceImpl.NodeDescription("localhost",5000, aliveReplicas,
                                replicatedElements),
                        "host2", new ReplicationServiceImpl.NodeDescription("localhost", 5001, aliveReplicas,
                                replicatedElements)
                        ), eventLoop);
*/

/*
        Set<Thread> threads = new HashSet<>();

        for (int i = 0; i < Runtime.getRuntime().availableProcessors() - 1; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> test(qa, router, finalI * 10));
            thread.setDaemon(true);
            threads.add(thread);
            thread.start();
        }


        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
*/

    }
}
