package fr.uparis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import fr.uparis.algorithms.Oblivious;
import fr.uparis.algorithms.Skolem;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.opencsv.CSVWriter;
import fr.uparis.algorithms.Standard;
import fr.uparis.constraints.database.EGD;
import fr.uparis.constraints.database.EqualityAtom;
import fr.uparis.constraints.database.GenerationDependencies;
import fr.uparis.constraints.database.TGD;
import fr.uparis.database.*;
import fr.uparis.exceptions.FormatException;
import fr.uparis.exceptions.InvalidConditionException;
import fr.uparis.exceptions.TupleNotFoundException;
import net.sourceforge.jeval.EvaluationException;

public class App 
{

    public static void main( String[] args ) throws FormatException, IOException, InvalidConditionException, TupleNotFoundException
    {
        System.out.println( "Starting the program..." );        
        Database myDb = new Database("myDB");
        //Ajout des tables
        Table emprunts = new Table("Emprunts");
        myDb.createTable(emprunts);
        emprunts.addColumn("idLivre",Integer.class);
        emprunts.addColumn("NumEtudiant",Integer.class);
        emprunts.addColumn("Nom",String.class);
        List<String> primaryKeysEmprunts = new ArrayList<>();
        primaryKeysEmprunts.add("idLivre");
        emprunts.setPrimaryKeyColumns(primaryKeysEmprunts);
        Table masters = new Table("Masters");
        myDb.createTable(masters);
        masters.addColumn("idMaster",Integer.class);
        masters.addColumn("NomMaster",String.class);
        List<String> primaryKeysMaster = new ArrayList<>();
        primaryKeysMaster.add("idMaster");
        List<String> autoIncrement = new ArrayList<>();
        autoIncrement.add("idMaster");
        masters.addAutoIncrement(autoIncrement);
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
        // Si dans ma table etudiants, j'ai un master IMPAIR -> j'ai IMPAIR à droite (renvoie null car tuple non trouvé)
        EGD firstEGD = new EGD();
        firstEGD.addRelationalAtomLeft(etudiants, null, myDb);
        EqualityAtom eAtomA = new EqualityAtom("NomMaster", etudiants, false);
        EqualityAtom eAtomB = new EqualityAtom("IMPAIR", etudiants, true);
        EqualityAtom eAtomC = new EqualityAtom("IMPAIR", masters, true);
        firstEGD.addEqualityAtomLeft(Pair.of(eAtomA,eAtomB));
        firstEGD.addEqualityAtomRight(Pair.of(eAtomA,eAtomC));
        myDb.addGenerationDependency(firstEGD);

        // Si dans emprunts j'ai un num étudiant -> je dois avoir un eleve avec ce num étudiant dans etudiants
        EGD secondEGD = new EGD();
        secondEGD.addRelationalAtomLeft(emprunts, null, myDb);
        secondEGD.addRelationalAtomLeft(etudiants, null, myDb);
        EqualityAtom eAtomD = new EqualityAtom("NumEtudiant", emprunts, false);
        EqualityAtom eAtomE = new EqualityAtom("NumEtudiant", etudiants, false);
        secondEGD.addEqualityAtomRight(Pair.of(eAtomD,eAtomE));
        myDb.addGenerationDependency(secondEGD);

        // Si dans emprunts j'ai un num étudiant et que cet étudiant est présent dans la liste des élèves
        //  -> je dois avoir cet élève avec le même nom
        EGD thirdEgd = new EGD();
        thirdEgd.addRelationalAtomLeft(emprunts, null, myDb);
        thirdEgd.addRelationalAtomLeft(etudiants, null, myDb);
        EqualityAtom eAtomF = new EqualityAtom("NumEtudiant", emprunts, false);
        EqualityAtom eAtomG = new EqualityAtom("NumEtudiant", etudiants, false);
        EqualityAtom eAtomI = new EqualityAtom("Nom", emprunts, false);
        EqualityAtom eAtomH = new EqualityAtom("Nom", etudiants, false);
        thirdEgd.addEqualityAtomLeft(Pair.of(eAtomF,eAtomG));
        thirdEgd.addEqualityAtomRight(Pair.of(eAtomH,eAtomI));
        myDb.addGenerationDependency(thirdEgd);

        // même EGD que la précédente, mais qui doit renvoyer tout le temps false -> n'a pas de sens
        // EGD trdEDG = new EGD();
        // ConstantAtoms cAtom = new ConstantAtoms();
        // cAtom.addConstante(Pair.of("NomMaster","IMPAIR"));
        // trdEDG.addRelationalAtomLeft(etudiants, cAtom, myDb);
        // EqualityAtom eAtomJ = new EqualityAtom(2, etudiants, true);
        // EqualityAtom eAtomK = new EqualityAtom(1, etudiants, true);
        // trdEDG.addEqualityAtomRight(Pair.of(eAtomJ,eAtomK));
        // myDb.addGenerationDependency(trdEDG);

        // ajout des TGD
        // Pour chaque étudiant, s'il est dans un Master, le master doit exister dans la table Masters
        TGD firstTGD = new TGD();
        firstTGD.addRelationalAtomLeft(etudiants, null, myDb);
        firstTGD.addRelationalAtomRight(masters, null, myDb);
        firstTGD.addCommonValue("NomMaster");
        myDb.addGenerationDependency(firstTGD);

        Scanner scanner = new Scanner(System.in);
        Integer choice = -1;
        while(choice.equals(-1)){
            System.out.println("Veuillez choisir une option :");
            System.out.println("1) Remplissage manuel des tables");
            System.out.println("2) Parser le contenu des tables à partir des CSVs");
            try {
                choice = scanner.nextInt();
                if (choice.equals(1)) {
                    System.out.println("Vous avez choisi de remplir manuellement les tables.");
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
                    List<MutablePair<String,Object>> etudiant3 = new ArrayList<>();
                    MutablePair<String,Object> numEtudiant3 = MutablePair.of("NumEtudiant",(Object)999999);
                    MutablePair<String,Object> nomEtudiant3 = MutablePair.of("Nom",(Object)"TestNom");
                    MutablePair<String,Object> prenomEtudiant3 = MutablePair.of("Prenom",(Object)"TestPrenom");
                    MutablePair<String,Object> masterEtudiant3 = MutablePair.of("NomMaster",(Object)"LP");
                    etudiant3.add(numEtudiant3);
                    etudiant3.add(nomEtudiant3);
                    etudiant3.add(prenomEtudiant3);
                    etudiant3.add(masterEtudiant3);

                    List<MutablePair<String, Object>> master1 = new ArrayList<>();
                    MutablePair<String,Object> idMaster1 = MutablePair.of("idMaster",(Object)1);
                    MutablePair<String,Object> nomMaster1 = MutablePair.of("NomMaster",(Object)"IMPAIR");
                    master1.add(idMaster1);
                    master1.add(nomMaster1);

                    List<MutablePair<String, Object>> emprunt1 = new ArrayList<>();
                    MutablePair<String,Object> idLivre1 = MutablePair.of("idLivre",(Object)0);
                    MutablePair<String,Object> numEtudiantEmprunt1 = MutablePair.of("NumEtudiant",(Object)71800578);
                    MutablePair<String,Object> nomEtudiantEmprunt1 = MutablePair.of("Nom",(Object)"Hafd");
                    emprunt1.add(idLivre1);
                    emprunt1.add(numEtudiantEmprunt1);
                    emprunt1.add(nomEtudiantEmprunt1);
                    List<MutablePair<String, Object>> emprunt2 = new ArrayList<>();
                    MutablePair<String,Object> idLivre2 = MutablePair.of("idLivre",(Object)1);
                    MutablePair<String,Object> numEtudiantEmprunt2 = MutablePair.of("NumEtudiant",(Object)71702333);
                    MutablePair<String,Object> nomEtudiantEmprunt2 = MutablePair.of("Nom",(Object)"Meziane");
                    emprunt2.add(idLivre2);
                    emprunt2.add(numEtudiantEmprunt2);
                    emprunt2.add(nomEtudiantEmprunt2);

                    etudiants.addRow(etudiant1,myDb);
                    etudiants.addRow(etudiant2,myDb);
                    etudiants.addRow(etudiant3,myDb);
                    System.out.println("Les étudiants ont été rajoutés avec succès !");
                    masters.addRow(master1, myDb);
                    System.out.println("Les masters ont été rajoutés avec succès !");
                    emprunts.addRow(emprunt1,myDb);
                    emprunts.addRow(emprunt2,myDb);
                    System.out.println("Les emprunts ont été rajoutés avec succès !");
                } else if (choice.equals(2)) {
                    System.out.println("Vous avez choisi de parser les fichiers CSV pour remplir les tables.");
                    parseCSV(emprunts, myDb, "Emprunts.csv");
                    parseCSV(etudiants, myDb, "Etudiants.csv");
                    parseCSV(masters, myDb, "Masters.csv");
                } else {
                    System.out.println("Choix invalide. Veuillez choisir une option valide.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Vous devez rentrer un entier. Réessayez!");
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // test du select
        List<Pair<String,?>> conditions = new ArrayList<>();
        conditions.add(Pair.of("Nom", "Hafid"));
        conditions.add(Pair.of("NumEtudiant", 71800578));

        List<List<Object>> result = etudiants.selectFromTable(conditions);
        System.out.println("Résultats du select: ");
        for(List<Object> row: result){
            System.out.println(row);
        }
        
        printGenerationDependencies(myDb);
        //Standard.chase(myDb);
        //Oblivious.obliviousChase(myDb, 5000);
        Skolem.obliviousSkolemChase(myDb);
        exportTableCSV(emprunts,"Emprunts_modifié.csv");
        exportTableCSV(etudiants,"Etudiants_modifié.csv");
        exportTableCSV(masters,"Masters_modifié.csv");
        printGenerationDependencies(myDb);
        scanner.close();
    }

    private static void exportTableCSV(Table table, String filePath) throws IOException{
        List<String> columnNames = table.getColumns();
        List<String> types = table.getTypes();
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath),';','\0','\\',"\n")) {
            
            writer.writeNext(columnNames.toArray(new String[0]));
            writer.writeNext(types.toArray(new String[0]));
            for(List<Object> row :table.getRows()){
                writer.writeNext(row.stream().map(Object::toString).toArray(String[]::new));
            }
            System.out.println("Export des données vers le fichier CSV réussi.");
        }catch(Exception e){
            System.err.println("Erreur lors de l'export des données vers le fichier CSV : " + e.getMessage());
        }
    }

