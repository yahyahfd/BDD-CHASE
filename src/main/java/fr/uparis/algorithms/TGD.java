// package fr.uparis.algorithms;

// import java.util.HashSet;
// import java.util.Set;

// import org.apache.commons.lang3.tuple.Pair;
// // 
// import fr.uparis.database.Table;

// public class TGD {
//     private final Set<Pair<Table,ConstantAtoms>> relationalAtomsLeft = new HashSet<>();
//     private final Set<Pair<Table,ConstantAtoms>> relationalAtomRight = new HashSet<>();

//     public void addRelationalAtomLeft(Table relationalAtom, ConstantAtoms constants,Database dBase) throws FormatException{
//         if(!dBase.tableExists(relationalAtom))
//             throw new FormatException
//             ("Vous essayez de rajouter un atome relationnel d'une table non existante !");
//         relationalAtomsLeft.add(Pair.of(relationalAtom,constants));
//     }
// }
