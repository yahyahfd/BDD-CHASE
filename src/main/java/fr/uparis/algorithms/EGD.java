package fr.uparis.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.database.Database;
import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public class EGD {
    //logique de premier ordre suite de et logique
    // conjonction d'atomes relationnels et d'atomes d'égalité
    // les tables concernés par cet EGD
    // Rajouter un "filter" constantatoms sur les tuples selectionnés,
    // Si vide, on prend tout bien sûr
    private final Set<Pair<Table,ConstantAtoms>> relationalAtomsLeft = new HashSet<>();
    //left = right : les attributs sur lequels il y a une égalité
    private final Set<Pair<EqualityAtom,EqualityAtom>> equalityAtomsLeft = new HashSet<>();

    private final Set<Pair<EqualityAtom,EqualityAtom>> equalityAtomsRight = new HashSet<>();

    // vérifier que la table existe dans DB? comment? mettre db en parametre?
    public void addRelationalAtomLeft(Table relationalAtom, ConstantAtoms constants,Database dBase) throws FormatException{
        if(!dBase.tableExists(relationalAtom))
            throw new FormatException
            ("Vous essayez de rajouter un atome relationnel d'une table non existante !");
        relationalAtomsLeft.add(Pair.of(relationalAtom,constants));
    }

    public void addEqualityAtomLeft(Pair<EqualityAtom,EqualityAtom> equalityAtom){
        equalityAtomsLeft.add(equalityAtom);
    }
    
    public void addEqualityAtomRight(Pair<EqualityAtom,EqualityAtom> equalityAtom){
        equalityAtomsRight.add(equalityAtom);
    }

    public boolean isSatisfied() throws FormatException{
        // liste des tuples qui nous interessent dans chaque table.
        Set<Pair<Table,List<List<Object>>>> filteredTables = new HashSet<>();
        // On associe à chaque table présente, la nouvelle liste de tuples après filtre
        for(Pair<Table,ConstantAtoms> relationalAtomLeft : relationalAtomsLeft){
            ConstantAtoms constantAtoms = relationalAtomLeft.getRight();
            Table table = relationalAtomLeft.getLeft();
            List<List<Object>> filteredTable;
            if(constantAtoms == null){ // on prend toutes la table
                filteredTable = table.getRows();
            }else{ // on ne prend que les tuples concernés (vu qu'on utilise des constantes)
                filteredTable = table.selectFromTable(new ArrayList<>(constantAtoms.getConstantes()));
            }
            filteredTables.add(Pair.of(table,filteredTable));
        }
        // On doit maintenant vérifier les equalityAtoms qui vérifient le contenu de filteredTable
        // Premier qui vérifie pas -> on renvoie false
        // fin de boucle, on renvoie true -> tous les atomes sont valides
        // Set<Pair<EqualityAtom, EqualityAtom>> equalityAtomsLeft
        for(Pair<EqualityAtom,EqualityAtom> equalityAtomLeft: equalityAtomsLeft){
            // On selectionne la table qui nous interessent dans les relations:
            Table leftEqualityTable = equalityAtomLeft.getLeft().table();
            String leftAttribute = equalityAtomLeft.getLeft().attribute();
            boolean leftConstant = equalityAtomLeft.getLeft().isConstant();

            Table rightEqualityTable = equalityAtomLeft.getRight().table();
            String rightAttribute = equalityAtomLeft.getRight().attribute();
            boolean rightConstant = equalityAtomLeft.getRight().isConstant();


            for(Pair<Table,List<List<Object>>> filtedTable : filteredTables){
                if(filtedTable.getLeft().equals(leftEqualityTable)){// On a trouvé une table qui est impliquée
                    // On vérifie maintenant si la partie droite du pair (les tuples)
                    // contiennent la colonne qui nous interesse dans la liste
                    // Si value est spécifiée dans equalityAtom, on filtre sur les colonnes de nom
                    // columnName qui ont pour valeur cette valeur
                    // List<List<Object>> tuplesFiltered = filtedTable.getLeft()
                    for(Pair<Table,List<List<Object>>> filtedTable_bis : filteredTables){
                        if(filtedTable_bis.getLeft().equals(rightEqualityTable)){// On a trouvé une table impliquée
                            if(leftConstant){// c'est une constante
                                if(rightConstant){// c'est une constante
                                    if(!leftAttribute.equals(rightAttribute)){
                                        return false;
                                        // on retourne false parce que x !=y et on cherche à vérifier si x == y
                                    }
                                }else{// columnName = rightAttribute
                                    int rightIndex = rightEqualityTable.getColumnIndex(rightAttribute);

                                    // for(List<Object>)
                                    // on cherche chaque attribut correspondant dans les tuples selectionnés
                                    // si un seul a une valeur differente de leftAttribute.parseInt
                                    for(List<Object> tuple: filtedTable_bis.getRight()){
                                        Object value = tuple.get(rightIndex);
                                        if(!value.equals(leftAttribute)){
                                            return false;
                                        }
                                    }
                                }
                            }else{ //left est un attribut
                                int leftIndex = leftEqualityTable.getColumnIndex(leftAttribute);
                                if(rightConstant){// constante
                                    for(List<Object> tuple: filtedTable.getRight()){
                                        Object value = tuple.get(leftIndex);
                                        if(!value.equals(rightAttribute)){
                                            return false;
                                        }
                                    }
                                }else{ // les 2 sont des attributs
                                    int rightIndex = rightEqualityTable.getColumnIndex(rightAttribute);
                                    for(List<Object> tuple_left: filtedTable.getRight()){
                                        for(List<Object> tuple_right: filtedTable_bis.getRight()){
                                            Object valueLeft = tuple_left.get(leftIndex);
                                            Object valueRight = tuple_right.get(rightIndex);
                                            if(!valueLeft.equals(valueRight)){
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
