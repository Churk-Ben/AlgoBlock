package com.algoblock.core.engine;

import com.algoblock.api.BinaryBlock;
import com.algoblock.api.Block;
import com.algoblock.api.UnaryBlock;
import com.algoblock.api.ValidationResult;
import com.algoblock.core.blocks.fn.ConstIntBlock;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final BlockRegistry registry;
    private final Lexer lexer;
    private List<Token> tokens;
    private int pos;

    public Parser(BlockRegistry registry) {
        this.registry = registry;
        this.lexer = new Lexer();
    }

    public Block<?> parse(String source) {
        tokens = lexer.tokenize(source);
        pos = 0;
        Block<?> root = parseExpr();
        expect(Token.Type.EOF);
        ValidationResult validation = root.validate();
        if (!validation.valid()) {
            throw new ParseException(validation.message());
        }
        return root;
    }

    private Block<?> parseExpr() {
        Token current = peek();
        if (current.type() == Token.Type.NUMBER) {
            advance();
            return new ConstIntBlock(Integer.parseInt(current.text()));
        }
        if (current.type() != Token.Type.IDENT) {
            throw new ParseException("Expected IDENT but got " + current.type());
        }
        advance();
        Block<?> block = registry.instantiate(current.text());
        int arity = registry.arity(current.text(), block);
        List<Block<?>> args = new ArrayList<>();
        while (peek().type() == Token.Type.LT) {
            advance();
            args.add(parseExpr());
            if (peek().type() == Token.Type.COMMA) {
                advance();
                args.add(parseExpr());
            }
            expect(Token.Type.GT);
        }
        if (args.size() != arity) {
            throw new ParseException(
                    "Arity mismatch for " + current.text() + ", expect " + arity + " but got " + args.size());
        }
        wire(block, args);
        return block;
    }

    @SuppressWarnings("unchecked")
    private void wire(Block<?> block, List<Block<?>> args) {
        if (block instanceof UnaryBlock unary && args.size() == 1) {
            unary.setChild(args.get(0));
            return;
        }
        if (block instanceof BinaryBlock binary && args.size() == 2) {
            binary.setLeft(args.get(0));
            binary.setRight(args.get(1));
        }
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private void advance() {
        pos++;
    }

    private void expect(Token.Type type) {
        Token current = peek();
        if (current.type() != type) {
            throw new ParseException("Expected " + type + " but got " + current.type());
        }
        advance();
    }
}
