package fr.uparis.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import fr.uparis.exceptions.FormatException;
import org.apache.commons.lang3.tuple.Pair;

public class Table {

    // Nom de la table
    private String name;
    // à chaque nom de colonne, on associe un type
    private LinkedHashMap<String, Class<?>> columns = new LinkedHashMap<>();

    // Liste de rows, on pourra filter la liste comme on veut sur n'importe quels attributs
    private List<List<Object>> rows = new ArrayList<>();
    /* Les contraintes */
    private final Constraints constraints = new Constraints();
    // indexes des colonnes de la clef primaire : les colonnes qui constituent la clef
    private List<Integer> primaryKeyIndexes;


    public Table(String name) {
        this.name = name;
    }

    public void addNotNullConstraints(List<String> columnNames){
        for(String cName: columnNames){
            constraints.addNotNullConstraint(cName);
        }
    }

    public void addDefaultValues(List<Pair<String,?>> defaultvalues){
        for(Pair<String,?> pair : defaultvalues){
            constraints.addDefaultValue(pair.getLeft(), pair.getRight());
        }
    }

    public void addUniqueConstraints(List<List<String>> uniqueConstraints){
        for(List<String> uniqueConstraint: uniqueConstraints){
            constraints.addUniqueConstraint(uniqueConstraint);
        }
    }

    public void addCheckConstraints(List<CheckConstraint> checkConstraints){
        for(CheckConstraint checkConstraint: checkConstraints){
            constraints.addCheckConstraint(checkConstraint);
        }
    }

    public void setPrimaryKeyColumns(List<String> pKColumns){
        if(!(pKColumns.size()<= columns.size()) 
        || !(columns.keySet().containsAll(pKColumns))){
            throw new IllegalArgumentException
            ("Table "+name+" : Une clef primaire doit-être une partie de (ou toute) la liste d'attributs");
        }
        ArrayList<Integer> pkIndexes = new ArrayList<>();
        for(String key: pKColumns){
            pkIndexes.add(getColumnIndex(key));
        }
        this.primaryKeyIndexes = pkIndexes;
        constraints.setPrimaryKeyColumns(pKColumns);
    }

    public void addForeignKeyConstraints(List<ForeignKeyConstraint> foreignKeyConstraints, Database dBase){
        for(ForeignKeyConstraint foreignKeyConstraint: foreignKeyConstraints){
            if(!dBase.tableExists(foreignKeyConstraint.getReferencedTableName())){
                throw new IllegalArgumentException
                ("Table "+name+" : Une clef étrangère doit référencer une table existante.");
            }
            constraints.addForeignKeyConstraints(foreignKeyConstraint);
        }
    }

    public void addColumn(String nomColonne, Class<?> typeColonne){
        if(columns.containsKey(nomColonne)){
            throw new IllegalArgumentException
            ("Table "+name+" : La colonne "+nomColonne+" existe déjà.");
        }
        this.columns.put(nomColonne,typeColonne);
    }

    public String getName() {
        return name;
    }

    // on garde pour l'instant, mais on devrait mettre final au name et retirer ce setter
    public void renameTable(String name){
        this.name = name;
    }

    private boolean deleteRow(List<Object> rowToDelete){
        return rows.remove(rowToDelete);
    }

    public void deleteRows(List<List<Object>> rowsToDelete){
        for(List<Object> rowToDelete : rowsToDelete){
            if(!deleteRow(rowToDelete)){
                throw new IllegalArgumentException
                    ("Table "+name+" : Vous essatez de supprimer un tuple qui n'existe pas.");
            }
        }
    }

    private int getColumnIndex(String columnName){
        int index = 0;
        for(String column : columns.keySet()){
            if(column.equals(columnName)){
                return index;
            }
            index++;
        }
        throw new IllegalArgumentException
            ("Table "+name+" : La colonne "+columnName+" n'existe pas.");
    }

    // nombre de tuples de ma table
    public int getRowCount(){
        return rows.size();
    }

    // Liste des attributs
    public List<String> getColumns(){
        return new ArrayList<>(columns.keySet());
    }

    // retourne une nouvelle instance pour éviter de les pb de modifications
    public List<List<Object>> getRows(){
        return new ArrayList<>(rows);
    }

    private boolean rowDoublon(List<Object> rowValues){
        for(List<Object> row :rows){
            boolean doublon = true;
            for(int i : primaryKeyIndexes){
                if(!rowValues.get(i).equals(row.get(i))){// c'est bon, on passe au row suivant
                    doublon = false;
                    break;
                }
            }
            if(doublon) return doublon;
        }
        return false;
    }

    // rajoute un tuple à notre table s'il ne partage pas les mêmes clefs primaires
    // qu'un tuple existant, etc...
    public void addRow(List<Object> rowValues) throws FormatException {
        if(rowDoublon(rowValues)){
            throw new IllegalArgumentException
            ("Table "+name
            +" : Le tuple que vous souhaitez rajouter partage les mêmes valeurs de clef primaire d'un autre tuple existant");
        }
        
        if (rowValues.size() != columns.size()){
            throw new FormatException
            ("Table "+name+" : Le nombre de valeur ne correspond pas au nombre de colonnes.");
        }

        List<Class<?>> columnTypes = new ArrayList<>(columns.values());
        for(int i = 0;i<columnTypes.size();i++){
            Object value = rowValues.get(i);
            Class<?> type = columnTypes.get(i);
            if(!value.getClass().equals(type)){
                throw new FormatException
                ("Table "+name+" : L'objet "+value+" est de type "
                +value.getClass()+" alors que le type "
                +type+ " était attendu à la "+i+"ème colonne");
            }
        }
        this.rows.add(new ArrayList<>(rowValues));
    }

    // prend en argument une liste de paires: à gauche le nom de la colonne,
    // à droite la valeur qui nous interesse
    // retourne la liste des rows qui correspondent à nos critères dans le select
    // cette méthode correspond au select
    public List<List<Object>> selectFromTable(List<Pair<String,?>> values){
        ArrayList<List<Object>> result = new ArrayList<>(rows);
        Iterator<List<Object>> iterator = result.iterator();
        while(iterator.hasNext()){
            List<Object> row = iterator.next();
            for(Pair<String,?> pair : values){
                int columnIndex = getColumnIndex(pair.getLeft());
                Object expectedValue = pair.getRight();
                Object actualValue = row.get(columnIndex);
                if (!expectedValue.equals(actualValue)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return result;
    }

}
