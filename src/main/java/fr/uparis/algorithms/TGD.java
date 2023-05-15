package fr.uparis.algorithms;

import java.util.List;

import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class TGD extends DataDependencyExpression {
    private String leftGuardCondition;
    private String rightGuardCondition;
    private Evaluator evaluator;

    public TGD(List<String> sourceAttribute, List<String> targetAttribute, String lftGuardCondition, String rgtGuardCondition) throws FormatException {
        super(sourceAttribute, targetAttribute);
        try {
            evaluator = new Evaluator();
            evaluator.parse(lftGuardCondition);
            evaluator.parse(rgtGuardCondition);
            this.leftGuardCondition = lftGuardCondition;
            this.rightGuardCondition = rgtGuardCondition;
        } catch (EvaluationException e) {
            throw new FormatException
            ("Les expressions de garde sont invalide !");
        }
    }

    @Override
    boolean isSatisfied(List<List<Object>> tupleList, Table table) throws FormatException {
        try {
            for (List<Object> tuple : tupleList) {
                for (String attribute : getLeftAttributes()) {
                    int index = table.getColumnIndex(attribute);
                    evaluator.putVariable(attribute, tuple.get(index).toString());
                }
                String leftResult = evaluator.evaluate(leftGuardCondition);
                boolean conditionSatisfiedLeft = evaluator.getBooleanResult(leftResult);
                // un tuple ne satisfait pas la condition sur la partie gauche, on passe return false
                if(!conditionSatisfiedLeft) return false; 
                
                for (String attribute : getRightAttributes()) {
                    int index = table.getColumnIndex(attribute);
                    evaluator.putVariable(attribute, tuple.get(index).toString());
                }
                String rightResult = evaluator.evaluate(rightGuardCondition);
                boolean conditionSatisfiedRight = evaluator.getBooleanResult(rightResult);
                // un tuple ne satisfait pas la condition sur la partie droite, on passe return false
                if(!conditionSatisfiedRight) return false; 
                
                
            }
            return true; // condition satisfaite pour tous les tuples
        } catch (EvaluationException e) {
            e.printStackTrace();
            return false;
        }
    }

}
