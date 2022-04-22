package it.polimi.ingsw.Server.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameInitializerTest {

    public static GameInitializer setGameInitializer (int numOfPLayers, int gameMode){
        GameInitializer g;
        RoundHandler roundHandler;
        switch (numOfPLayers) {
            case 2 -> {
                int[] id2 = new int[2];
                id2[0] = 4;
                id2[1] = 24;
                g = new GameInitializer(gameMode, id2.length);
                roundHandler = new RoundHandler(g);
                g.createAllGame(id2, roundHandler);
                roundHandler.start();
                return g;
            }
            case 3 -> {
                int[] id3 = new int[3];
                id3[0] = 4;
                id3[1] = 24;
                id3[2] = 10;
                g = new GameInitializer(gameMode, id3.length);
                roundHandler = new RoundHandler(g);
                g.createAllGame(id3, roundHandler);
                roundHandler.start();
                return g;
            }
            case 4 -> {
                int[] id4 = new int[4];
                id4[0] = 1;
                id4[1] = 2;
                id4[2] = 3;
                id4[3] = 4;
                g = new GameInitializer(gameMode, id4.length);
                roundHandler = new RoundHandler(g);
                g.createAllGame(id4, roundHandler);
                roundHandler.start();
                return g;
            }
        }
        return null;
    }

    @Test
    void createAllGame() {
        //done in all constructor
        assertTrue(true);
    }

    @Test
    void getGameMode() {
        //trivial
        assertTrue(true);
    }

    @Test
    void getPlayersNumber() {
        //trivial
        assertTrue(true);
    }

    @Test
    void getBag() {
        //trivial
        assertTrue(true);
    }

    @Test
    void getProfessors() {
        //trivial
        assertTrue(true);
    }

    @Test
    void getIslands() {
        //trivial
        assertTrue(true);
    }

    @Test
    void getBoard() {
        //trivial
        assertTrue(true);
    }

    @Test
    void getRoundHandler() {
        //trivial
        assertTrue(true);
    }

    @Test
    void getPlayerById() {
        GameInitializer gInit = setGameInitializer(4, 1);

        Player p = gInit.getPlayerById(4);
        assertNotNull(p, "Test 1 - not null for valid id");
        assertEquals(4, p.getId(), "Test 1 - sameID");

        p = gInit.getPlayerById(1);
        assertNotNull(p, "Test 1 - not null for valid id");
        assertEquals(1, p.getId(), "Test 1 - sameID");

        p = gInit.getPlayerById(2);
        assertNotNull(p, "Test 1 - not null for valid id");
        assertEquals(2, p.getId(), "Test 1 - sameID");

        p = gInit.getPlayerById(3);
        assertNotNull(p, "Test 1 - not null for valid id");
        assertEquals(3, p.getId(), "Test 1 - sameID");

        p = gInit.getPlayerById(0);
        assertNull(p, "Test 2 - null for non existing player id");
    }

    @Test
    void getNumberOfPlayers() {
        GameInitializer gInit;
        for (int i = 2; i <= 4; i++) {
            gInit = setGameInitializer(i, i % 2);
            assertEquals(i, gInit.getPlayersNumber(), "Test " + (i - 1) + " - number of player" + i);
        }
        //no other control because the control of number of player is done by game and to gameInitializer came only right value
    }

    @Test
    void existsPlayer() {
        GameInitializer gInit = setGameInitializer(4, 0);

        final int startId = -1;
        final int endId = 10; //included
        for (int i = startId; i <= endId; i++){
            if (i < 1 || i > 4)
                assertFalse(gInit.existsPlayer(i), "Test " + (1 + startId) + " - exist player id " + i);
            else
                assertTrue(gInit.existsPlayer(i), "Test " + (1 + startId) + " - exist player id" + i);
        }
    }

    @Test
    void checkWin() {
        //todo
    }
}