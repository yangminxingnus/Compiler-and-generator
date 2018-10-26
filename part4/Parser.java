/* *** This file is given as part of the programming assignment. *** */

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token

    private void scan() {
        tok = scanner.scan();
    }

    private Scan scanner;

    private SymbolTable symbolTable;

    Parser(Scan scanner) {
        this.scanner = scanner;
        scan();
        program();
        if (tok.kind != TK.EOF)
            parse_error("junk after logical end of program");
    }

    private void program() {
        this.symbolTable = new SymbolTable();
        outputJava("public class My_e2j{");
        outputJava("public static void main(String[] args){");
        block();
        outputJava("}");
        outputJava("}");
    }

    private void block() {
        this.symbolTable.newBlcok();

        declaration_list();
        statement_list();
        this.symbolTable.exitBlock();
    }

    private void declaration_list() {
        // below checks whether tok is in first set of declaration.
        // here, that's easy since there's only one token kind in the set.
        // in other places, though, there might be more.
        // so, you might want to write a general function to handle that.
        while (is(TK.DECLARE)) {
            declaration();
        }
    }

    private void declaration() {
        int flag = 0;
        if (is(TK.DECLARE)) {
            scan();
            if(is(TK.ID)) {
                if(symbolTable.searchInCurrentBlock(tok.string + symbolTable.getValueStack().size())) {
                    System.err.println("redeclaration of variable " + tok.string);
                    scan();
                } else {
                    outputJava("int ");
                    flag = 1;
                    symbolTable.newSymbol(tok.string + symbolTable.getValueStack().size());
                    mustbe(TK.ID);
                }
            }

            while (is(TK.COMMA)) {
                scan();
                if(is(TK.ID)) {
                    if(symbolTable.searchInCurrentBlock(tok.string + symbolTable.getValueStack().size())) {
                        System.err.println("redeclaration of variable " + tok.string);
                        scan();
                    } else {
                        if(flag == 0) {
                            outputJava("int ");
                        } else {
                            outputJava(", ");
                        }
                        symbolTable.newSymbol(tok.string + symbolTable.getValueStack().size());
                        mustbe(TK.ID);
                    }
                } else {
                    mustbe(TK.ID);
                }
            }
            outputJava(";");
        } else {
            mustbe(TK.DECLARE);
        }
    }

    private void statement_list() {
        while (isFirst("statement")) {
            statement();
        }
    }

    private void statement() {
        if (isFirst("assign")) {
            ASSIGN();
            outputJava(";");
        } else if (is(TK.PRINT)) {
            PRINT();
            outputJava(";");
        } else if (is(TK.DO)) {
            DO();
        } else if (is(TK.IF)) {
            IF();
        } else {
            parse_error("illegal token encountered: " + tok);
        }
    }

    private void ASSIGN() {
        REF_ID();
        mustbe(TK.ASSIGN);
        EXPR();
    }

    private void PRINT() {
        mustbe(TK.PRINT);
        EXPR();
        outputJava(")");
    }

    private void DO() {
        mustbe(TK.DO);
        GUARDED();
        mustbe(TK.ENDDO);
    }

    private void IF() {
        mustbe(TK.IF);
        GUARDED();
        while (is(TK.ELSEIF)) {
            mustbe(TK.ELSEIF);
            GUARDED();
        }
        if (is(TK.ELSE)) {
            mustbe(TK.ELSE);
            block();
            outputJava("}");
        }
        mustbe(TK.ENDIF);
    }

    private void EXPR() {
        TERM();
        while (is(TK.PLUS) || is(TK.MINUS)) {
            outputJava(tok.string);
            scan();
            TERM();
        }
    }

    private void TERM() {
        Factor();
        while (is(TK.TIMES) || is(TK.DIVIDE)) {
            outputJava(tok.string);
            scan();
            Factor();
        }
    }

    private void Factor() {
        if (is(TK.LPAREN)) {
            mustbe(TK.LPAREN);
            EXPR();
            mustbe(TK.RPAREN);
        } else if (isFirst("ref_id")) {
            REF_ID();
        } else if (is(TK.NUM)) {
            mustbe(TK.NUM);
        } else {
            parse_error("Illegal character encountered, expecting a factor");
        }
    }

    private void GUARDED() {
        EXPR();
        outputJava("<= 0");
        mustbe(TK.THEN);
        block();
        outputJava("}");
    }

    private void REF_ID() {
        if (is(TK.TILDE)) {
            mustbe(TK.TILDE);
            int level = 0;

            if(is(TK.NUM)) {
                level = Integer.parseInt(tok.string);
                if(is(TK.NUM)) {
                    scan();
                } else {
                    NUMBER();
                }
                if(is(TK.ID)) {
                    if(!symbolTable.checkRefID(level, tok.string)) {
                        System.err.println("no such variable " + "~" + level + tok.string + " on line " + tok.lineNumber);
                        System.exit(1);
                    } else {
                        outputJava(tok.string + (symbolTable.getValueStack().size() - level));
                        scan();
                    }
                }
            } else {
                if(is(TK.ID)) {
                    if(!symbolTable.checkRefID(-1, tok.string)) {
                        System.err.println("no such variable " + "~" + tok.string + " on line " + tok.lineNumber);
                        System.exit(1);
                    } else {
                        outputJava(tok.string + 1);
                        scan();
                    }
                }
            }
        } else {
            if(is(TK.ID)) {
                if(!symbolTable.search(tok.string)) {
                    System.err.println(tok.string + " is an undeclared variable on line " + tok.lineNumber);
                    System.exit(1);
                }
            }
            mustbe(TK.ID);
        }
    }

    private void NUMBER() {
        mustbe(TK.NUM);
    }

    private void ID() {
        mustbe(TK.ID);
    }

    private boolean isFirst(String terminal) {
        boolean result = false;
        switch (terminal) {
            case "statement":
                result = isFirst("assign") || is(TK.PRINT) || is(TK.DO) || is(TK.IF);
                break;
            case "expr":
                result = isFirst("term");
                break;
            case "assign":
                result = isFirst("ref_id");
                break;
            case "term":
                result = isFirst("factor");
                break;
            case "factor":
                // how to check the parenthesis
                result = is(TK.LPAREN) || isFirst("ref_id") || is(TK.NUM);
                break;
            case "ref_id":
                result = is(TK.TILDE) || is(TK.ID);
                break;
            default:
                parse_error("illegal token encountered: " + tok);
        }
        return result;
    }


    private void outputJava(String javacode) {
        System.out.println(javacode);
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
        if (tok.kind != tk) {
            System.err.println("mustbe: want " + tk + ", got " + tok);
            parse_error("missing token (mustbe)");
        }

        if (tok.kind == TK.ID) {
            if(symbolTable.search(tok.string)) {
                outputJava(symbolTable.last(tok.string));
            }

        } else {
            switch (tok.string) {
                case "@":
                    outputJava("int ");
                    break;
                case "!":
                    outputJava("System.out.println(");
                    break;
                case "[":
                    outputJava("if(");
                    break;
                case "]":
                    outputJava("");
                    break;
                case ":":
                    outputJava("){");
                    break;
                case "|":
                    outputJava("else if(");
                    break;
                case "%":
                    outputJava("else{");
                    break;
                case "<":
                    outputJava("while(");
                    break;
                case ">":
                    outputJava("");
                    break;
                case "~":
                    break;
                default:
                    outputJava(tok.string);
            }
        }
        scan();
    }

    private void parse_error(String msg) {
        System.err.println("can't parse: line " + tok.lineNumber + " " + msg);
        System.exit(1);
    }
}
