package fr.uparis.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import fr.uparis.exceptions.FormatException;
import net.sourceforge.jeval.EvaluationException;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Table {

    // Nom de la table
    private String name;
    // à chaque nom de colonne, on associe un type
    private LinkedHashMap<String, Class<?>> columns = new LinkedHashMap<>();

    // Liste de rows, on pourra filter la liste comme on veut sur n'importe quels attributs
    private final List<List<Object>> rows = new ArrayList<>();
    /* Les contraintes */
    private final Constraints constraints = new Constraints();
    // indexes des colonnes de la clef primaire : les colonnes qui constituent la clef
    private final List<Integer> primaryKeyIndexes = new ArrayList<>();


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
        this.primaryKeyIndexes.clear();
        this.primaryKeyIndexes.addAll(pkIndexes);
        constraints.setPrimaryKeyColumns(pKColumns);
    }

    // On rajoute une liste de contrainte : une seule foreignKey à la fois
    public void addForeignKeyConstraints(List<ForeignKeyConstraint> foreignKeyConstraints, Database dBase){
        List<String> referencedColumns = new ArrayList<>();
        String referencedTableName = "";
        for(ForeignKeyConstraint foreignKeyConstraint: foreignKeyConstraints){
            if(!dBase.tableExists(foreignKeyConstraint.getReferencedTableName())){
                throw new IllegalArgumentException
                ("Table "+name+" : Une clef étrangère doit référencer une table existante.");
            }
            if(referencedTableName.isEmpty())
                referencedTableName = foreignKeyConstraint.getReferencedTableName();
            if(!referencedTableName.equals(foreignKeyConstraint.getReferencedTableName()))
                throw new IllegalArgumentException
                ("Table "+name+" : Vous essayez de rajouter une contrainte sur différentes tables pour un même attribut.");
                
            referencedColumns.add(foreignKeyConstraint.getReferencedColumn());
            constraints.addForeignKeyConstraints(foreignKeyConstraint);
        }
        List<String> primaryKeys = new ArrayList<>(constraints.getPrimaryKeyColumns());
        boolean notFound = true;
        if(referencedColumns.containsAll(primaryKeys) && primaryKeys.containsAll(referencedColumns)){
            notFound = false;
        }
        if(notFound){
            List<List<String>> uniqueConstraints = new ArrayList<>(constraints.getUniqueConstraints());
            for(List<String> uniqueConstraint :uniqueConstraints){
                if(referencedColumns.containsAll(uniqueConstraint) && primaryKeys.containsAll(uniqueConstraint)){
                    notFound = false;
                }
            }
        }
        if(notFound){
            throw new IllegalArgumentException
                ("Table "+name+" : Une clef étrangère doit être une clef unique ou une clef primaire.");
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
    
    // renvoie true si 100% similaire, false sinon
    private boolean rowDoublon(List<Object> rowValues, List<String> columnNames){
        for(List<Object> row :rows){
            boolean doublon = true;
            for(String columnName: columnNames){
                int indexColumn = getColumnIndex(columnName);
                if(!rowValues.get(indexColumn).equals(row.get(indexColumn))){// c'est bon, on passe au row suivant
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
    public void addRow(List<MutablePair<String,Object>> columnValues, Database dBase) throws FormatException, EvaluationException {
        List<Object> resultValues = new ArrayList<>(Collections.nCopies(columns.size(), null));
        List<Object> rowValues = new ArrayList<>();
        for(Pair<String,Object> pair : columnValues){
            rowValues.add(pair.getRight());
        }
        List<Class<?>> columnTypes = new ArrayList<>(columns.values());
        List<String> notNull = new ArrayList<>(constraints.getNotNullConstraints());
        LinkedHashMap<String,Object> defaultValues = new LinkedHashMap<>(constraints.getDefaultValues());
        List<List<String>> uniqueConstraints = new ArrayList<>(constraints.getUniqueConstraints());
        List<ForeignKeyConstraint> foreignKeyConstraints = new ArrayList<>(constraints.getForeignKeyConstraints());
        // Contrainte PRIMARY KEY
        if(rowDoublon(rowValues, new ArrayList<>(constraints.getPrimaryKeyColumns()))){
            throw new IllegalArgumentException
            ("Table "+name
            +" : Le tuple que vous souhaitez rajouter partage les mêmes valeurs de clef primaire d'un autre tuple existant");
        }
        if (rowValues.size()>columns.size()){
            throw new FormatException
            ("Table "+name+" : Le nombre de couples colonne-valeur doit-être inférieur ou égal au nombre de colonnes.");
        }
        List<String> emptyValues = new ArrayList<>(getColumns());
        for(int i = 0; i<columnValues.size();i++){
            String columnName = columnValues.get(i).getLeft();
            Object value = columnValues.get(i).getRight();
            //verification du type en premier
            int index = getColumnIndex(columnName);
            Class<?> type = columnTypes.get(index);
            if(!value.getClass().equals(type)){
                throw new FormatException
                ("Table "+name+" : L'objet "+value+" est de type "
                +value.getClass()+" alors que le type "
                +type+ " était attendu à la colonne "+columnName);
            }
            emptyValues.remove(columnName);
            // Contrainte FOREIGN KEY
            for(ForeignKeyConstraint foreignKeyConstraint : foreignKeyConstraints){
                if(foreignKeyConstraint.getReferencingColumn().equals(columnName)){
                    Table foreignTable = dBase.getTable(foreignKeyConstraint.getReferencedTableName());
                    if(!foreignTable.columnContainsValue(value,foreignKeyConstraint.getReferencedColumn())){
                        throw new FormatException
                        ("Table "+name+" : L'objet "+value+" n'existe pas dans la table "+foreignTable.getName());
                    }
                }
            }
            resultValues.set(index, value);
        }

        for(int i = 0; i<resultValues.size();i++){
            if(resultValues.get(i) == null){
                String columnName = getColumns().get(i);
                //Contrainte DEFAULT
                resultValues.set(i,defaultValues.get(columnName));

                // contrainte NOT NULL
                if(notNull.contains(columnName)){
                    throw new FormatException
                    ("Table "+name+" : "+columnName+" ne doit pas être null.");
                }
            }
        }
        // Contrainte UNIQUE
        for(List<String> uniqueConstraint : uniqueConstraints){
            if(rowDoublon(rowValues, uniqueConstraint)){
                throw new IllegalArgumentException
                ("Table "+name
                +" : Le tuple que vous souhaitez rajouter partage les mêmes valeurs de clef primaire d'un autre tuple existant");
            }
        }

        // Contrainte CHECK
        if(!evaluateBeforeAdding(columnValues)){
            throw new IllegalArgumentException
            ("Table "+name
            +" : Le tuple que vous souhaitez rajouter ne respecte pas une contrainte CHECK");
        }
        this.rows.add(new ArrayList<>(resultValues));
    }

    private boolean evaluateBeforeAdding(List<MutablePair<String,Object>> columnValues) throws EvaluationException {
        for(CheckConstraint checkConstraint: constraints.getCheckConstraints()){
            if(!checkConstraint.evaluateExpression(columnValues)){
                return false;
            }
        }
        return true;
    }

    private boolean columnContainsValue(Object value, String columnName){
        int index = getColumnIndex(columnName);
        for(List<Object>row :rows){
            if(row.get(index).equals(value)){
                return true;
            }
        }
        return false;
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
