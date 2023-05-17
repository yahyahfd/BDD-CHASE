package fr.uparis.constraints.tables;

import fr.uparis.database.Table;

public class ForeignKeyConstraint {
    /**
     * Externe
     */
    private final String referencedColumn;
    private final Table referencedTable;
    /**
     * Interne
     */
    private final String referencingColumn;

    public ForeignKeyConstraint(String referencedColumn, Table referencedTable, String referencingColumn) {
        this.referencedColumn = referencedColumn;
        this.referencedTable = referencedTable;
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

    public Table getReferencedTable() {
        return referencedTable;
    }
}
