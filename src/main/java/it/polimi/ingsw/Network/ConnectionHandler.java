package it.polimi.ingsw.Network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import it.polimi.ingsw.Enum.Errors;
import it.polimi.ingsw.Message.Message;
import it.polimi.ingsw.Message.Ping;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A complete implementation that mask completely the network level and gives you two easy queue, one to the server and one from the server. <br>
 * It is implemented with a TCP Socket and use a peak of 6 thread to run (1 for the main thread that is always in wait, 2 for input and output from the server, and finally 3 for the ping where a thread creates 2 java.util.Timer).<br>
 * This class takes care also of the ping from and to this client, it send always a ping within the default timeout passed to the constructor, and calls the TimerTask function passed when the server doesn't send anything for the default timeout passed * 2. <br>
 */
public class ConnectionHandler implements Runnable{

    private final BlockingQueue<JsonElement> out = new LinkedBlockingQueue<>();
    private final Talker talker;
    private final BlockingQueue<JsonElement> in = new LinkedBlockingQueue<>();
    private final Listener listener;

    private final PingTimer pingTimer;
    private final Socket socket;

    private volatile boolean hasToRun;
    private final Object lock = new Object();

    /**
     * Create connection Handler
     * @param socket socket used for this connection
     * @param timeout the default timeout for the ping (exactly fro the sender, x2 for the receiver )
     * @param errorTask the task to do if the server go down and don't answer the ping message, if null it will print that the timeout is occurred in the default system out and in the graphic environment, then it will close the ConnectionHandler
     */
    public ConnectionHandler (Socket socket, Duration timeout, @Nullable Callable errorTask){
        this.socket = socket;
        //initialize input and output from server
        InputStream fromSocket;
        OutputStream toSocket;
        try {
            fromSocket = socket.getInputStream();
            toSocket = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        //create function for pint timer
        Callable sendPing = new Callable() {
            private PrintWriter printer;
            private String ping;

            public Callable init(String ping, OutputStream outputStream){
                this.printer = new PrintWriter(outputStream);
                this.ping = ping;
                return this;
            }
            @Override
            public Object call() {
                this.printer.println(ping);
                return null;
            }
        }.init(new Gson().toJson(new Ping()), toSocket);

        Callable errorCall;
        if (errorTask != null)
            errorCall = errorTask;
        else{
            errorCall = new Callable() {
                private ConnectionHandler c;
                public Callable init(ConnectionHandler c){
                    this.c = c;
                    return this;
                }
                @Override
                public Object call() {
                    System.err.println("Disconnected, run default action (stop only connection handler)");
                    c.stopConnectionHandler();
                    return null;
                }
            }.init(this);
        }

        this.pingTimer = new PingTimer(timeout, new TimerTaskCloneable(sendPing), new TimerTaskCloneable(errorCall));
        this.talker = new Talker(this.out, toSocket, pingTimer);
        this.listener = new Listener(this.in, fromSocket, pingTimer);

        System.out.println("Connected at " + socket.getInetAddress().getHostAddress() + " at port " + socket.getLocalPort());
    }

    /**
     * Create the ConnectionHandler with the creation of a client socket
     * @param serverHost the ip of the server
     * @param serverPort the port of the server
     * @param timeout the default timeout for the ping (exactly fro the sender, x2 for the receiver )
     * @param errorTask the task to do if the server go down and don't answer the ping message, if null it will print that the timeout is occurred in the default system out and in the graphic environment, then it will close the ConnectionHandler
     */
    public ConnectionHandler(String serverHost, int serverPort, Duration timeout, @Nullable Callable errorTask) throws IOException {
        this(new Socket(serverHost, serverPort), timeout, errorTask);
    }

    /**
     * Getter for the queue to server, any message put in this queue is send to the server one by one in order of arrival in this queue
     * @return the queue that is used to send the message to the server
     */
    public BlockingQueue<JsonElement> getQueueToServer() {
        return out;
    }

    /**
     * Getter for the queue from server, any message that has received from server is put in this queue and the first message returned is the older message not retrieved in this queue
     * @return the queue where are stored the message received from server
     */
    public BlockingQueue<JsonElement> getQueueFromServer() {
        return in;
    }

    /**
     * Return if the ConnectionHandler is running or not
     * @return true if this class thread is running, of false otherwise
     */
    public boolean isRunning() {
        return hasToRun;
    }

    /**
     * Start all the thread needed for handling the connection
     */
    @Override
    public void run() {

        //set to run
        this.hasToRun = true;

        new Thread(this.pingTimer).start();
        new Thread(this.listener).start();
        new Thread(this.talker).start();


        synchronized (this.lock) {
            while (hasToRun) {
                try {
                    this.lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Stop the ConnectionHandler and all his inner class including all thread created by him, close also the socket
     */
    public void stopConnectionHandler (){
        synchronized(this.lock) {
            this.hasToRun = false;
            this.lock.notifyAll();
        }

        this.pingTimer.stopPing();
        if (this.listener.isRunning())
            this.listener.stopListener();
        if (this.talker.isRunning())
            this.talker.stopTalker();

        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
