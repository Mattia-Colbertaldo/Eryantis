package it.polimi.ingsw.Server.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class GameBoard {

    private final Collection<Cloud> clouds;
    private final Islands islands;
    //private final Collection<School> schools = null;
    private final Professors professors;
    private final GameInitializer gInit;

    public GameBoard(GameInitializer gInit, int numOfPlayer){

        this.gInit = gInit;

        //instantiating clouds
        //this.clouds = new ArrayList<>(gInit.getNumberOfPlayers());
        this.clouds = new ArrayList<>(numOfPlayer);


        //for(int i=0; i < gInit.getNumberOfPlayers(); i++){
        for(int i=0; i < numOfPlayer; i++){
            clouds.add(new Cloud());
        }

        //connecting schools
        /*for (Player p : gInit.getPlayers()){
            schools.add(p.getSchool());
        }*/

        //instantiating islands
        this.islands = new Islands(gInit);

        //instantiating professors
        this.professors = new Professors();


    }

    public void AddStudentToIsland(Color color, Island island){
        this.islands.AddStudentToIsland(color, island);
    }

    public void MoveMotherNature(int count){
        this.islands.MoveMotherNature(count);
    }

    public void NewRound(){
        PopulateClouds();
    }

    private void PopulateClouds(){
        for(Cloud cloud : clouds){
            cloud.AddStudents();
        }
    }

    public Collection<Player> getPlayerFromTower(int TowerColor){
        Collection<Player> temp = new ArrayList<>(4);
        Collection<Player> players = gInit.getPlayers();
        for (Player p : players){
            if (p.getTowerColor() == TowerColor) temp.add(p);
        }
        return temp;
    }

    public void ChooseCloud(Player player, Cloud cloud){

    }
}
