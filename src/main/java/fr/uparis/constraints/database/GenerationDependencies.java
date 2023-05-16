package fr.uparis.constraints.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.algorithms.ConstantAtoms;
import fr.uparis.database.Database;
import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public abstract class GenerationDependencies {
    private final Set<Pair<Table,ConstantAtoms>> relationalAtomsLeft = new HashSet<>();
    public Set<Pair<Table, ConstantAtoms>> getRelationalAtomsLeft() {
        return relationalAtomsLeft;
    }

    public void addRelationalAtom
    (Table relationalAtom, ConstantAtoms constants,Database dBase, Set<Pair<Table,ConstantAtoms>> relationalAtoms) throws FormatException{
        if(!dBase.tableExists(relationalAtom))
            throw new FormatException
            ("Vous essayez de rajouter un atome relationnel d'une table non existante !");
        relationalAtoms.add(Pair.of(relationalAtom,constants));
    }
    
    // Set avec pair -> comme hashmap mais la clef et la valeur sont la table et sa liste de tuples
    // Chaque pair de la table correspond à une table, et une liste de tuples définis par un pair
    // qui associe à chaque colonne une valeur. Les tuples sont bien sur filtré par les constantes si elles sont définies.
    public Set<Pair<Table, List<List<Object>>>> filterTable(Set<Pair<Table,ConstantAtoms>> relationalAtoms) throws FormatException{
            Set<Pair<Table,List<List<Object>>>> filteredTables = new HashSet<>();
            // On associe à chaque table présente, la nouvelle liste de tuples après filtre
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

    public abstract boolean isSatisfied () throws FormatException;
}
