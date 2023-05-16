package fr.uparis.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.uparis.algorithms.EGD;
import fr.uparis.exceptions.FormatException;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class Database {
    private final List<EGD> egdList = new ArrayList<>();
    public List<EGD> getEgd() {
        return new ArrayList<>(egdList);
    }

    public void addEGD(EGD egd){
        egdList.add(egd);
    }

    private final String dbName;
    private final Set<Table> tables = new HashSet<>();
    public static final Evaluator evaluator = new Evaluator();

    public static boolean evaluateExpression(String expression) throws EvaluationException  {
        String result = evaluator.evaluate(expression);
        return evaluator.getBooleanResult(result);
    }
    
    public static boolean isValidExpression(String expression){
        try {
            Database.evaluator.parse(expression);
        } catch (EvaluationException e) {
            return false;
        }

        return true;
    }
    
    public Database(String dbName){
        this.dbName = dbName;
    }

    public List<Table> getTables(){
        return new ArrayList<>(tables);
    }

    public void createTable(Table table) throws FormatException{
        if(tableExists(table))
            throw new FormatException("Database "+dbName
            +" : La table "+table.getName()+ "existe déjà.");
        tables.add(table);
    }

    public Table dropTable(Table table) throws FormatException{
        if(!tables.remove(table)){
            throw new FormatException("Database "+dbName
            +" : La table "+table.getName()+ "n'existe pas.");
        }
        return table;
    }

    public Table getTable(String tableName) throws FormatException{
        Table result = null;
        for(Table table: tables){
            if(table.getName().equals(tableName)){
                result = table;
                break;
            }
        }
        if(result == null){
            throw new FormatException("Database "+dbName
            +" : La table "+tableName+ "n'existe pas.");
        }
        return result;
    }

    public List<String> getTableNames(){
        List<String> tableNames = new ArrayList<>();
        for(Table table: tables){
            tableNames.add(table.getName());
        }
        return tableNames;
    }

    public boolean tableExists(Table table){
        return tables.contains(table);
    }

    // méthode pour sauvegarder bd dans un fichier
    // méthode pour charger bd à partir d'un fichier
    // méthode de jointure tri, filtrage etc
    // c'est ici qu'on aura le chase etc

    // public boolean satisfiesConstraints() {
    //     return false;
    // }
}
