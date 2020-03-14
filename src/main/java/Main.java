import java.io.IOException;
import java.util.Scanner;

import actors.ActorMessageRouter;
import actors.implementations.ActorMessageRouterImpl;
import events.EventLoop;
import events.implementations.EventLoopImpl;
import row.KeyImpl;
import row.RowImpl;
import row.TimestampImpl;
import row.ValueImpl;
import storage.implementations.AsyncServerImpl;
import storage.implementations.VolatileGenerationImpl;
import storage.implementations.commands.CommandParserImpl;
import storage.implementations.disk.DiskTablesService;
import storage.implementations.disk.DiskTablesServiceImpl;
import storage.implementations.disk.messages.lookup.LookupRequest;
import storage.implementations.disk.messages.set.SetKVRequest;

public class Main {

    //static byte[] value = new byte[4096];

    public static void test(ActorMessageRouter router, int offset) {
        while (true) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 10000; i++) {
                    String key = Thread.currentThread().getId() * i + "";
                    String value = "" + (i * j + offset);

                    SetKVRequest setKVRequest = new SetKVRequest(null, new RowImpl(new KeyImpl(key),
                            new ValueImpl(value, new TimestampImpl())),
                            v -> {
                            }
                    );

                    router.sendRequest(setKVRequest);

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    LookupRequest request = new LookupRequest(null, new KeyImpl(key), rv -> {
                        assert rv.isPresent() && rv.get().toString().equals(value);
                    });

                    router.sendRequest(request);
                }
            }
        }
    }

    public static void main(String[] argv) throws IOException {
        Scanner input = new Scanner(System.in);
        ActorMessageRouter router = ActorMessageRouterImpl.INSTANCE;

        CommandParserImpl commandParser = new CommandParserImpl(1, null, "node-1", router);
        DiskTablesService diskTablesService = new DiskTablesServiceImpl(10, VolatileGenerationImpl::new, router);

        commandParser.run();
        diskTablesService.run();

        // test(router, 0);

        EventLoop eventLoop = new EventLoopImpl();
        eventLoop.start();

        /*
        ReplicationService replicationService = new ReplicationServiceImpl(
                Map.of("host1", new ReplicationServiceImpl.NodeDescription("localhost",5000, aliveReplicas,
                                replicatedElements),
                        "host2", new ReplicationServiceImpl.NodeDescription("localhost", 5001, aliveReplicas,
                                replicatedElements)
                        ), eventLoop);
*/

        AsyncServerImpl tcpServer = new AsyncServerImpl(1, 9090, eventLoop, router);

        tcpServer.run();

        System.out.println("Started");

        /*Set<Thread> threads = new HashSet<>();

        for (int i = 0; i < Runtime.getRuntime().availableProcessors() - 1; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                try {
                    test(storageService, finalI * 10);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
            thread.start();
        }

        System.out.println("Ready");

        while (input.hasNext()) {
            String string = input.nextLine();

            String kv[] = string.split("\\s+");

            if (kv[0].equals("get")) {

                System.err.println("KEY=" + kv[1] + " " + new String(new KeyImpl(kv[1].getBytes()).toBytes()));

                Value value = storageService.get(new KeyImpl(kv[1].getBytes())).orElse(null);
                String str = value == null ? "not found" : new String(value.asBytes());

                System.out.println(str);
            } else if (kv[0].equals("set")) {
                storageService.set(new KeyImpl(kv[1].getBytes()),
                        new ValueImpl(kv[2].getBytes(), new TimestampImpl()));
            }
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
