package fr.uparis.constraints.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.algorithms.ConstantAtoms;
import fr.uparis.database.Database;
import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public class TGD extends GenerationDependencies {
    private final Set<Pair<Table,ConstantAtoms>> relationalAtomsRight = new HashSet<>();
    private final Set<String> commonValues = new HashSet<>();

    public void addRelationalAtomLeft(Table relationalAtom, ConstantAtoms constants,Database dBase) throws FormatException{
        this.addRelationalAtom(relationalAtom,constants,dBase,getRelationalAtomsLeft());
    }

    public void addRelationalAtomRight(Table relationalAtom, ConstantAtoms constants,Database dBase) throws FormatException{
        this.addRelationalAtom(relationalAtom,constants,dBase,relationalAtomsRight);
    }

    public void addCommonValue(String columnName) throws FormatException{
        commonValues.add(columnName);
    }

    @Override
    public boolean isSatisfied () throws FormatException{
        Set<Pair<Table, List<List<Object>>>> left = filterTable(getRelationalAtomsLeft());
        Set<Pair<Table, List<List<Object>>>> right = filterTable(relationalAtomsRight);
        
        for(Pair<Table,List<List<Object>>> leftPair : left){// Pour chaque Relation à gauche (R1(x))
            Table leftTable = leftPair.getLeft();
            List<String> leftColumns = leftTable.getColumns();
            for(Pair<Table,List<List<Object>>> rightPair : right){// Pour chaque relation à droite (R2(x))
                Table rightTable = rightPair.getLeft();
                List<String> rightColumns = rightTable.getColumns();
                // On vérifie si R1 et R2 ont des colonnes présentes dans la liste de commonValues
                leftColumns.retainAll(commonValues);
                rightColumns.retainAll(commonValues);
                leftColumns.retainAll(rightColumns);// on stocke les colonnes en communs entre les 2 tables, mais aussi à retenir dans notre dépendance
                for(String columnName: leftColumns){
                    int indexLeft = leftTable.getColumnIndex(columnName);
                    int indexRight = rightTable.getColumnIndex(columnName);
                    // On doit maintenant parcours les tuples cad la partie droite de leftPair et rightPair
                    List<List<Object>> leftTuples = leftPair.getRight();
                    List<List<Object>> rightTuples = rightPair.getRight();
                    // On parcourt les Tuples des 2 côtés
                    for(List<Object> leftTuple : leftTuples){
                        if(rightTuples.size() == 0) return false;
                        for(List<Object> rightTuple: rightTuples){
                            // On renvoie false si les 2 valeurs censées être égales ne le sont pas
                            if(!leftTuple.get(indexLeft).equals(rightTuple.get(indexRight))){
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
