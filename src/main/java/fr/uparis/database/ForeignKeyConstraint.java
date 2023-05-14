package fr.uparis.database;

public class ForeignKeyConstraint {
    private final String columnName;
    private final String referencedTableName;
    private final String referencedTableColumn;

    public ForeignKeyConstraint(String columnName, String referencedTableName, String referencedTableColumn) {
        this.columnName = columnName;
        this.referencedTableName = referencedTableName;
        this.referencedTableColumn = referencedTableColumn;
    }
    
    public String getColumnName() {
        return columnName;
    }

    public String getReferencedTableColumn() {
        return referencedTableColumn;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }
}
