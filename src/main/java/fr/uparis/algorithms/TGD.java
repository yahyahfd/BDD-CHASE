package fr.uparis.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.database.Database;
import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public class TGD {
    private final Set<Pair<Table,ConstantAtoms>> relationalAtomsLeft = new HashSet<>();
    private final Set<Pair<Table,ConstantAtoms>> relationalAtomsRight = new HashSet<>();
    private final Set<String> commonValues = new HashSet<>();

    public void addRelationalAtomLeft(Table relationalAtom, ConstantAtoms constants,Database dBase) throws FormatException{
        if(!dBase.tableExists(relationalAtom))
            throw new FormatException
            ("Vous essayez de rajouter un atome relationnel d'une table non existante !");
        relationalAtomsLeft.add(Pair.of(relationalAtom,constants));
    }

    public void addRelationalAtomRight(Table relationalAtom, ConstantAtoms constants,Database dBase) throws FormatException{
        if(!dBase.tableExists(relationalAtom))
            throw new FormatException
            ("Vous essayez de rajouter un atome relationnel d'une table non existante !");
        relationalAtomsRight.add(Pair.of(relationalAtom,constants));
    }

    public void addCommonValue(String columnName) throws FormatException{
        commonValues.add(columnName);
    }
    
    // Set avec pair -> comme hashmap mais la clef et la valeur sont la table et sa liste de tuples
    // Chaque pair de la table correspond à une table, et une liste de tuples définis par un pair
    // qui associe à chaque colonne une valeur. Les tuples sont bien sur filtré par les constantes si elles sont définies.
    public Set<Pair<Table, List<List<Object>>>> filterTable(boolean leftRight) throws FormatException{
            Set<Pair<Table,List<List<Object>>>> filteredTables = new HashSet<>();
            // On associe à chaque table présente, la nouvelle liste de tuples après filtre
            Set<Pair<Table,ConstantAtoms>>  relationalAtoms;
            relationalAtoms = leftRight?relationalAtomsLeft:relationalAtomsRight;
        
            for(Pair<Table,ConstantAtoms> relationalAtomLeft : relationalAtoms){
                ConstantAtoms constantAtoms = relationalAtomLeft.getRight();
                Table table = relationalAtomLeft.getLeft();
                List<List<Object>> filteredTable;
                if(constantAtoms == null){ // on prend toutes la table
                    filteredTable = table.getRows();
                }else{ // on ne prend que les tuples concernés (vu qu'on utilise des constantes)
                    filteredTable = table.selectFromTable(new ArrayList<>(constantAtoms.getConstantes()));
                }
                filteredTables.add(Pair.of(table,filteredTable));
            }
            return filteredTables;
    }

    public boolean isSatisfied () throws FormatException{
        Set<Pair<Table, List<List<Object>>>> left = filterTable(true);
        Set<Pair<Table, List<List<Object>>>> right = filterTable(false);
        
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
