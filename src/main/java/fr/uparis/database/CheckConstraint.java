package fr.uparis.database;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

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
    private final Evaluator evaluator;

    public CheckConstraint(String expression){
        try {
            evaluator = new Evaluator();
            evaluator.parse(expression);
            this.expression = expression;
        } catch (EvaluationException e) {
            throw new IllegalArgumentException
            ("L'expression "+expression+" est invalide !",e);
        }
    }

    public String getExpression(){
        return expression;
    }

    public boolean evaluateExpression(List<Pair<String,?>> columnValues) throws EvaluationException{
        for(Pair<String,?> columnValue : columnValues){
            evaluator.putVariable(columnValue.getLeft(), columnValue.getRight().toString());
        }
        String result = evaluator.evaluate(expression);
        return evaluator.getBooleanResult(result);
    }
}
