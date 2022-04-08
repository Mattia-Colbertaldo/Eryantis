package it.polimi.ingsw.Server.Model;

import it.polimi.ingsw.Server.Errors;

//todo calc influence little method, finish check win, check if the players can play the character during the planning phase

// This class is the interface towards the controller. It also check if the move from player/controller are valid. The only methods tha can be invoked from the controller are the factory method for getting a Game Instance or and advanced one, and the method for the possible interactions of users to the model
public class Game{

    private final int numOfPlayer;

    protected final GameInitializer gameInit;
    protected final RoundHandler round;

    Game (int numOfPlayer, GameInitializer gameInit, RoundHandler round) {
        this.numOfPlayer = numOfPlayer;
        this.gameInit = gameInit;
        this.round = round;
    }


    public int playAssistant (int playerId, int assistantValue){

        if (!gameInit.existsPlayer(playerId))
            return Errors.PLAYER_NOT_EXIST.getCode();
        if (assistantValue < 1 || assistantValue > Assistant.getNumberOfAssistants())
            return Errors.NOT_VALID_ASSISTANT.getCode();

        Player p = gameInit.getPlayerById(playerId);
        Assistant assistant = Assistant.getAssistantByValue(assistantValue);

        // check if it's a possible moves
        if (!p.equals(round.getCurrent()))
            return Errors.NOT_CURRENT_PLAYER.getCode();
        if (!round.getPhase().equals(Phase.Planning))
            return Errors.NOT_RIGHT_PHASE.getCode();
        if (!p.hasAssistant(assistant))
            return Errors.NO_SUCH_ASSISTANT.getCode();
        if (!round.canPLayAssistant(p, assistant))
            return Errors.ASSISTANT_ALREADY_PLAYED.getCode();

        // call the correct method in player to modify the model
        p.playAssistant(assistant);

        // update the round handler
        round.next();

        return Errors.NO_ERROR.getCode();
    }

    public int moveStudents (int playerId, int colorId, int destinationId){

        if (!gameInit.existsPlayer(playerId))
            return Errors.PLAYER_NOT_EXIST.getCode();
        if (colorId < 0 || colorId > Color.getNumberOfColors())
            return Errors.NOT_VALID_COLOR.getCode();
        if (destinationId != -1 && !gameInit.getIslands().existsIsland(destinationId))
            return Errors.NOT_VALID_DESTINATION.getCode();

        Player p = gameInit.getPlayerById(playerId);
        Color c = Color.getColorById(colorId);

        if (!p.equals(round.getCurrent()))
            return Errors.NOT_CURRENT_PLAYER.getCode();
        if (!round.getPhase().equals(Phase.Action) || !round.getActionPhase().equals(ActionPhase.MoveStudent))
            return Errors.NOT_RIGHT_PHASE.getCode();

        //check if the player can still move student
        //the check is already done in round handler and this one is redundant because if you have already move all the students in this turn the phase is automatically updated
        int movementDone = round.getStudentMovedInThisTurn();
        if (((numOfPlayer == 2 || numOfPlayer == 4) && movementDone >= 3) || (numOfPlayer == 3 && movementDone >= 4))
            return Errors.NO_MORE_MOVEMENT.getCode();

        //check if the player has the student that he wants to move
        if (!p.hasStudent(c))
            return Errors.NO_STUDENT.getCode();

        //check if student has already 10 student of that color in the room
        if (p.getNumberOfStudentInRoomByColor(c) == 10)
            return Errors.MAX_STUDENT_ROOM.getCode();


        // call the player for move the student
        p.moveStudent(c, destinationId);

        round.next();

        return Errors.NO_ERROR.getCode();
    }

    public int moveMotherNature (int playerId, int position){

        if (!gameInit.existsPlayer(playerId))
            return Errors.PLAYER_NOT_EXIST.getCode();
        if (position < 1)
            return Errors.MOVEMENT_NOT_VALID.getCode();

        Player p = gameInit.getPlayerById(playerId);

        if (!p.equals(round.getCurrent()))
            return Errors.NOT_CURRENT_PLAYER.getCode();
        if (!round.getPhase().equals(Phase.Action) || !round.getActionPhase().equals(ActionPhase.MoveMotherNature))
            return Errors.NOT_RIGHT_PHASE.getCode();

        if (position > p.getActiveAssistant().getMaxMovement())
            return Errors.MOVEMENTS_TOO_HIGH.getCode();

        p.moveMotherNature(position);

        round.next();

        return Errors.NO_ERROR.getCode();
    }

    public int chooseCloud (int playerId, int cloudId){

        if (!gameInit.existsPlayer(playerId))
            return Errors.PLAYER_NOT_EXIST.getCode();
        if (!gameInit.getBoard().existsCloud(cloudId))
            return Errors.NO_SUCH_CLOUD.getCode();

        Player p = gameInit.getPlayerById(playerId);
        Cloud c = gameInit.getBoard().getCloudById(cloudId);

        // check if it's a possible moves
        if (!p.equals(round.getCurrent()))
            return Errors.NOT_CURRENT_PLAYER.getCode();
        if (!round.getPhase().equals(Phase.Action) || !round.getActionPhase().equals(ActionPhase.ChooseCloud))
            return Errors.NOT_RIGHT_PHASE.getCode();

        p.chooseCloud(c);

        round.next();

        return Errors.NO_ERROR.getCode();
    }

    //return the correct interface for the parameter passed, in case of four player the teammates need to have the same final bit (in decimal same team all odds or all even, and the other team the opposite)
    static public Game getGameModel (int[] ids, int gameMode){

        //check game mode and number of player
        if (gameMode < 0 || gameMode > 1 || ids.length < 2 || ids.length > 4)
            return null;

        //check id > 0
        boolean allPositive = true;
        for (int id : ids)
            if (id < 0) {
                allPositive = false;
                break;
            }
        if (!allPositive)
            return null;

        //check same id
        boolean sameId = false;
        for (int i = 0; i < ids.length && !sameId; i++) {
            for (int j = i + 1; j < ids.length && !sameId; j++){
                if (ids[i] == ids[j])
                    sameId = true;
            }
        }
        if (sameId){
            return null;
        }

        //check odd and even for 4 player game
        if (ids.length == 4){
            int teamA = 0, teamB = 0;
            for (int i = 0; i < 4; i++){
                if (ids[i] % 2 == 0){
                    teamA++;
                }
                else
                    teamB++;
            }
            if (teamA != 2 || teamB != 2)
                return null;
        }


        GameInitializer gInit = new GameInitializer(gameMode, ids.length);

        //round handler is used for track the phase, the round, and the cycle of them
        RoundHandler roundHandler = new RoundHandler(gInit);


        //game initializer create all the model
        gInit.createAllGame(ids, roundHandler);
        roundHandler.start();


        //advanced or normal game
        if (gameMode == 0)
            return new Game(ids.length, gInit, roundHandler);
        else
            return new AdvancedGame(ids.length, gInit, roundHandler);
    }
}
