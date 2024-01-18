package lab09_05_11.parser.ast;

import lab09_05_11.visitors.Visitor;

public class Vect extends BinaryOp{

    public Vect(Exp left, Exp right) {
        super(left, right);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVect(left, right);
    }

}
