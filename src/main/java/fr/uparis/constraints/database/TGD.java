package fr.uparis.constraints.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.database.Database;
import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public class TGD extends GenerationDependencies {
    private final Set<Pair<Table, ConstantAtoms>> relationalAtomsRight = new HashSet<>();
    private final Set<String> commonValues = new HashSet<>();

    public void addRelationalAtomLeft(Table relationalAtom, ConstantAtoms constants, Database dBase)
            throws FormatException {
        this.addRelationalAtom(relationalAtom, constants, dBase, getRelationalAtomsLeft());
    }

    public void addRelationalAtomRight(Table relationalAtom, ConstantAtoms constants, Database dBase)
            throws FormatException {
        this.addRelationalAtom(relationalAtom, constants, dBase, relationalAtomsRight);
    }

    public void addCommonValue(String columnName) throws FormatException {
        commonValues.add(columnName);
    }

    public Pair<Table, List<Object>> isSatisfied() throws FormatException {
        Set<Pair<Table, List<List<Object>>>> left = filterTable(getRelationalAtomsLeft());
        Set<Pair<Table, List<List<Object>>>> right = filterTable(relationalAtomsRight);

        for (Pair<Table, List<List<Object>>> leftPair : left) {// Pour chaque Relation à gauche (R1(x))
            Table leftTable = leftPair.getLeft();
            List<String> leftColumns = leftTable.getColumns();
            for (Pair<Table, List<List<Object>>> rightPair : right) {// Pour chaque relation à droite (R2(x))
                Table rightTable = rightPair.getLeft();
                List<String> rightColumns = rightTable.getColumns();
                // On vérifie si R1 et R2 ont des colonnes présentes dans la liste de
                // commonValues
                leftColumns.retainAll(commonValues);
                rightColumns.retainAll(commonValues);
                leftColumns.retainAll(rightColumns);// on stocke les colonnes en communs entre les 2 tables, mais aussi
                                                    // à retenir dans notre dépendance

                // On doit maintenant parcours les tuples cad la partie droite de leftPair et
                // rightPair
                List<List<Object>> leftTuples = leftPair.getRight();
                List<List<Object>> rightTuples = rightPair.getRight();
                // On parcourt les Tuples des 2 côtés
                for (List<Object> leftTuple : leftTuples) {
                    // Par défaut, tant qu'on a pas vérifié les colonnes en commun, foundValue est à
                    // true si
                    // rightTuple est non vide, sinon false
                    boolean foundValue = true;

                    if (rightTuples.size() == 0) {
                        foundValue = false;
                    } // Aucun tuple à droite...
                    List<Object> correctionTuple = new ArrayList<>();
                    for (List<Object> rightTuple : rightTuples) {
                        for (String columnName : leftColumns) {
                            int indexLeft = leftTable.getColumnIndex(columnName);
                            int indexRight = rightTable.getColumnIndex(columnName);
                            if (leftTuple.get(indexLeft).equals(rightTuple.get(indexRight))) {
                                // On a trouvé une colonne correcte
                                // foundValue est mise à true à chaque colonne correcte
                                foundValue = true;
                            } else {
                                // et est mise à false à la première colonne incorrecte + break pour passer au
                                // tuple droit suivant
                                correctionTuple = new ArrayList<>(Collections.nCopies(rightTuple.size(), null));
                                foundValue = false;
                                break;
                            }
                        }
                        if(foundValue){// ON a trouvé le bon tuple, on doit passer au tuple gauche suivant
                            // après itération sur toutes les colonnes en commun, on a trouvé un tuple
                            // ou si il n'y a aucun élément en commun entre la tête et la queue
                            // Exemple: R1(x,y) -> R2(z,t) : veut dire que si il y a un tuple dans
                            // R1(quelconque), il y en a un dans R2 (quelconque)
                            // On passe donc au tuple gauche suivant...
                            break;
                        }
                    }
                    if (!foundValue) {
                        // Sinon, on doit renvoyer le tuple correcteur directement et donc arreter
                        // l'evaluation de l'EGD
                        // On crée le tuple droit avec full null, puis on rempli les commonColumns avec
                        // les valeur de tuple gauche
                        for (int i = 0; i < leftColumns.size(); i++) {
                            int index_right = rightTable.getColumnIndex(leftColumns.get(i));
                            correctionTuple.set(index_right, leftTuple.get(leftTable.getColumnIndex(leftColumns.get(i))));
                        }
                        return Pair.of(rightTable,correctionTuple);
                    }
                }
            }
        }
        return null;
    }

    public Pair<Table, List<Object>> isSatisfiedOblivious() throws FormatException {
        Set<Pair<Table, List<List<Object>>>> left = filterTable(getRelationalAtomsLeft());
        Set<Pair<Table, List<List<Object>>>> right = filterTable(relationalAtomsRight);

        for (Pair<Table, List<List<Object>>> leftPair : left) {// Pour chaque Relation à gauche (R1(x))
            Table leftTable = leftPair.getLeft();
            List<String> leftColumns = leftTable.getColumns();
            for (Pair<Table, List<List<Object>>> rightPair : right) {// Pour chaque relation à droite (R2(x))
                Table rightTable = rightPair.getLeft();
                List<String> rightColumns = rightTable.getColumns();
                // On vérifie si R1 et R2 ont des colonnes présentes dans la liste de
                // commonValues
                leftColumns.retainAll(commonValues);
                rightColumns.retainAll(commonValues);
                leftColumns.retainAll(rightColumns);// on stocke les colonnes en communs entre les 2 tables, mais aussi
                // à retenir dans notre dépendance

                // On doit maintenant parcours les tuples cad la partie droite de leftPair
                List<List<Object>> leftTuples = leftPair.getRight();
                List<List<Object>> rightTuples = rightPair.getRight();

                for (List<Object> leftTuple : leftTuples) {

                    //List<Object> correctionTuple = new ArrayList<>();

                    List<Object> correctionTuple = new ArrayList<>(Collections.nCopies(rightTuples.size()+1, null));

                    for (String leftColumn : leftColumns) {
                        int index_right = rightTable.getColumnIndex(leftColumn);
                        int index_light = rightTable.getColumnIndex(leftColumn);
                        correctionTuple.set(index_right, leftTuple.get(index_light));
                    }

                    return Pair.of(rightTable, correctionTuple);
                }
            }
        }
        return null;
    }
}
