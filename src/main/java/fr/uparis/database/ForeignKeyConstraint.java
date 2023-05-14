package fr.uparis.database;

public class ForeignKeyConstraint {
    /**
     * Externe
     */
    private final String referencedColumn;
    private final String referencedTableName;
    /**
     * Interne
     */
    private final String referencingColumn;

    public ForeignKeyConstraint(String referencedColumn, String referencedTableName, String referencingColumn) {
        this.referencedColumn = referencedColumn;
        this.referencedTableName = referencedTableName;
        this.referencingColumn = referencingColumn;
    }
    
    /**
     * Externe
     * @return
     */
    public String getReferencedColumn() {
        return referencedColumn;
    }

    /**
     * Interne
     * @return
     */
    public String getReferencingColumn() {
        return referencingColumn;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }
}
