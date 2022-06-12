package it.polimi.ingsw.Client;

import it.polimi.ingsw.Client.GraphicInterface.Graphic;
import it.polimi.ingsw.Enum.Wizard;
import it.polimi.ingsw.Message.ClientMessage;
import it.polimi.ingsw.Message.LobbyInfoMessage;
import it.polimi.ingsw.Message.ModelMessage.ModelMessage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DataCollector {

    private final Graphic gInstance;

    private Integer first = -1; //-1 initial case, 0 first, 1 not first
    private Runnable firstCallback = null;

    private Integer done = -1; //-1 initial or during verification/acquisition of data, 0 incorrect, 1 correct
    private String errorData = null; //error message sent from server when the data sent are not correct
    private Runnable doneCallback = null;


    private String username = null;
    private Wizard wizard = null;

    private Integer gameMode = -1;
    private Integer numOfPlayers = -1;
    private Map<Integer, String> usernames = null;
    private Map<Integer, Wizard> wizards = null;
    private Integer id = null;

    private final Object lock = new Object();

    private ModelMessage model = null;

    private Runnable callbackForModel = null;

    private ClientMessage nextMove = null;

    //all this method will be called by the Graphic class

    /**
     * Constructor called by a Graphic class
     * @param gInstance the Graphic class that is creating this class
     */
    public DataCollector(Graphic gInstance) {
        this.gInstance = gInstance;
    }


    /**
     * Called by the Graphic class, get the value set by the main server, and if the value is not set, call the passed runnable with Platform.runLater() when set
     * @param whenChangedIfNotValid the runnable to be executed if the value requested is not in a valid state
     * @return the actual state of the value
     */
    public int getFirst(Runnable whenChangedIfNotValid) {
        if (whenChangedIfNotValid == null) {
            System.out.println("Warning: no runnable passed at getFirst");
        }

        if (first == -1 && whenChangedIfNotValid != null)
            firstCallback = whenChangedIfNotValid;
        return first;
    }

    /**
     * Called by the Graphic class, get the value set by the main server, and if the value is not set, call the passed runnable with Platform.runLater() when set
     * @param whenChangedIfNotValid the runnable to be executed if the value requested is not in a valid state
     * @return the actual state of the value
     */
    public int getDone(Runnable whenChangedIfNotValid) {
        if (whenChangedIfNotValid == null) {
            System.out.println("Warning: no runnable passed at getFirst");
        }
        if (done == -1 && whenChangedIfNotValid != null)
            doneCallback = whenChangedIfNotValid;
        if (done == 0) {
            this.done = -1;
            return 0;
        }
        return done;
    }

    /**
     * If the Info sended by the client to the server are not corrected, the server will send an error with this message that explained it
     * @return the string set by the main thread of client
     */
    public String getErrorData() {
        return errorData;
    }


    public void setCallbackForModel (Runnable callbackForModel) {
        this.callbackForModel = callbackForModel;
    }

    public ModelMessage getModel() {
        return model;
    }

    public Map<Integer, String> getUsernames() {
        return usernames;
    }

    public Map<Integer, Wizard> getWizards() {
        return wizards;
    }

    public void setNextMove(ClientMessage nextMove) {
        if (nextMove == null)
            return;
        synchronized (this.lock){
            this.nextMove = nextMove;
            this.lock.notifyAll();
        }
        System.out.println("Move Set");
    }

    public Integer getId() {
        return id;
    }

    //all this method will be called by the Client main thread itself


    public Graphic getGraphicInstance() {
        return gInstance;
    }

    public void setFirst (boolean first){
        if (first)
            this.first = 0;
        else
            this.first = 1;

        if (this.firstCallback != null){
            this.firstCallback.run();
        }
    }

    public void setDone (boolean done, @Nullable String message){
        if (done) {
            this.done = 1;
            this.errorData = null;
        }
        else {
            this.done = 0;
            this.errorData = message;

            //resetting the value
            this.username = null;
            this.wizard = null;
            this.gameMode = -1;
            this.numOfPlayers = -1;
        }

        if (this.doneCallback != null){
            this.doneCallback.run();
        }
    }

    public void setModel(ModelMessage model) {

        if (model == null && this.model == null)
            return;

        if (model != null) {
            this.model = model;
        }

        this.callbackForModel.run();
    }

    public void setUsernames(Map<Integer, String> usernames) {
        this.usernames = usernames;
    }

    public void setWizards(Map<Integer, Wizard> wizards) {
        this.wizards = wizards;
    }

    public ClientMessage askMove() throws InterruptedException {
        ClientMessage mess;
        synchronized (this.lock) {
            while (this.nextMove == null) {
                this.lock.wait();
            }
            mess = this.nextMove;
            this.nextMove = null;
        }

        return mess;
    }

    //this method can be called both from the Graphic class and the main client thread

    public void setUsername(String username) {
        synchronized (this.lock) {
            this.username = username;
            this.lock.notifyAll();
        }
    }

    public void setWizard(Wizard wizard) {
        synchronized (this.lock) {
            this.wizard = wizard;
            this.lock.notifyAll();
        }
    }

    public void setGameMode(int gameMode) {
        synchronized (this.lock) {
            this.gameMode = gameMode;
            this.lock.notifyAll();
        }
    }

    public void setNumOfPlayers (int numOfPlayers) {
        synchronized (this.lock) {
            this.numOfPlayers = numOfPlayers;
            this.lock.notifyAll();
        }
    }

    public String getUsername() throws InterruptedException {
        synchronized (this.lock) {
            while (this.username == null) {
                this.lock.wait();
            }
        }
        return this.username;
    }

    public Wizard getWizard() throws InterruptedException {
        synchronized (this.lock) {
            while (this.wizard == null) {
                this.lock.wait();
            }
        }
        return this.wizard;
    }

    public int getGameMode() throws InterruptedException {
        synchronized (this.lock) {
            while (this.gameMode == -1) {
                this.lock.wait();
            }
        }
        return gameMode;
    }

    public int getNumOfPlayers() throws InterruptedException {
        synchronized (this.lock) {
            while (this.numOfPlayers == -1) {
                this.lock.wait();
            }
        }
        return this.numOfPlayers;
    }

    public void setGameData(LobbyInfoMessage gameData, int id) {
        this.gameMode = gameData.getGameMode();
        this.numOfPlayers = gameData.getNumOfPlayer();
        this.usernames = gameData.getUsernames();
        this.wizards = gameData.getWizards();
        this.id = id;
    }


    //some usefully method that pre-compute some information

    public boolean isThisMyTurn() {
        return this.id == this.model.getCurrentPlayerId();
    }

    public String getUsernameOfCurrentPlayer () {
        return this.usernames.get(this.model.getCurrentPlayerId());
    }

    public String getStandardWinMessage() {
        if (!this.model.gameIsOver())
            throw new IllegalStateException("Cannot invoke this method if the game is not finished");

        int winningId = this.model.getWinnerId();

        if (this.numOfPlayers != 4){
            return "The player " + this.usernames.get(winningId) + " with id " + winningId + " has won the game, congratulation";
        }
        else{
            //todo 4 player win message
            return null;
        }
    }
}