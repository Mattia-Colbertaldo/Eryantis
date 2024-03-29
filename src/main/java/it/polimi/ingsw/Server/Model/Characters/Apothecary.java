package it.polimi.ingsw.Server.Model.Characters;

import it.polimi.ingsw.Enum.Errors;
import it.polimi.ingsw.Message.ModelMessage.CharacterSerializable;
import it.polimi.ingsw.Server.Model.GameInitializer;
import it.polimi.ingsw.Server.Model.Island;

/**
 * Apothecary class, can use the ban card
 */
final public class Apothecary extends Character {

    private int banCard;

    //not public only created in factory in Character class
    Apothecary(GameInitializer gameInitializer) {
        super (0, 2, gameInitializer, "Apothecary");
        this.banCard = 4;
    }

    Apothecary(GameInitializer gameInitializer, CharacterSerializable character) {
        super (gameInitializer, character);
        this.banCard = character.getBanCard();
    }

    // used to add a BanCard "token" to this card (after being removed from an island)
    public void addBanCard() { this.banCard += 1; }

    // used to remove a BanCard "token" from this card (and to add it to an island)
    public void removeBanCard() { this.banCard -= 1; }

    // return number of banCard "token" on this card
    public int getBanCard() { return this.banCard; }

    @Override
    public void activateEffect(int[] obj) {

        int islandId = obj[0];

        Island i = gameInitializer.getIslands().getIslandFromId(islandId);

        // remove banCard from this
        removeBanCard();

        // add banCard to island
        i.setBanCard();

    }

    @Override
    public Errors canActivateEffect(int[] obj) {
        if (obj.length != 1)
            return Errors.NOT_RIGHT_PARAMETER;

        int islandId = obj[0];

        if (!gameInitializer.getIslands().existsIsland(islandId))
            return Errors.NOT_VALID_DESTINATION;
        if (this.banCard <= 0)
            return Errors.NOT_ENOUGH_TOKEN;
        return Errors.NO_ERROR;
    }

}