package it.polimi.ingsw.Server.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class GameBoard {

    private final ArrayList<Cloud> clouds;
    private final Islands islands;
    //private final Collection<School> schools = null;
    private final Professors professors;
    private final GameInitializer gInit;
    private final Bag bag;

    GameBoard(GameInitializer gInit, int numOfPlayer){

        this.gInit = gInit;

        //instantiating clouds
        //this.clouds = new ArrayList<>(gInit.getNumberOfPlayers());
        this.clouds = new ArrayList<>(numOfPlayer);

        this.bag = new Bag();

        //for(int i=0; i < gInit.getNumberOfPlayers(); i++){
        for(int i=0; i < numOfPlayer; i++){
            clouds.add(new Cloud(bag, numOfPlayer));
        }

        //connecting schools
        /*for (Player p : gInit.getPlayers()){
            schools.add(p.getSchool());
        }*/

        //instantiating islands
        this.islands = new Islands(this);

        //instantiating professors
        this.professors = new Professors(gInit);


    }

    void AddStudentToIsland(Color color, int id){
        this.islands.AddStudentToIsland(color, id);
        this.professors.updateProfessors();
    }

    void MoveMotherNature(int count){
        this.islands.MoveMotherNature(count);
    }

    void NewRound(){
        PopulateClouds();
    }

    void PopulateClouds(){
        for(Cloud cloud : clouds){
            cloud.AddStudents();
        }
    }

    Collection<Player> getPlayerFromTower(int TowerColor){
        Collection<Player> temp = new ArrayList<>(4);
        Collection<Player> players = gInit.getPlayers();
        for (Player p : players){
            if (p.getTowerColor() == TowerColor) temp.add(p);
        }
        return temp;
    }

    void getCloud(int cloudId){

    }

    ArrayList<Cloud> getClouds() {
        return clouds;
    }


    GameInitializer getgInit() {
        return gInit;
    }

    Islands getIslands() {
        return islands;
    }

    Professors getProfessors() {
        return professors;
    }
}
