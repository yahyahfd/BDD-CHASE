package fr.uparis.algorithms;


import java.util.List;

import fr.uparis.database.Database;

public interface Chase {
    void chase(Database database, List<DataDependencyExpression> contraintes);

    default boolean isEGD(){
        return false;
    }

    default boolean isTGD(){
        return false;
    }
}
