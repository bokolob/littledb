import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import row.KeyImpl;
import row.TimestampImpl;
import row.Value;
import row.ValueImpl;
import storage.CommandParser;
import storage.DiskTablesService;
import storage.StorageService;
import storage.implementations.AsyncServerImpl;
import storage.implementations.CommandParserImpl;
import storage.implementations.DiskTablesServiceImpl;
import storage.implementations.StorageServiceImpl;
import storage.implementations.VolatileGenerationImpl;

public class Main {

    //static byte[] value = new byte[4096];

    public static void test(StorageService storageService, int offset)
            throws IOException, ExecutionException, InterruptedException
    {
        while (true) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 10000; i++) {
                    String key = Thread.currentThread().getId() * i + "";
                    String value = ""+ (i * j + offset);

                    storageService.set(new KeyImpl(key), new ValueImpl(value, new TimestampImpl()));

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Optional<Value> rv = storageService.get(new KeyImpl(key)).get();

                    assert rv.isPresent() && rv.get().toString().equals(value);
                }
            }
        }
    }

    public static void main(String[] argv) throws IOException {
        Scanner input = new Scanner(System.in);
        DiskTablesService diskTablesService = new DiskTablesServiceImpl(VolatileGenerationImpl::new);
        StorageService storageService = new StorageServiceImpl(diskTablesService);

        System.out.println("Started");

        CommandParser commandParser = new CommandParserImpl(storageService);

        //TcpServerImpl tcpServer = new TcpServerImpl(commandParser);

        AsyncServerImpl tcpServer = new AsyncServerImpl(4999,commandParser);

        tcpServer.run();

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
