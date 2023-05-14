package fr.uparis.database;

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

    public Constraints(){
        notNullConstraints = new HashSet<>();
        defaultValues = new HashMap<>();
        uniqueConstraints = new HashSet<>();
        checkConstraints = new HashSet<>();
        primaryKeyColumns = new HashSet<>();
        foreignKeyConstraints = new HashSet<>();
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
}
