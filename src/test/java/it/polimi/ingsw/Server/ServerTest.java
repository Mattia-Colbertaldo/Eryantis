package it.polimi.ingsw.Server;

import it.polimi.ingsw.Client.Client;
import it.polimi.ingsw.Client.GraphicInterface.TestingGraphicHandler;
import it.polimi.ingsw.Enum.Errors;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//real test done while testing the connection between server and client like in this run test
class ServerTest {

    static class ServerStopper extends ServerCodeSetter{
        public ServerStopper(long timeDelta, Server server) {
            super(timeDelta, 1, Errors.GAME_OVER, server);
        }
    }

    static class ServerCodeSetter implements Runnable{
        private final long timeDelta;
        private final int repetition;
        private final Errors code;
        private final Server server;

        ServerCodeSetter(long timeDelta, int repetition, Errors code, Server server) {
            this.timeDelta = timeDelta;
            this.repetition = repetition;
            this.code = code;
            this.server = server;
        }

        @Override
        public void run() {
            for (int i = 0; i < this.repetition; i++) {
                try {
                    Thread.sleep(this.timeDelta);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server.setCode(this.code);
            }
        }
    }

    @Test
    void start() throws InterruptedException {

        Server server = new Server(PortGetter.getPort());

        //create a thread for stop the server after some times
        new Thread(new ServerStopper(500, server)).start();

        //not using other thread to be sure that the server is shut down
        Thread t = new Thread(server::start);
        t.start();

        t.join();

        //stop message works
        assertTrue(true);

        Thread.sleep(1000);
        System.out.print(System.lineSeparator() + System.lineSeparator());

        int port = PortGetter.getPort();
        server = new Server(port);

        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(server::start);

        Thread.sleep(10000);

        assertFalse(ex.isTerminated());

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            TestingGraphicHandler tgh = new TestingGraphicHandler("Cli");
            tgh.startGraphic();
            Client c = new Client(tgh, "127.0.0.1", port);
            executorService.execute(c::start);
        }

        assertFalse(ex.isTerminated());
        assertFalse(executorService.isTerminated());

        Thread.sleep(1000);
        executorService.shutdownNow();

        Thread.sleep(1000);

        assertTrue(executorService.isTerminated());

        System.out.println("Client is terminated wait 2 minutes and half");

        //wait
        ex.shutdown();
        assertTrue(ex.awaitTermination(3, TimeUnit.MINUTES));
    }

    @Test
    void setCode() {
        Server server = new Server(PortGetter.getPort());

        //create a thread for set a lot of times a code
        new Thread(new ServerCodeSetter(10, 50, Errors.NOTHING_TODO, server));

        //create a thread for stop the server after some times
        new Thread(new ServerStopper(500, server)).start();

        server.start();

        assertTrue(true);
    }

    static Random rand = new Random();
    @Test
    void setGameProperties() throws InterruptedException {
        int port = PortGetter.getPort();
        Server server = new Server(port);
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(server::start);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            TestingGraphicHandler t = new TestingGraphicHandler("Cli");
            t.startGraphic();
            Client c = new Client(t, "127.0.0.1", port);
            executorService.execute(c::start);
        }


        Thread.sleep(1000);

        executorService.shutdownNow();
        server.setCode(Errors.GAME_OVER);
        ex.shutdown();
        Thread.sleep(1000);
        assertTrue(ex.awaitTermination(2, TimeUnit.MINUTES));
    }

    private void mainServer (){
        Server.main(null);
    }
    @Test
    void main() throws InterruptedException {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(this::mainServer);
        Thread.sleep(20);
        ex.shutdownNow();
        Thread.sleep(20);
        assertTrue(ex.awaitTermination(2, TimeUnit.MINUTES));
    }
}