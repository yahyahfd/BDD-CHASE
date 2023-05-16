package fr.uparis.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class ConstantAtoms {
    private Set<Pair<String, Object>> constantes = new HashSet<>();

    public Set<Pair<String, Object>> getConstantes() {
        return new HashSet<>(constantes);
    }

    public void addConstante(Pair<String, Object> constante) {
        this.constantes.add(constante);
    }
}
