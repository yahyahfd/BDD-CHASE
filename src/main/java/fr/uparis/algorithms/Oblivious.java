package fr.uparis.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import fr.uparis.constraints.database.*;
import fr.uparis.database.Database;
import fr.uparis.database.Table;

public class Oblivious {

    public static boolean obliviousChase(Database database, int iterations) {
        //liste de tuples
        ArrayList<List<Object>> list = new ArrayList<>();
        try {
            for (GenerationDependencies generationDependency : database.getGenerationDependencies()) {
                // On vérifie pour chaque tuple de table si generationDependency.corps est
                // satisfaite
                // Si c'est le cas, on vérifie si ça satisfait PAS generationDependency.tete
                // et si e n'a pas été appliquée à ce tuple (? comment ça mon reuf, avec ces
                // for, c'est deja le cas?)


                if (generationDependency instanceof TGD tgd && !list.contains(generationDependency)) {
                    /*
                     * ajouter un nouveau tuple u a D
                     */
                    Pair<Table, List<Object>> toAdd = tgd.isSatisfiedOblivious(list);

                    while (toAdd != null) {
                        List<MutablePair<String, Object>> rowsToAdd = new ArrayList<>();
                        Table table = toAdd.getLeft();
                        List<Object> newTuple = toAdd.getRight();
                        for (String columnName : table.getColumns()) {
                            int index = table.getColumnIndex(columnName);
                            rowsToAdd.add(MutablePair.of(columnName,newTuple.get(index)));
                        }
                        boolean trueorfalse = table.addRow(rowsToAdd, database);
                        System.out.println("updated TGD? " + trueorfalse);
                        toAdd = tgd.isSatisfiedOblivious(list);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("exception : "+e);
            return false;
        }
        // renvoyer false si D non satisfaite si on arrete le programme? je ne sais pas
        return true;
    }
}
