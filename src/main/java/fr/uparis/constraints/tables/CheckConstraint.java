package fr.uparis.constraints.tables;

import fr.uparis.database.Database;
import fr.uparis.exceptions.FormatException;

public class CheckConstraint {
    /**
     * <, >, <=, >=, ==, !=, peuvent être utilisés pour comparer des valeurs.
     * && (ET) et || (OU) peuvent être utilisés pour combiner des expressions. 
     * Les parenthèses ( et ) peuvent être utilisées pour définir la priorité 
     * des opérations. Les valeurs littérales peuvent être des nombres entiers, 
     * des nombres à virgule flottante, des chaînes de caractères ou des booléens.
     * Les variables peuvent être utilisées pour représenter des valeurs 
     * dynamiques à évaluer lors de l'exécution.
     * Exemple : "nom.startsWith(\"H\")"
     */
    private final String expression;

    public CheckConstraint(String expression) throws FormatException{
        if(Database.isValidExpression(expression)){
            this.expression = expression;
        }else{
            throw new FormatException
            ("L'expression "+expression+" est invalide !");
        }
    }

    public String getExpression(){
        return expression;
    }

    // public boolean evaluateExpression(List<MutablePair<String,Object>> columnValues) throws EvaluationException{
    //     for(MutablePair<String,?> columnValue : columnValues){
    //         Database.evaluator.putVariable(columnValue.getLeft(), columnValue.getRight().toString());
    //     }
    //     String result = Database.evaluator.evaluate(expression);
    //     return Database.evaluator.getBooleanResult(result);
    // }
}
