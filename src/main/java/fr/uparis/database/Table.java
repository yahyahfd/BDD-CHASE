package fr.uparis.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import fr.uparis.exceptions.FormatException;
import org.apache.commons.lang3.tuple.Pair;

public class Table {

    // Nom de la table
    private String name;
    // à chaque nom de colonne, on associe un type
    private Map<String, Class<?>> columns;

    // Liste de rows, on pourra filter la liste comme on veut sur n'importe quels attributs
    private List<List<Object>> rows;

    public Table(String name, Map<String, Class<?>> columns) {
        this.name = name;
        this.columns = new LinkedHashMap<>(columns);
        this.rows = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void insertRow(List<Object> rowValues) throws FormatException {
        // code d'une hashmap que j'avais rajouté.
        // Pour vérifier si c'est le meme objet, on se basera sur la clef primaire de la table.
        // if(rows.containsKey(rowValues)){
        //     throw new FormatException
        //     ("Table "+name+" : L'objet "+object+" existe déjà dans la table.");
        // }
        
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
    public List<List<Object>> selectFromTable(List<Pair<String,String>> values){
        ArrayList<List<Object>> result = new ArrayList<>(rows);
        Iterator<List<Object>> iterator = result.iterator();
        while(iterator.hasNext()){
            List<Object> row = iterator.next();
            for(Pair<String,String> pair : values){
                int columnIndex = getColumnIndex(pair.getLeft());
                if(!pair.getRight().equals(row.get(columnIndex))){
                    iterator.remove();
                    break;
                }
            }
        }
        return result;
    }

    private int getColumnIndex(String columnName){
        int index = 0;
        for(String column : columns.keySet()){
            if(column.equals(columnName)){
                return index;
            }
            index++;
        }
        return -1;
    }
}
