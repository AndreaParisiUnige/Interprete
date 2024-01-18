package lab09_05_11.visitors.execution;

import java.util.ArrayList;
import java.util.Collections;

public class VectValue extends AtomicValue<ArrayList<Integer>>{

    // Chiama il costruttore della superclasse, controlla dim>=0 e ind < 0 || ind >= dim
    // Se rispetta tali vincoli inserisce nella superclasse un vettore di tutti 0, con un 1
    // in posizione ind

    public VectValue(Integer ind, Integer dim) {
        super(new ArrayList<>());
        if(dim<0)
            throw new InterpreterException("NegativeVectorDimension: " + dim );
        if(ind < 0 || ind >= dim)
            throw new InterpreterException("Vector index " + ind + " out of bounds for length " + dim);
        super.value.addAll(Collections.nCopies(dim, 0));
        super.value.set(ind,1);
    }

    // overload del costruttore per supporto ad operazioni + e *
    protected VectValue(ArrayList<Integer> l){
        super(l);
    }

    @Override
    public VectValue toVect() {
        return this;
    }

    @Override
    public String toString() {
        return super.toString().replace(", ", ";" );
    }
}
