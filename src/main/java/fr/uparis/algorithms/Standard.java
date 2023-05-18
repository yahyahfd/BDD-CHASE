package fr.uparis.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import fr.uparis.constraints.database.*;
import fr.uparis.database.Database;
import fr.uparis.database.Table;

public class Standard {

    public static boolean chase(Database database) {
        try {
            for (GenerationDependencies generationDependency : database.getGenerationDependencies()) {
                // On vérifie pour chaque tuple de table si generationDependency.corps est
                // satisfaite
                // Si c'est le cas, on vérifie si ça satisfait PAS generationDependency.tete
                // et si e n'a pas été appliquée à ce tuple (? comment ça mon reuf, avec ces
                // for, c'est deja le cas?)

                if (generationDependency instanceof EGD) {
                    /*
                     * egaliser les tuples de T en accord avec e, C’est à dire, pour chaque paire de
                     * variables wi, wj telles que wi = wj apparaît dans la
                     * contrainte, remplacer l’élément du tuple correspondant à wi par celui
                     * correspondant à wj (ou l’inverse). Si l’un des deux éléments est une
                     * constante,
                     * on la choisit toujours comme “remplaçant”, sinon le choix est libre.
                     */
                    EGD egd = (EGD) generationDependency;
                    Pair<Pair<Table, List<Object>>, Pair<String, Object>> toModify = egd.isSatisfied();
                    while (toModify != null) {// tant que non satisfaite
                        // public List<List<Object>> selectFromTable(List<Pair<String,?>> values) throws
                        // FormatException{
                        Pair<String, Object> newValue = toModify.getRight();
                        Pair<Table, List<Object>> tableAndTuple = toModify.getLeft();
                        tableAndTuple.getLeft().updateTuple(tableAndTuple.getRight(),
                                newValue, database);
                        System.out.println("EGD update:" +tableAndTuple.getLeft()+" :"+ newValue);
                        toModify = egd.isSatisfied();
                    }
                }
                if (generationDependency instanceof TGD) {
                    /*
                     * ajouter un nouveau tuple u a D
                     * t.q. D ∪ u satisfait e
                     */
                    TGD tgd = (TGD) generationDependency;
                    Pair<Table, List<Object>> toAdd = tgd.isSatisfied();
                    

                    while (toAdd != null) {
                        List<MutablePair<String, Object>> rowsToAdd = new ArrayList<>();
                        Table table = toAdd.getLeft();
                        List<Object> newTuple = toAdd.getRight();
                        for (String columnName : table.getColumns()) {
                            int index = table.getColumnIndex(columnName);
                            rowsToAdd.add(MutablePair.of(columnName,newTuple.get(index)));
                        }
                        table.addRow(rowsToAdd, database);
                        System.out.println("TGD update:" + rowsToAdd);
                        toAdd = tgd.isSatisfied();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }

    public static boolean obliviousChase(Database database/*, long maxExecutionTimeMillis*/) {
        /*long startTime = System.currentTimeMillis();
        long endTime = startTime + maxExecutionTimeMillis;*/

        try {
            /*while(System.currentTimeMillis() < endTime){
                boolean appliedConstraint = false;*/

            for (GenerationDependencies generationDependency : database.getGenerationDependencies()) {
                // On vérifie pour chaque tuple de table si generationDependency.corps est
                // satisfaite
                // Si c'est le cas, on vérifie si ça satisfait PAS generationDependency.tete
                // et si e n'a pas été appliquée à ce tuple (? comment ça mon reuf, avec ces
                // for, c'est deja le cas?)

                if (generationDependency instanceof TGD) {
                    /*
                     * ajouter un nouveau tuple u a D
                     * t.q. D ∪ u satisfait e
                     */
                    TGD tgd = (TGD) generationDependency;
                    Pair<Table, List<Object>> toAdd = tgd.isSatisfiedOblivious();

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

                        //appliedConstraint = true;
                        toAdd = tgd.isSatisfied();

                        // on verifie le temps écoulé
                            /*if(System.currentTimeMillis() >= endTime){
                                return false; // si le temps est écoulé, fin de l'algo
                            }*/
                    }
                }
            }

                /*if(!appliedConstraint){
                    // si aucune contrainte a été appliquée lors du parcours, fin de l'algo
                    return true;
                }
            }*/
        } catch (Exception e) {
            System.out.println("exception : "+e);
            return false;
        }
        // renvoyer false si D non satisfaite si on arrete le programme? je ne sais pas
        return true;
    }
}
