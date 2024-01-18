package lab09_05_11.parser.ast;

import lab09_05_11.visitors.Visitor;

import static java.util.Objects.requireNonNull;

public class ForStmt implements Stmt{
    private final Variable var; // non-optional field
    private final Exp exp; // non-optional field
    private final Block block; // non-optional field

    public ForStmt(Variable var, Exp exp, Block block) {
        this.var = requireNonNull(var);
        this.exp = requireNonNull(exp);
        this.block = requireNonNull(block);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + var + "," + exp + "," + block + ")";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitForStmt(var, exp, block);
    }
}
