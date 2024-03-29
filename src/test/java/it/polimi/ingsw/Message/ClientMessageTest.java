package it.polimi.ingsw.Message;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import it.polimi.ingsw.Enum.Errors;
import it.polimi.ingsw.Server.Model.Game;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientMessageTest {

    @Test
    void getMoveId() {
        ClientMessage m1 = new PlayAssistantMessage(Errors.NO_ERROR, "Play Assistant", 1);
        assertEquals(1, m1.getMoveId());
    }

    @Test
    void executeMove() {
        int[] ids = new int[2];
        ids[1] = 1;

        Game g = Game.getGameModel(ids, 1);

        ClientMessage m1 = new PlayAssistantMessage(Errors.NO_ERROR, "Play Assistant", 1);
        ClientMessage m2 = new MoveStudentMessage(Errors.NO_ERROR, "Move Student", 2, -1);
        ClientMessage m3 = new MoveMotherNatureMessage(Errors.NO_ERROR, "Move MotherNature", 3);
        ClientMessage m4 = new ChooseCloudMessage(Errors.NO_ERROR, "Choose Clouds", 2);
        ClientMessage m5 = new PlayCharacterMessage(Errors.NO_ERROR, "Play Character", 9, null);

        assertTrue(m1.executeMove(g, 0) >= 0); //the only one >= 0 because is the only one that can return no error
        assertTrue(m2.executeMove(g, 0) > 0);
        assertTrue(m3.executeMove(g, 0) > 0);
        assertTrue(m4.executeMove(g, 0) > 0);
        assertTrue(m5.executeMove(g, 0) > 0);
    }

    @Test
    void getElement (){
        PlayAssistantMessage p = new PlayAssistantMessage(Errors.NO_ERROR, "", 1);

        Gson g = new Gson();
        JsonElement j = g.toJsonTree(p);
        JsonElement x = j.getAsJsonObject().get("moveId");
        int i = x.getAsInt();
        assertEquals(1, i);
    }
}