package lab09_05_11.visitors.execution;

import java.io.PrintWriter;
import java.util.ArrayList;

import lab09_05_11.environments.EnvironmentException;
import lab09_05_11.environments.GenEnvironment;
import lab09_05_11.parser.ast.*;
import lab09_05_11.visitors.Visitor;

import static java.util.Objects.requireNonNull;

public class Execute implements Visitor<Value> {

	private final GenEnvironment<Value> env = new GenEnvironment<>();
	private final PrintWriter printWriter; // output stream used to print values

	public Execute() {
		printWriter = new PrintWriter(System.out, true);
	}

	public Execute(PrintWriter printWriter) {
		this.printWriter = requireNonNull(printWriter);
	}

	// dynamic semantics for programs; no value returned by the visitor

	@Override
	public Value visitMyLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
			// possible runtime errors
			// EnvironmentException: undefined variable
		} catch (EnvironmentException e) {
			throw new InterpreterException(e);
		}
		return null;
	}

	// dynamic semantics for statements; no value returned by the visitor
	@Override
	public Value visitAssignStmt(Variable var, Exp exp) {
	    env.update(var, exp.accept(this));
		return null;
	}

	@Override
	public Value visitPrintStmt(Exp exp) {
		printWriter.println(exp.accept(this));
		return null;
	}

	@Override
	public Value visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Value visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
		if (exp.accept(this).toBool())
			thenBlock.accept(this);
		else if (elseBlock != null)
			elseBlock.accept(this);
		return null;
	}

	@Override
	public Value visitForStmt(Variable var, Exp exp, Block block){
		var Vect = exp.accept(this).toVect();
		env.enterScope();
		env.dec(var, new IntValue(0));
		for(int i = 0 ; i < Vect.value.size() ; i++){
			env.update(var, new IntValue(Vect.value.get(i)));
			block.accept(this);
		}
		env.exitScope();
		return null;
	}


	@Override
	public Value visitBlock(StmtSeq stmtSeq) {
		env.enterScope();
		stmtSeq.accept(this);
		env.exitScope();
	    return null;
	}

	// dynamic semantics for sequences of statements
	// no value returned by the visitor
	@Override
	public Value visitEmptyStmtSeq() {
	    return null;
	}

	@Override
	public Value visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
		first.accept(this);
		rest.accept(this);
		return null;
	}

	// dynamic semantics of expressions; a value is returned by the visitor

	@Override
	public Value visitAdd(Exp left, Exp right) {
		var fst = left.accept(this);
		var snd = right.accept(this);

		if(fst instanceof VectValue && snd instanceof VectValue) { 		   // caso somma tra vettori
			if (fst.toVect().value.size() != snd.toVect().value.size())    // solleva un eccezione in caso di vettori con dimensione differente
				throw new InterpreterException("Vectors must have the same dimension");
			ArrayList<Integer> res = new ArrayList<>();							   // lista per contenere il risultato
			for (int i = 0; i < fst.toVect().value.size(); i++)
				res.add(fst.toVect().value.get(i) + snd.toVect().value.get(i));
			return new VectValue(res);
		}
		else if(fst instanceof IntValue && snd instanceof IntValue)		// caso somma tra interi
			return new IntValue(fst.toInt()+snd.toInt());
		else if(fst instanceof IntValue)	// arrivati qui, se il primo operatore è un intero, il secondo sarà un tipo inaspettato, sollevo un eccezione
			throw new InterpreterException("ExpectingDynamicType Int");
		else if(fst instanceof VectValue)	// arrivati qui, se il primo operatore è un vettore, il secondo sarà un tipo inaspettato, sollevo un eccezione
			throw new InterpreterException("ExpectingDynamicType Vect");
		// non è stato trovato ne un intero ne un vettore come primo valore
		throw new InterpreterException("ExpectingIntOrVectError");
	}

	@Override
	public IntValue visitIntLiteral(int value) {
		return new IntValue(value);
	}

	@Override
	public Value visitMul(Exp left, Exp right) {
		var fst = left.accept(this);
		var snd = right.accept(this);

		if(fst instanceof IntValue && snd instanceof IntValue)		// caso moltiplicazione tra interi
			return new IntValue(fst.toInt()*snd.toInt());
		else if(fst instanceof VectValue && snd instanceof VectValue){
			if (fst.toVect().value.size() != snd.toVect().value.size())    // solleva un eccezione in caso di vettori con dimensione differente
				throw new InterpreterException("Vectors must have the same dimension");
			int res = 0;
			for(int i=0; i < fst.toVect().value.size(); i++)
				res+=(fst.toVect().value.get(i) * snd.toVect().value.get(i));
			return new IntValue(res);
		}
		else if ((fst instanceof IntValue && snd instanceof VectValue) || (fst instanceof VectValue && snd instanceof IntValue)) {
			ArrayList<Integer> res = new ArrayList<>();
			if (fst instanceof IntValue) {
				for (int i = 0; i < snd.toVect().value.size(); i++)
					res.add(fst.toInt() * snd.toVect().value.get(i));
			} else {
				for (int i = 0; i < fst.toVect().value.size(); i++)
					res.add(snd.toInt() * fst.toVect().value.get(i));
			}
			return new VectValue(res);
		}

		throw new InterpreterException("ExpectingIntOrVectError");
	}
    
	@Override
	public IntValue visitSign(Exp exp) {
		return new IntValue(-exp.accept(this).toInt());
	}

	@Override
	public Value visitVariable(Variable var) {
		return env.lookup(var);
	}

	@Override
	public BoolValue visitNot(Exp exp) {
		return new BoolValue(!exp.accept(this).toBool());
	}

	@Override
	public BoolValue visitAnd(Exp left, Exp right) {
		return new BoolValue(left.accept(this).toBool() && right.accept(this).toBool());
	}

	@Override
	public BoolValue visitBoolLiteral(boolean value) {
	    return new BoolValue(value);
	}

	@Override
	public BoolValue visitEq(Exp left, Exp right) {
	    return new BoolValue(left.accept(this).equals(right.accept(this)));
	}

	@Override
	public PairValue visitPairLit(Exp left, Exp right) {
	    return new PairValue(left.accept(this),right.accept(this));
	}

	@Override
	public Value visitFst(Exp exp) {
		return exp.accept(this).toPair().getFstVal();
	}

	@Override
	public Value visitSnd(Exp exp) {
		return exp.accept(this).toPair().getSndVal();
	}

	@Override
	public Value visitVect(Exp left, Exp right){
		return new VectValue(left.accept(this).toInt(),right.accept(this).toInt());
	}

}

