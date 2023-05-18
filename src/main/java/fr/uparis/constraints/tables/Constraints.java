package fr.uparis.constraints.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Constraints {
    private final Set<String> notNullConstraints;
    private final Map<String,Object> defaultValues;
    private final Set<Set<String>> uniqueConstraints;
    private final Set<CheckConstraint> checkConstraints;
    private final Set<String> primaryKeyColumns;
    private final Set<ForeignKeyConstraint> foreignKeyConstraints;
    private final Set<String> autoIncrement;

    public Constraints(){
        notNullConstraints = new HashSet<>();
        defaultValues = new HashMap<>();
        uniqueConstraints = new HashSet<>();
        checkConstraints = new HashSet<>();
        primaryKeyColumns = new HashSet<>();
        foreignKeyConstraints = new HashSet<>();
        autoIncrement = new HashSet<>();
    }

    public Set<CheckConstraint> getCheckConstraints(){
        return new HashSet<>(checkConstraints);
    }

    public Set<String> getAutoIncrement(){
        return new HashSet<>(autoIncrement);
    }
    
    public Set<String> getNotNullConstraints(){
        return new HashSet<>(notNullConstraints);
    }

    public Map<String,Object> getDefaultValues(){
        return new HashMap<>(defaultValues);
    }
    
    public Set<String> getPrimaryKeyColumns(){
        return new HashSet<>(primaryKeyColumns);
    }

    public Set<List<String>> getUniqueConstraints(){
        Set<List<String>> result = new HashSet<>();
        for(Set<String> uniqueConstraint : uniqueConstraints){
            result.add(new ArrayList<>(uniqueConstraint));
        }
        return result;
    }
     
    public Set<ForeignKeyConstraint> getForeignKeyConstraints(){
        return new HashSet<>(foreignKeyConstraints);
    }
    
    public void addNotNullConstraint(String columnName){
        notNullConstraints.add(columnName);
    }

    public void addDefaultValue(String columnName, Object defaultValue){
        defaultValues.put(columnName, defaultValue);
    }

    public void addUniqueConstraint(List<String> columnNames){
        uniqueConstraints.add(new HashSet<>(columnNames));
    }

    public void addCheckConstraint(CheckConstraint checkConstraint){
        checkConstraints.add(checkConstraint);
    }

    public void setPrimaryKeyColumns(List<String> pKColumns){
        primaryKeyColumns.clear();
        primaryKeyColumns.addAll(new HashSet<>(pKColumns));
    }

    public void addForeignKeyConstraints(ForeignKeyConstraint foreignKeyConstraint){
        foreignKeyConstraints.add(foreignKeyConstraint);
    }

    public void addAutoIncrement(String columnName){
        autoIncrement.add(columnName);
    }
}
