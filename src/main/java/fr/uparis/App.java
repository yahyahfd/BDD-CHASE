package fr.uparis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.algorithms.ConstantAtoms;
import fr.uparis.algorithms.EqualityAtom;
import fr.uparis.constraints.database.EGD;
import fr.uparis.constraints.database.GenerationDependencies;
import fr.uparis.constraints.database.TGD;
import fr.uparis.database.*;
import fr.uparis.exceptions.FormatException;

public class App 
{
    public static void main( String[] args ) throws FormatException
    {
        System.out.println( "Starting the program..." );        
        Database myDb = new Database("myDB");
        // Ajout des tables
        Table masters = new Table("Masters");
        myDb.createTable(masters);
        masters.addColumn("ID",Integer.class);
        masters.addColumn("NomMaster",String.class);
        List<String> primaryKeysMaster = new ArrayList<>();
        primaryKeysMaster.add("ID");
        masters.setPrimaryKeyColumns(primaryKeysMaster);
        Table etudiants = new Table("Etudiants");
        myDb.createTable(etudiants);
        etudiants.addColumn("NumEtudiant",Integer.class);
        etudiants.addColumn("Nom",String.class);
        etudiants.addColumn("Prenom",String.class);
        etudiants.addColumn("NomMaster",String.class);
        Pair<String,String> nomMasterDefault = Pair.of("NomMaster","IMPAIR");
        List<Pair<String,?>> defaultList = new ArrayList<>();
        defaultList.add(nomMasterDefault);
        etudiants.addDefaultValues(defaultList);
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("NumEtudiant");
        etudiants.setPrimaryKeyColumns(primaryKeys);
        // ajout des EGD
        EGD firstEGD = new EGD();
        firstEGD.addRelationalAtomLeft(etudiants, null, myDb);
        EqualityAtom eAtomA = new EqualityAtom("NomMaster", etudiants, false);
        EqualityAtom eAtomB = new EqualityAtom("IMPAIR", etudiants, true);
        EqualityAtom eAtomC = new EqualityAtom("IMPAIR", etudiants, true);
        firstEGD.addEqualityAtomRight(Pair.of(eAtomA,eAtomC));
        firstEGD.addEqualityAtomLeft(Pair.of(eAtomA,eAtomB));
        myDb.addGenerationDependency(firstEGD);

        // ajout des TGD
        TGD firstTGD = new TGD();
        ConstantAtoms cAtom = new ConstantAtoms();
        cAtom.addConstante(Pair.of("NomMaster","IMPAIR"));
        firstTGD.addRelationalAtomLeft(etudiants, cAtom, myDb);
        firstTGD.addRelationalAtomRight(masters, cAtom, myDb);
        firstTGD.addCommonValue("NomMaster");
        myDb.addGenerationDependency(firstTGD);

        // ajout des tuples
        List<MutablePair<String, Object>> etudiant1 = new ArrayList<>();
        MutablePair<String,Object> numEtudiant1 = MutablePair.of("NumEtudiant",(Object)71800578);
        MutablePair<String,Object> nomEtudiant1 = MutablePair.of("Nom",(Object)"Hafid");
        MutablePair<String,Object> prenomEtudiant1 = MutablePair.of("Prenom",(Object)"Yahya");
        etudiant1.add(numEtudiant1);
        etudiant1.add(nomEtudiant1);
        etudiant1.add(prenomEtudiant1);
        List<MutablePair<String,Object>> etudiant2 = new ArrayList<>();
        MutablePair<String,Object> numEtudiant2 = MutablePair.of("NumEtudiant",(Object)71702333);
        MutablePair<String,Object> nomEtudiant2 = MutablePair.of("Nom",(Object)"Meziane");
        MutablePair<String,Object> prenomEtudiant2 = MutablePair.of("Prenom",(Object)"Reda");
        etudiant2.add(numEtudiant2);
        etudiant2.add(nomEtudiant2);
        etudiant2.add(prenomEtudiant2);

        List<MutablePair<String, Object>> master1 = new ArrayList<>();
        MutablePair<String,Object> idMaster1 = MutablePair.of("ID",(Object)0);
        MutablePair<String,Object> nomMaster1 = MutablePair.of("NomMaster",(Object)"IMPAIR");
        master1.add(idMaster1);
        master1.add(nomMaster1);


        try {
            etudiants.addRow(etudiant1,myDb);
            etudiants.addRow(etudiant2,myDb);
            System.out.println("Les étudiants ont été rajoutés avec succès !");
            masters.addRow(master1, myDb);
            System.out.println("Les masters ont été rajoutés avec succès !");

            // test du select
            List<Pair<String,?>> conditions = new ArrayList<>();
            conditions.add(Pair.of("Nom", "Hafid"));
            conditions.add(Pair.of("NumEtudiant", 71800578));


            List<List<Object>> result = etudiants.selectFromTable(conditions);
            System.out.println("Résultats du select: ");
            for(List<Object> row: result){
                System.out.println(row);
            }
            System.out.println(Database.evaluator.getVariables());

            for(int i = 0; i <myDb.getGenerationDependencies().size();i++){
                GenerationDependencies generationDependency = myDb.getGenerationDependencies().get(i);
                if(generationDependency instanceof EGD){
                    System.out.println("EGD_"+i+" :"+generationDependency.isSatisfied());
                }
                if(generationDependency instanceof TGD){
                    System.out.println("TGD_"+i+" :"+generationDependency.isSatisfied());
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
