package lab09_05_11.visitors.typechecking;

import static lab09_05_11.visitors.typechecking.AtomicType.*;

import lab09_05_11.environments.EnvironmentException;
import lab09_05_11.environments.GenEnvironment;
import lab09_05_11.parser.ast.Block;
import lab09_05_11.parser.ast.Exp;
import lab09_05_11.parser.ast.Stmt;
import lab09_05_11.parser.ast.StmtSeq;
import lab09_05_11.parser.ast.Variable;
import lab09_05_11.visitors.Visitor;
import lab09_05_11.visitors.execution.Value;

public class Typecheck implements Visitor<Type> {

	private final GenEnvironment<Type> env = new GenEnvironment<>();

    // useful to typecheck binary operations where operands must have the same type 
	private void checkBinOp(Exp left, Exp right, Type type) {
		type.checkEqual(left.accept(this));
		type.checkEqual(right.accept(this));
	}

	// verifica che il tipo sia o VECT o INT, in tal caso restituisce tale tipo
	// altrimenti solleva un'eccezione
	private Type checkIntOrVect(Exp exp) {
		var var = exp.accept(this);
		if(INT.equals(var) || VECT.equals(var))
			return var;
		throw new TypecheckerException(var.toString(), INT.toString()+ " or " + VECT.toString());
	}

	// static semantics for programs; no value returned by the visitor

	@Override
	public Type visitMyLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
		} catch (EnvironmentException e) { // undeclared variable
			throw new TypecheckerException(e);
		}
		return null;
	}

	// static semantics for statements; no value returned by the visitor

	// Assegna un valore ad una variabile esistente, lookup ricerca la variabile nell'ambiente,
	// e verifico che il tipo  corrisponda ad exp
	@Override
	public Type visitAssignStmt(Variable var, Exp exp) {
		env.lookup(var).checkEqual(exp.accept(this));
		return null;
	}

	@Override
	public Type visitPrintStmt(Exp exp) {
		exp.accept(this);
		return null;
	}

	@Override
	public Type visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));	//controlla già che non sia presente nello scope
		return null;
	}

	@Override
	public Type visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
	    BOOL.checkEqual(exp.accept(this));
		thenBlock.accept(this);
		if (elseBlock != null)	//l'elseBlock potrebbe non essere presente
			elseBlock.accept(this);
		return null;
	}

	@Override
	public Type visitForStmt(Variable var, Exp exp, Block block){
		VECT.checkEqual(exp.accept(this));	// controllo che exp abbia tipo VECT
		env.enterScope();
		env.dec(var, INT);	// dichiaro la nuova variabile su cui ciclare
		block.accept(this);
		env.exitScope();
		return null;
	}

	@Override
	public Type visitBlock(StmtSeq stmtSeq) {
		env.enterScope();	// creo un nuovo scope per le variabili del blocco
		stmtSeq.accept(this);
		env.exitScope();	// chiudo lo scope creato
		return null;
	}

	// static semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Type visitEmptyStmtSeq() { return null; }

	@Override
	public Type visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
		first.accept(this);
		rest.accept(this);
		return null;
	}

	// static semantics of expressions; a type is returned by the visitor

	@Override
	public AtomicType visitAdd(Exp left, Exp right) {
		var var = checkIntOrVect(left);
		var.checkEqual(right.accept(this));
		return var == INT ? INT : VECT;
	}

	@Override
	public AtomicType visitIntLiteral(int value) { return INT; }

	@Override
	public AtomicType visitMul(Exp left, Exp right) {
		if(checkIntOrVect(left) != checkIntOrVect(right))
			return VECT;
		else return INT;
	}

	@Override
	public AtomicType visitSign(Exp exp) {
		INT.checkEqual(exp.accept(this));
		return INT;
	}

	@Override
	public Type visitVariable(Variable var) { return env.lookup(var); }

	@Override
	public AtomicType visitNot(Exp exp) {
		BOOL.checkEqual(exp.accept(this));
		return BOOL;
	}

	@Override
	public AtomicType visitAnd(Exp left, Exp right) {
		checkBinOp(left, right, BOOL);
		return BOOL;
	}

	@Override
	public AtomicType visitBoolLiteral(boolean value) { return BOOL; }

	@Override
	public AtomicType visitEq(Exp left, Exp right) {
		left.accept(this).checkEqual(right.accept(this));
		return BOOL;
	}

	@Override
	public AtomicType visitVect(Exp left, Exp right){
		INT.checkEqual(left.accept(this));
		INT.checkEqual(right.accept(this));
		return VECT;
	}

	@Override
	public PairType visitPairLit(Exp left, Exp right) {
		return new PairType(left.accept(this), right.accept(this));
	}

	@Override
	public Type visitFst(Exp exp) {
		return exp.accept(this).getFstPairType();
	}

	@Override
	public Type visitSnd(Exp exp) {
		return exp.accept(this).getSndPairType();
	}

}