    private static void parseCSV(Table table,Database dBase, String csvFilePath) throws IOException, FormatException, EvaluationException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String headerLine = reader.readLine();
            if (headerLine != null) {
                String[] columns = headerLine.split(";");
                List<String> columnNames = Arrays.asList(columns);
                List<String> types = Arrays.asList(reader.readLine().split(";"));
                if(types != null){
                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(";");
                        List<MutablePair<String,Object>> rowData = new ArrayList<>();
                        for (int i = 0; i < values.length; i++) {
                            String columnName = columnNames.get(i);
                            Object result;
                            switch(types.get(i)){
                                case "Boolean": {
                                    result = Boolean.parseBoolean(values[i]);
                                    break;
                                }
                                case "Byte": {
                                    result = Byte.parseByte(values[i]);
                                    break;
                                }
                                case "Short": {
                                    result = Short.parseShort(values[i]);
                                    break;
                                }
                                case "Integer": {
                                    result = Integer.parseInt(values[i]);
                                    break;
                                }
                                case "Long": {
                                    result = Long.parseLong(values[i]);
                                    break;
                                }
                                case "Float": {
                                    result = Float.parseFloat(values[i]);
                                    break;
                                }
                                case "Double": {
                                    result = Double.parseDouble(values[i]);
                                    break;
                                }
                                case "Character": {
                                    result = values[i].charAt(0);
                                    break;
                                }
                                case "String": {
                                    result = values[i];
                                    break;
                                }
                                default:{
                                    throw new IllegalArgumentException("Type non pris en charge : " + types.get(i));
                                }
                            }
                            rowData.add(new MutablePair<>(columnName,result));
                        }
                        table.addRow(rowData,dBase);
                        System.out.println(rowData+ " ajouté à "+table.getName());
                    }
                }
            }
        }
    }


    private static void printGenerationDependencies(Database myDb) throws FormatException, InvalidConditionException, TupleNotFoundException{
        // System.out.println(Database.evaluator.getVariables());
        for(int i = 0; i <myDb.getGenerationDependencies().size();i++){
            GenerationDependencies generationDependency = myDb.getGenerationDependencies().get(i);
            if(generationDependency instanceof EGD){
                EGD egd_cast = (EGD) generationDependency;
                System.out.println("EGD_"+i+" :"+egd_cast.isSatisfied());
            }
            if(generationDependency instanceof TGD){
                TGD tgd_cast = (TGD) generationDependency;
                System.out.println("TGD_"+i+" :"+tgd_cast.isSatisfied());
            }
        }
    }
    
}
