package it.polimi.ingsw.Message;

import it.polimi.ingsw.Enum.Assistant;
import it.polimi.ingsw.Enum.Color;
import it.polimi.ingsw.Enum.Errors;
import it.polimi.ingsw.Server.Model.*;
import it.polimi.ingsw.Server.Model.Characters.*;
import it.polimi.ingsw.Server.Model.Characters.Character;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ModelMessage {

    public class PlayerSerializable{
        private final int id;
        private final int towerColor;
        private final int[] assistantDeck;
        private final int activeAssistant;
        private final School school;
        private final int coins;

        PlayerSerializable(Player p, int mode) {
            this.id = p.getId();
            this.towerColor = p.getTowerColor();
            this.school = p.getSchool();
            if (p.getActiveAssistant() == null)
                this.activeAssistant = -1;
            else
                this.activeAssistant = p.getActiveAssistant().getValue();

            int i = 0;
            for (Assistant a: p)
                i++;
            this.assistantDeck = new int[i];
            i = 0;
            for (Assistant a: p)
                this.assistantDeck[i++] = a.getValue();
            if (mode == 0)
                this.coins = 0;
            else
                this.coins = ((AdvancedPlayer)p).getCoins();
        }

        public int getId() {
            return id;
        }

        public int getTowerColor() {
            return towerColor;
        }

        public int[] getAssistantDeck() {
            return assistantDeck;
        }

        public int getActiveAssistant() {
            return activeAssistant;
        }

        public School getSchool() {
            return school;
        }

        public int getCoins() {
            return coins;
        }
    }

    public class CloudSerializable{

        private final int id;
        private final int[] drawnStudents;

        CloudSerializable(Cloud c) {
            this.id = c.getId();
            this.drawnStudents = c.getCopyOfDrawnStudents();
        }
    }

    public class normalCharacterSerializable{
        private final int id;
        private final int cost;
        private final boolean used;

        public normalCharacterSerializable(Character c) {
            this.id = c.getId();
            this.cost = c.getCost();
            this.used = c.getUsed();
        }
    }

    public final class ApothecarySerializable extends normalCharacterSerializable{
        private final int banCard;

        ApothecarySerializable(Apothecary c) {
            super(c);
            this.banCard = c.getBanCard();
        }
    }

    public final class CookSerializable extends normalCharacterSerializable{
        private final int colorId;

        public CookSerializable(Cook c) {
            super(c);
            Color temp = c.getColor();
            if (temp == null)
                this.colorId = -1;
            else
                this.colorId = temp.getIndex();
        }
    }

    public final class ClericSerializable extends normalCharacterSerializable{
        private final int[] students;

        ClericSerializable(Cleric c) {
            super(c);
            this.students = c.getStudentsCopy();
        }
    }

    public final class JesterSerializable extends normalCharacterSerializable{
        private final int[] students;

        JesterSerializable(Jester c) {
            super(c);
            this.students = c.getStudentsCopy();
        }
    }

    public final class PrincessSerializable extends normalCharacterSerializable{
        private final int[] students;

        PrincessSerializable(Princess c) {
            super(c);
            this.students = c.getStudentsCopy();
        }
    }


    private final String time;
    private final int errorCode;
    private final int gameMode;
    private final int playerNumber;
    private final int winnerId;

    private final int currentPlayerId;

    private final String actualPhase;
    private final String actualActionPhase;
    private final int studentsMoved;

    private final int motherNatureIslandId;

    private final int[] professorsList;

    private final int[] bag;

    private final List<Island> islandList;

    private final List<CloudSerializable> cloudList;

    private final List<PlayerSerializable> playerList; //or advanced player if game mode 1

    private final List<normalCharacterSerializable> characterList;

    private final int activeCharacterId;

    protected ModelMessage(GameInitializer g, Errors er) {
        Islands s = g.getIslands();
        GameBoard b = g.getBoard();
        RoundHandler r = g.getRoundHandler();

        this.winnerId = g.getWinningPlayerId();
        this.errorCode = er.getCode();
        this.time = Instant.now().toString();
        this.gameMode = g.getGameMode();
        this.playerNumber = g.getPlayersNumber();
        this.currentPlayerId = r.getCurrent().getId();
        this.actualPhase = r.getPhase().toString();
        this.actualActionPhase = r.getActionPhase().toString();
        this.studentsMoved = r.getStudentMovedInThisTurn();
        this.motherNatureIslandId = s.getMotherNature().getId();
        this.professorsList = g.getProfessors().getProfessorsCopy();
        this.bag = g.getBag().getStudentsCopy();


        this.islandList = new ArrayList<>(s.getIslandsNumber());
        for (Island island : s)
            this.islandList.add(island);

        this.cloudList = new ArrayList<>(this.playerNumber);
        for (Cloud cloud : b)
            this.cloudList.add(new CloudSerializable(cloud));

        this.playerList = new ArrayList<>(this.playerNumber);
        for (Player player : g)
            this.playerList.add(new PlayerSerializable(player, this.gameMode));

        if (this.gameMode == 1) {
            this.characterList = new ArrayList<>(3);
            for (int i = 0; i < 12; i++)
                if (b.existsCharacter(i)) {
                    Character temp = b.getCharacterById(i);
                    int tempId = temp.getId();
                    if (tempId == 0)
                        this.characterList.add(new ApothecarySerializable((Apothecary) temp));
                    else if (tempId == 3)
                        this.characterList.add(new CookSerializable((Cook) temp));
                    else if (tempId == 2)
                        this.characterList.add(new ClericSerializable((Cleric) temp));
                    else if (tempId == 6)
                        this.characterList.add(new JesterSerializable((Jester) temp));
                    else if (tempId == 10)
                        this.characterList.add(new PrincessSerializable((Princess) temp));
                    else
                        this.characterList.add(new normalCharacterSerializable(temp));
                }
            Character t = b.getActiveCharacter();

            if (t != null)
                this.activeCharacterId = t.getId();
            else
                this.activeCharacterId = -1;
        }
        else {
            this.characterList = null;
            this.activeCharacterId = -1;
        }
    }

    public boolean gameIsOver (){
        return this.winnerId != -1;
    }

    public int getCurrentPlayerId() {
        return currentPlayerId;
    }

    public String getActualPhase() {
        return actualPhase;
    }

    public String getActualActionPhase() {
        return actualActionPhase;
    }

    public int getStudentsMoved() {
        return studentsMoved;
    }

    public List<Island> getIslandList() {
        return islandList;
    }

    public int getMotherNatureIslandId() {
        return motherNatureIslandId;
    }

    public int[] getProfessorsList() {
        return professorsList;
    }

    public int[] getBag() {
        return bag;
    }

    public List<PlayerSerializable> getPlayerList() {
        return playerList;
    }
}