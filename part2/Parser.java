/* *** This file is given as part of the programming assignment. *** */

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token

    private void scan() {
        tok = scanner.scan();
    }

    private Scan scanner;

    Parser(Scan scanner) {
        this.scanner = scanner;
        scan();
        program();
        if (tok.kind != TK.EOF)
            parse_error("junk after logical end of program");
    }

    private void program() {
        block();
    }

    private void block() {
        declaration_list();
        statement_list();
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
        mustbe(TK.DECLARE);
        mustbe(TK.ID);
        while (is(TK.COMMA)) {
            scan();
            mustbe(TK.ID);
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
        } else if (is(TK.PRINT)) {
            PRINT();
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
            scan();
            GUARDED();
        }
        if (is(TK.ELSE)) {
            scan();
            block();
        }
        mustbe(TK.ENDIF);
    }

    private void EXPR() {
        TERM();
        while (is(TK.PLUS) || is(TK.MINUS)) {
            scan();
            TERM();
        }
    }

    private void TERM() {
        Factor();
        while (is(TK.TIMES) || is(TK.DIVIDE)) {
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
        mustbe(TK.THEN);
        block();
    }

    private void REF_ID() {
        if (is(TK.TILDE)) {
            mustbe(TK.TILDE);
            if(is(TK.NUM)) {
                NUMBER();
            }
        }
        ID();
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
        scan();
    }

    private void parse_error(String msg) {
        System.err.println("can't parse: line " + tok.lineNumber + " " + msg);
        System.exit(1);
    }
}
