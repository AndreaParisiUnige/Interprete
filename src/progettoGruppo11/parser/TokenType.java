package lab09_05_11.parser;


public enum TokenType { 
	// symbols
	ASSIGN, MINUS, PLUS, TIMES, NOT, AND, EQ, SEPARATOR, PAIR_OP, OPEN_PAR, CLOSE_PAR, OPEN_BLOCK, CLOSE_BLOCK, OPEN_VECT, CLOSE_VECT,
	// keywords
	PRINT, VAR, BOOL, IF, ELSE, FOR, IN, FST, SND,
	// non singleton categories
	SKIP, IDENT, NUM,   
	// end-of-file
	EOF, 	
}
