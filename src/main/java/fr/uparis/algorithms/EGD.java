package fr.uparis.algorithms;

import java.util.List;

import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public class EGD extends DataDependencyExpression {

    public EGD(List<String> leftAttributes, List<String> rightAttributes) throws FormatException{
       super(leftAttributes, rightAttributes);
    }

    @Override
    public boolean isSatisfied(List<List<Object>> tupleList, Table table) throws FormatException{
        for(List<Object> tuple : tupleList){
            if(!compareAttributes(table, tuple)){
                return false;
            }
        }
        return true;
    }

    public boolean compareAttributes(Table table, List<Object> tuple) throws FormatException{
        for(int i = 0;i<getLeftAttributes().size();i++){
            String attributeA = getLeftAttributes().get(i);
            String attributeB = getRightAttributes().get(i);
            int indexA = table.getColumnIndex(attributeA);
            int indexB = table.getColumnIndex(attributeB);
            Object valueA = tuple.get(indexA);
            Object valueB = tuple.get(indexB);
            if(!valueA.equals(valueB)){
                return false;
            }
        }
        return true;
    }
}
