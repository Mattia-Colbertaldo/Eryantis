package it.polimi.ingsw.Server;

import com.google.gson.Gson;
import it.polimi.ingsw.Enum.Errors;
import it.polimi.ingsw.Message.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Server s;
    private Socket client = null;
    private ServerSocket server = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private int port = 0;
    private int id;
    private String username;
    private Lobby l;
    Gson gson;
    public ClientHandler (Server s, ServerSocket server, Socket client, int id, Lobby l){
        this.s = s;
        this.server = server;
        this.client = client;
        this.id = id;
        this.l = l;
        this.gson = new Gson();
        try {
            this.in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            this.out = new PrintWriter(
                    client.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Message readJson(){
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ( (line = in.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            String content = sb.toString();
            System.out.println(content);
            String json = this.gson.toJson(content);
            Message m = this.gson.fromJson(json, Message.class);
            return m;
        } catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void Send(Message m){
        String json = this.gson.toJson(m);
        out.println(json);
    }

    @Override
    public void run() {

        System.out.println("Client accepted");

        try {

            ReceiveFirstMessage();

            SendFirstMessage();

            ReceiveData();

            SendId();

            ReceiveConfirm();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        l.SetOk(id, username);
        
        Ping ping = new Ping(this);
        new Thread(ping).start();

    }

    void ReceiveFirstMessage() throws IOException, ClassNotFoundException {
        Message m = readJson();
        System.out.println(m.getError() + ", " + m.getMessage());

    }

    void SendFirstMessage() throws IOException {
        Send(new Message(Errors.NO_ERROR, "Welcome Client"));
        if(this.id == 0){
            int num = SendNumOfPlayersRequest();
            int gameMode = SendGameModeRequest();
            this.l.setParameters(num, gameMode);
        }
        Send(new Message(Errors.NO_ERROR, "Choose your username"));
    }

    void ReceiveData(){
        Message m = readJson();
        this.username = m.getMessage();
    }

    void SendId(){
        Send(new Message(Errors.NO_ERROR, String.valueOf(this.id)));
    }

    void ReceiveConfirm(){
        Message m = readJson();
        System.out.println(m.getError() + ", " + m.getMessage());
    }

    int SendNumOfPlayersRequest(){

        Send(new Message(Errors.NO_ERROR, "How many players will the game be composed of?"));
        int num = -1;
        while(num == -1){
            try{
                Message m = readJson();
                System.out.println("Received: " + m.getMessage());
                num = Integer.parseInt(m.getMessage());
                if(num<2 || num>4){
                    out.println(new Message(Errors.NUM_OF_PLAYER_ERROR, "Please select a valid number (2-4): "));
                    num = -1;
                }
            }catch(Exception e){
                out.println(new Message(Errors.NUM_OF_PLAYER_ERROR, "Please select a valid number (2-4): "));
                num = -1;
            }
        }
        return num;
    }

    int SendGameModeRequest(){
        Send(new Message(Errors.NO_ERROR, "Select the game mode (0 - Normal, 1 - Advanced) : "));
        int num = -1;
        while(num == -1){
            try{
                Message m = readJson();
                System.out.println("Received: " + m.getMessage());
                num = Integer.parseInt(m.getMessage());
                if(num != 0 || num != 1){
                    out.println(new Message(Errors.WRONG_GAME_MODE, "Please select a valid game mode (0/1): "));
                    num = -1;
                }
            }catch(Exception e){
                out.println(new Message(Errors.WRONG_GAME_MODE, "Please select a valid game mode (0/1): "));
                num = -1;
            }
        }
        return num;

    }

    public String getUsername() {
        return this.username;
    }


    void stop(){
        try {
            in.close();
            out.close();
            client.close();
            //server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClient() {
        return client;
    }
}

class Ping implements Runnable{

    private ClientHandler ch;

    public Ping(ClientHandler ch){
        this.ch = ch;
    }

    @Override
    public void run() {
        Socket client = ch.getClient();
        InetAddress inet = client.getInetAddress();
        boolean isReachable;

        while(true){
            try {
                isReachable = inet.isReachable(5000);
                if(isReachable){
                    System.out.println("Host is Reachable");
                }
                else{
                    System.out.println("Host is not reachable. Closing connection...");
                    ch.stop();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
