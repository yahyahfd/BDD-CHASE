package fr.uparis.algorithms;

import java.util.ArrayList;
import java.util.List;

import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public abstract class DataDependencyExpression {
    private List<String> leftAttributes;
    private List<String> rightAttributes;

    public List<String> getLeftAttributes(){
        return new ArrayList<>(leftAttributes);
    }

    public List<String> getRightAttributes(){
        return new ArrayList<>(rightAttributes);
    }
    
    public DataDependencyExpression(List<String> leftAttributes, List<String> rightAttributes) throws FormatException{
        if(leftAttributes.size() != rightAttributes.size()){
            throw new FormatException
            ("EGD : leftAttributes et rightAttributes doivent être de même taille.");
        }
        this.leftAttributes = leftAttributes;
        this.rightAttributes = rightAttributes;
    }

    abstract boolean isSatisfied(List<List<Object>> tupleList, Table table) throws FormatException;
}
