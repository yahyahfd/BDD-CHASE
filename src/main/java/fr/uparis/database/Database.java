package fr.uparis.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database {

    private final String dbName;
    private final HashMap<String,Table> tables = new HashMap<>();

    public Database(String dbName){
        this.dbName = dbName;
    }

    public void createTable(Table table){
        if(tables.containsKey(table.getName()))
            throw new IllegalArgumentException("Database "+dbName
            +" : La table "+table.getName()+ "existe déjà.");
        tables.put(table.getName(), table);
    }

    public Table dropTable(String tableName){
        Table result = tables.remove(tableName);
        if(result == null){
            throw new IllegalArgumentException("Database "+dbName
            +" : La table "+tableName+ "n'existe pas.");
        }
        return result;
    }

    public Table getTable(String tableName){
        Table result = tables.get(tableName);
        if(result == null){
            throw new IllegalArgumentException("Database "+dbName
            +" : La table "+tableName+ "n'existe pas.");
        }
        return result;
    }

    public List<String> getTableNames(){
        return new ArrayList<String>(tables.keySet());
    }

    // méthode pour sauvegarder bd dans un fichier
    // méthode pour charger bd à partir d'un fichier
    // méthode de jointure tri, filtrage etc
    // c'est ici qu'on aura le chase etc
}
