package fr.uparis.constraints.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.database.Database;
import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;

public class EGD extends GenerationDependencies {
    // logique de premier ordre suite de et logique
    // conjonction d'atomes relationnels et d'atomes d'égalité
    // les tables concernés par cet EGD
    // Rajouter un "filter" constantatoms sur les tuples selectionnés,
    // Si vide, on prend tout bien sûr
    // left = right : les attributs sur lequels il y a une égalité
    private final Set<Pair<EqualityAtom, EqualityAtom>> equalityAtomsLeft = new HashSet<>();

    private final Set<Pair<EqualityAtom, EqualityAtom>> equalityAtomsRight = new HashSet<>();

    public Set<Pair<EqualityAtom, EqualityAtom>> getEqualityAtomsRight() {
        return equalityAtomsRight;
    }

    public Set<Pair<EqualityAtom, EqualityAtom>> getEqualityAtomsLeft() {
        return equalityAtomsLeft;
    }

    // vérifier que la table existe dans DB? comment? mettre db en parametre?
    public void addRelationalAtomLeft(Table relationalAtom, ConstantAtoms constants, Database dBase)
            throws FormatException {
        this.addRelationalAtom(relationalAtom, constants, dBase, getRelationalAtomsLeft());
    }

    public void addEqualityAtomLeft(Pair<EqualityAtom, EqualityAtom> equalityAtom) {
        equalityAtomsLeft.add(equalityAtom);
    }

    public void addEqualityAtomRight(Pair<EqualityAtom, EqualityAtom> equalityAtom) {
        equalityAtomsRight.add(equalityAtom);
    }

