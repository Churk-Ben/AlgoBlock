package com.algoblock.core.engine;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '<') {
                tokens.add(new Token(Token.Type.LT, "<"));
                i++;
                continue;
            }
            if (c == '>') {
                tokens.add(new Token(Token.Type.GT, ">"));
                i++;
                continue;
            }
            if (c == ',') {
                tokens.add(new Token(Token.Type.COMMA, ","));
                i++;
                continue;
            }
            if (Character.isDigit(c)) {
                int start = i;
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    i++;
                }
                tokens.add(new Token(Token.Type.NUMBER, input.substring(start, i)));
                continue;
            }
            if (Character.isLetter(c) || c == '_' ) {
                int start = i;
                while (i < input.length()) {
                    char cc = input.charAt(i);
                    if (!(Character.isLetterOrDigit(cc) || cc == '_')) {
                        break;
                    }
                    i++;
                }
                tokens.add(new Token(Token.Type.IDENT, input.substring(start, i)));
                continue;
            }
            throw new ParseException("Invalid character: " + c);
        }
        tokens.add(new Token(Token.Type.EOF, ""));
        return tokens;
    }
}
