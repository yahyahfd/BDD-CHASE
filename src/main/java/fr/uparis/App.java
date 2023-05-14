package fr.uparis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.database.*;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Starting the program..." );
        Database myDb = new Database("myDB");
        Table etudiants = new Table("Etudiants");
        myDb.createTable(etudiants);
        etudiants.addColumn("NumEtudiant",Integer.class);
        etudiants.addColumn("Nom",String.class);
        etudiants.addColumn("Prenom",String.class);

        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("NumEtudiant");
        etudiants.setPrimaryKeyColumns(primaryKeys);

        List<Object> etudiant1 = new ArrayList<>();
        etudiant1.add(71800578);
        etudiant1.add("Hafid");
        etudiant1.add("Yahya");
        List<Object> etudiant2 = new ArrayList<>();
        etudiant2.add(71702333);
        etudiant2.add("Meziane");
        etudiant2.add("Reda");

        try {
            etudiants.addRow(etudiant1);
            etudiants.addRow(etudiant2);
            System.out.println("Les étudiants ont été rajoutés avec succès !");

            // test du select
            List<Pair<String,?>> conditions = new ArrayList<>();
            conditions.add(Pair.of("Nom", "Hafid"));
            conditions.add(Pair.of("NumEtudiant", 71800578));


            List<List<Object>> result = etudiants.selectFromTable(conditions);
            System.out.println("Résultats du select: ");
            for(List<Object> row: result){
                System.out.println(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
