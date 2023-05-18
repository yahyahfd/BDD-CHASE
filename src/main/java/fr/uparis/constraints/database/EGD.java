package fr.uparis.constraints.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

import fr.uparis.database.Database;
import fr.uparis.database.Table;
import fr.uparis.exceptions.FormatException;
import fr.uparis.exceptions.InvalidConditionException;
import fr.uparis.exceptions.TupleNotFoundException;

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
            throws FormatException, InvalidConditionException, TupleNotFoundException {
        // liste des tuples qui nous interessent dans chaque table.
        Set<Pair<Table, List<List<Object>>>> filteredTables = filterTable(getRelationalAtomsLeft());

        // On doit maintenant vérifier les equalityAtoms qui vérifient le contenu de
        // filteredTable
        // Premier qui vérifie pas -> on renvoie false
        // fin de boucle, on renvoie true -> tous les atomes sont valides
        // Set<Pair<EqualityAtom, EqualityAtom>> equalityAtomsLeft

        Set<Pair<EqualityAtom, EqualityAtom>> equalityAtoms;
        equalityAtoms = leftRight ? equalityAtomsLeft : equalityAtomsRight;
        for(Pair<Table,List<List<Object>>> filteredTableA :filteredTables){
            for(Pair<Table,List<List<Object>>> filteredTableB :filteredTables){
                for(Pair<EqualityAtom,EqualityAtom> equalityAtom : equalityAtoms){
                    Table leftEqualityTable = equalityAtom.getLeft().table();
                    Object leftAttribute = equalityAtom.getLeft().attribute();
                    boolean leftConstant = equalityAtom.getLeft().isConstant();

                    Table rightEqualityTable = equalityAtom.getRight().table();
                    Object rightAttribute = equalityAtom.getRight().attribute();
                    boolean rightConstant = equalityAtom.getRight().isConstant();
                    // On a trouvé une table impliqué par la gauche dans l'égalité
                    if(filteredTableA.getLeft().equals(leftEqualityTable)){
                        // On a trouvé une table impliqué par la droite dans l'égalité
                        if(filteredTableB.getLeft().equals(rightEqualityTable)){
                            // Si l'égalité est valide, on ne fait rien
                            // Si l'égalité est fausse, on renvoie le tuple faux
                            if(leftConstant){// La gauche est une constante
                                if(rightConstant){// La droite est une constante
                                    if (!leftAttribute.equals(rightAttribute)) {
                                        throw new InvalidConditionException("Une EGD présente n'a pas de sens: " + leftAttribute
                                        + " est tout le temps différent de " + rightAttribute);
                                    }
                                }else{// constant;attribute
                                    // Index de la colonne spécifiée dans l'égalité droite
                                    int rightIndex = rightEqualityTable.getColumnIndex((String) rightAttribute);
                                    boolean foundValue = false;
                                    for(List<Object> tuple : filteredTableB.getRight()){
                                        Object value = tuple.get(rightIndex); // On récupère notre valeur de droite
                                        // On teste si on a trouvé le bon attribut
                                        if(value.equals(leftAttribute)){
                                            foundValue = true;
                                            break; // L'EGD est satisfaite
                                        }
                                    }
                                    if(!foundValue){// On n'a pas trouvé un seul tuple correct?
                                        // l'EGD n'est pas satisfaite, on ne peut pas la corriger vu qu'il s'agit d'une 
                                        //  constante
                                        /* THROW UNE EXCEPTION -> return false dans le standard*/
                                        throw new TupleNotFoundException("EGD non satisfaite. Aucun tuple dans la table "
                                        +rightEqualityTable+" ne contient la valeur "+leftAttribute+" à la colonne "+ rightAttribute);
                                    }
                                }
                            }else{// La gauche est un attribut
                                if(rightConstant){// La droite est une constante
                                    // Index de la colonne spécifiée dans l'égalité gauche
                                    int leftIndex = leftEqualityTable.getColumnIndex((String) leftAttribute);
                                    boolean foundValue = false;
                                    for(List<Object> tuple: filteredTableA.getRight()){
                                        Object value = tuple.get(leftIndex);
                                        if(value.equals(rightAttribute)){
                                            foundValue = true;
                                            break;
                                        }
                                    }
                                    if(!foundValue){// On n'a pas trouvé un seul tuple correct?
                                    // l'EGD n'est pas satisfaite, on ne peut pas la corriger vu qu'il s'agit d'une 
                                    //  constante
                                    /* THROW UNE EXCEPTION -> return false dans le standard*/
                                    throw new TupleNotFoundException("EGD non satisfaite. Aucun tuple dans la table "
                                        +leftEqualityTable+" ne contient la valeur "+rightAttribute+" à la colonne "+ leftAttribute);
                                    }
                                }else{// attribute;attribute
                                    // pour chaque attribut gauche, je cherche l'attribut de droite correspondant
                                    int leftIndex = leftEqualityTable.getColumnIndex((String) leftAttribute);
                                    int rightIndex = rightEqualityTable.getColumnIndex((String) rightAttribute);
                                    for (List<Object> tuple_left : filteredTableA.getRight()) {
                                        boolean foundValue = false;
                                        Object valueLeft = tuple_left.get(leftIndex);
                                        for (List<Object> tuple_right : filteredTableB.getRight()) {
                                            Object valueRight = tuple_right.get(rightIndex);
                                            if (valueLeft.equals(valueRight)) {
                                                foundValue = true;
                                                break;
                                            }
                                        }
                                        if (!foundValue) {
                                            // l'EGD n'est pas satisfaite par ce tuple,
                                            // on peut renvoyer le tuple gauche et la table droite
                                            // Ou juste throw une exception
                                            boolean foundToCorrect = false;
                                            for(List<Object> tuple_right: filteredTableB.getRight()){
                                                List<Pair<EqualityAtom,EqualityAtom>> equalityAtomsTmp = new ArrayList<>();
                                                equalityAtomsTmp.addAll(equalityAtomsLeft);
                                                equalityAtomsTmp.addAll(equalityAtomsRight);
                                                for(int i = 0; i<equalityAtomsTmp.size();i++){
                                                    Pair<EqualityAtom,EqualityAtom> equalityAtomTmp = equalityAtomsTmp.get(i);
                                                    boolean isConstantLeft = equalityAtomTmp.getLeft().isConstant();
                                                    if(!isConstantLeft){
                                                        Table tableLeft = equalityAtomTmp.getLeft().table();
                                                        Object attributeLeft = equalityAtomTmp.getLeft().attribute();
                                                        if(tableLeft.equals(filteredTableB.getLeft())){// égalité correcte à gauche
                                                            boolean isConstantRight = equalityAtomTmp.getRight().isConstant();
                                                            if(!isConstantRight){
                                                                Object attributeRight = equalityAtomTmp.getRight().attribute();
                                                                Table tableRight = equalityAtomTmp.getRight().table();
                                                                if(tableRight.equals(filteredTableA.getLeft())){
                                                                    int attributeIndexLeft = tableLeft.getColumnIndex((String) attributeLeft);
                                                                    int attributeIndexRight = tableRight.getColumnIndex((String) attributeRight);
                                                                    if(tuple_left.get(attributeIndexRight)
                                                                    .equals(tuple_right.get(attributeIndexLeft))){
                                                                        foundToCorrect = true;
                                                                    }else{
                                                                        if(foundToCorrect){
                                                                            // return le tuple
                                                                        Pair<Table, List<Object>> tableTupleIncorrect = Pair
                                                                                .of(rightEqualityTable, tuple_right);

                                                                        // Contient la colonne et la nouvelle valeur
                                                                        Pair<String, Object> columnValueIncorrect = Pair.of((String) rightAttribute,
                                                                                valueLeft);

                                                                        return Pair.of(tableTupleIncorrect, columnValueIncorrect);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if(foundToCorrect){
                                                            // return le tuple
                                                        Pair<Table, List<Object>> tableTupleIncorrect = Pair
                                                                .of(rightEqualityTable, tuple_right);

                                                        // Contient la colonne et la nouvelle valeur
                                                        Pair<String, Object> columnValueIncorrect = Pair.of((String) rightAttribute,
                                                                valueLeft);

                                                        return Pair.of(tableTupleIncorrect, columnValueIncorrect);
                                                        }
                                                    }
                                                }

                                                // On fait une liste de colonnes en commun avec tuple_left et les égalités
                                            }
                                            // On a notre tuple gauche
                                            // throw new TupleNotFoundException("EGD non satisfaite. Aucun tuple dans la table "
                                            // +leftEqualityTable+" ne contient de valeur équivalente à celle d'un tuple dans la table "
                                            // +rightEqualityTable+" à la colonne "+rightAttribute+". On cherchait la valeur "+tuple_left.get(leftIndex)
                                            // +" dans la deuxième table, mais on n'y a trouvé que ");
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
    public Pair<Pair<Table, List<Object>>, Pair<String, Object>> isSatisfied() throws FormatException, InvalidConditionException, TupleNotFoundException {
        if (isSatisfied(true) == null) {// gauche valide
            return isSatisfied(false); // null si droite valide, les valeurs invalides de gauche sinon
        }
        return null; // Si gauche invalide c'est bon
    }
}