    // Si true, on prend le left, si false, on prend le right
    private Pair<Pair<Table, List<Object>>, Pair<String, Object>> isSatisfied(boolean leftRight)
            throws FormatException {
        // liste des tuples qui nous interessent dans chaque table.
        Set<Pair<Table, List<List<Object>>>> filteredTables = filterTable(getRelationalAtomsLeft());

        // On doit maintenant vérifier les equalityAtoms qui vérifient le contenu de
        // filteredTable
        // Premier qui vérifie pas -> on renvoie false
        // fin de boucle, on renvoie true -> tous les atomes sont valides
        // Set<Pair<EqualityAtom, EqualityAtom>> equalityAtomsLeft

        Set<Pair<EqualityAtom, EqualityAtom>> equalityAtoms;
        equalityAtoms = leftRight ? equalityAtomsLeft : equalityAtomsRight;

        for (Pair<EqualityAtom, EqualityAtom> equalityAtomLeft : equalityAtoms) {
            // On selectionne la table qui nous interessent dans les relations:
            Table leftEqualityTable = equalityAtomLeft.getLeft().table();
            Object leftAttribute = equalityAtomLeft.getLeft().attribute();
            boolean leftConstant = equalityAtomLeft.getLeft().isConstant();

            Table rightEqualityTable = equalityAtomLeft.getRight().table();
            Object rightAttribute = equalityAtomLeft.getRight().attribute();
            boolean rightConstant = equalityAtomLeft.getRight().isConstant();

            for (Pair<Table, List<List<Object>>> filtedTable : filteredTables) {
                if (filtedTable.getLeft().equals(leftEqualityTable)) {// On a trouvé une table qui est impliquée
                    // On vérifie maintenant si la partie droite du pair (les tuples)
                    // contiennent la colonne qui nous interesse dans la liste
                    // Si value est spécifiée dans equalityAtom, on filtre sur les colonnes de nom
                    // columnName qui ont pour valeur cette valeur
                    // List<List<Object>> tuplesFiltered = filtedTable.getLeft()
                    for (Pair<Table, List<List<Object>>> filtedTable_bis : filteredTables) {
                        if (filtedTable_bis.getLeft().equals(rightEqualityTable)) {// On a trouvé une table impliquée
                            if (leftConstant) {// c'est une constante
                                if (rightConstant) {// constant;constant
                                    if (!leftAttribute.equals(rightAttribute)) {
                                        // on throw une exception plutot pour dire que c'est la condition qui est
                                        // bizarre
                                        // return false;
                                        System.out.println("Une EGD présente n'a pas de sens: " + leftAttribute
                                                + " est tout le temps différent de " + rightAttribute);
                                        return null;
                                        // on retourne false parce que x !=y et on cherche à vérifier si x == y
                                    }
                                } else {// constant;attribute
                                    int rightIndex = rightEqualityTable.getColumnIndex((String) rightAttribute);

                                    // for(List<Object>)
                                    // on cherche chaque attribut correspondant dans les tuples selectionnés
                                    // si un seul a une valeur differente de leftAttribute.parseInt
                                    for (List<Object> tuple : filtedTable_bis.getRight()) {
                                        Object value = tuple.get(rightIndex);
                                        if (!value.equals(leftAttribute)) {
                                            // On doit retourner la table, le tuple et l'attribut à modifier plus la
                                            // valeur
                                            // table et tuple à corriger
                                            Pair<Table, List<Object>> tableTupleIncorrect = Pair.of(rightEqualityTable,
                                                    tuple);

                                            // Contient la colonne et la nouvelle valeur
                                            Pair<String, Object> columnValueIncorrect = Pair.of((String) rightAttribute,
                                                    leftAttribute);

                                            return Pair.of(tableTupleIncorrect, columnValueIncorrect);
                                        }
                                    }
                                }
                            } else { // left est un attribut
                                int leftIndex = leftEqualityTable.getColumnIndex((String) leftAttribute);
                                if (rightConstant) {// attribute;constant
                                    boolean foundValue = false;
                                    List<Object> resultTuple = null;
                                    for (List<Object> tuple : filtedTable.getRight()) {
                                        Object value = tuple.get(leftIndex);
                                        if (!value.equals(rightAttribute)) {
                                            resultTuple = tuple;
                                            continue;
                                        } else {
                                            foundValue = true;
                                            break;
                                        }
                                    }
                                    if (!foundValue) {
                                        // On doit retourner la table, le tuple et l'attribut à modifier plus la
                                        // valeur
                                        // table et tuple à corriger
                                        Pair<Table, List<Object>> tableTupleIncorrect = Pair.of(leftEqualityTable,
                                                resultTuple);

                                        // Contient la colonne et la nouvelle valeur
                                        Pair<String, Object> columnValueIncorrect = Pair.of((String) leftAttribute,
                                                rightAttribute);

                                        return Pair.of(tableTupleIncorrect, columnValueIncorrect);
                                    }
                                } else { // attribute;attribute
                                    int rightIndex = rightEqualityTable.getColumnIndex((String) rightAttribute);
                                    boolean foundValue = false;
                                    List<Object> resultTuple = null;
                                    for (List<Object> tuple_left : filtedTable.getRight()) {
                                        Object valueLeft = tuple_left.get(leftIndex);
                                        for (List<Object> tuple_right : filtedTable_bis.getRight()) {
                                            Object valueRight = tuple_right.get(rightIndex);
                                            if (!valueLeft.equals(valueRight)) {
                                                resultTuple = tuple_right;
                                                continue;
                                            } else {
                                                foundValue = true;
                                                break; // on passe au tuple suivant gauche
                                            }
                                        }
                                        if (!foundValue) {
                                            // On doit retourner la table, le tuple et l'attribut à modifier plus la
                                            // valeur
                                            // table et tuple à corriger
                                            // on considere que droite est fausse: traiter le cas où gauche est faux
                                            // et contient null?
                                            Pair<Table, List<Object>> tableTupleIncorrect = Pair
                                                    .of(rightEqualityTable, resultTuple);

                                            // Contient la colonne et la nouvelle valeur
                                            Pair<String, Object> columnValueIncorrect = Pair.of((String) rightAttribute,
                                                    valueLeft);

                                            return Pair.of(tableTupleIncorrect, columnValueIncorrect);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // renvoie null si la partie gauche n'est pas satisfaite, sinon renvoie la
    // droite
    public Pair<Pair<Table, List<Object>>, Pair<String, Object>> isSatisfied() throws FormatException {
        if (isSatisfied(true) == null) {// gauche valide
            return isSatisfied(false); // null si droite valide, les valeurs invalides de gauche sinon
        }
        return null; // Si gauche invalide c'est bon
    }
}
