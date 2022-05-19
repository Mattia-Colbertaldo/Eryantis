package it.polimi.ingsw.Network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import it.polimi.ingsw.Enum.Errors;
import it.polimi.ingsw.Message.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class Listener implements Runnable{

    private final Gson gson = new GsonBuilder().create();
    private final BlockingQueue<JsonElement> messageDest;
    private final BufferedReader in;
    private final PingTimer ping;


    private volatile boolean go = false;

    public Listener(BlockingQueue<JsonElement> messageSource, InputStream inputStream, PingTimer ping) {
        this.messageDest = messageSource;
        this.in = new BufferedReader(new InputStreamReader(inputStream));
        this.ping = ping;
    }

    @Override
    public void run() {
        //listener thread
        this.go = true;
        while (this.go){
            //retrieve message from server
            String message = null;
            try {
                while (message == null)
                    message = in.readLine();
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    System.err.println("Listener: Socket Close");
                    this.ping.triggerServerError();
                    this.go = false;
                }
                else
                    System.err.println("Listener: Error while reading from socket");

                continue;
            }

            //reset the ping timer of receiver
            this.ping.resetReceiveTimer();

            //convert the message
            JsonElement messageJ = this.gson.fromJson(message, JsonElement.class);

            //check if the message is not only a ping message
            Message m = this.gson.fromJson(messageJ, Message.class);
            if (m.getError() != Errors.PING) {

                //if is not a ping message put it in the out queue
                try {
                    this.messageDest.put(messageJ);
                } catch (InterruptedException e) {
                    System.err.println("Listener: Interrupted when putting data in the queue");
                    this.go = false;
                }
            }
        }
        System.out.println("Listener Stopped");
    }

    public void stopListener(){
        this.go = false;
    }
}
