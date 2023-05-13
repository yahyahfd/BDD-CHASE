package fr.uparis.database;

import java.util.HashMap;

public class Database {

    private final HashMap<String,Table> tables = new HashMap<>();

    public void addTable(String tableName, Table table){
        tables.put(tableName,table);
    }

    public void removeTable(String tableName){
        tables.remove(tableName);
    }

    public Table getTable(String tableName){
        return tables.get(tableName);
    }
}
