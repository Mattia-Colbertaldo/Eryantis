package it.polimi.ingsw.Server.Model.Characters;

import it.polimi.ingsw.Enum.Color;
import it.polimi.ingsw.Enum.Errors;
import it.polimi.ingsw.Server.Model.*;

import java.util.Arrays;
import java.util.Objects;

final public class Princess extends Character {
    private final int[] students;

    Princess(GameInitializer gameInitializer) {
        super (10, 2, gameInitializer, "Princess");
        students = gameInitializer.getBag().drawStudents(4);
    }

    public int[] getStudents() {
        return this.students;
    }

    public int[] getStudentsCopy(){
        return Arrays.copyOf(students, students.length);
    }

    @Override
    protected void activateEffect(int[] obj) {

        int colorId = obj[0];

        Color c = Color.getColorById(colorId);
        Player p = super.gameInitializer.getRoundHandler().getCurrent();

        this.students[colorId]--;

        p.getSchool().moveStudentToRoom(Objects.requireNonNull(c));

        int[] newStudent = super.gameInitializer.getBag().drawStudents(1);
        for (Color color: Color.values())
            this.students[color.getIndex()] += newStudent[color.getIndex()];
    }

    @Override
    public Errors canActivateEffect(int[] obj) {

        if (obj.length != 1)
            return Errors.NOT_RIGHT_PARAMETER;

        int colorId = obj[0];
        Player p = super.gameInitializer.getRoundHandler().getCurrent();

        if (!Color.isColorIdValid(colorId))
            return Errors.NOT_VALID_COLOR;
        if (!super.gameInitializer.existsPlayer(p.getId()))
            return Errors.PLAYER_NOT_EXIST;
        if (this.students[colorId] <= 0)
            return Errors.NO_STUDENT;

        Color c = Color.getColorById(colorId);

        if (p.getNumberOfStudentInRoomByColor(c) == 10)
            return Errors.NO_MORE_MOVEMENT;

        return Errors.NO_ERROR;
    }
}
