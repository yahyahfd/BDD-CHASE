package fr.uparis.constraints.database;

import fr.uparis.database.Table;

// record classe basique qui fait un constructeur basique et des getters setters
// des parametres, pour éviter de trop écrire 
// si constantes non null, elle fera office de filtre pour l'EGD 
// si value est spécifiée, on ne cherche pas la valeur qui correspond à attribute dans table
// mais plutot les tuples dans table qui on value pour valeur de attribute   
public record EqualityAtom(Object attribute, Table table, boolean isConstant) {}