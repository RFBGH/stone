package com.flypig.stone.bnf;

import com.flypig.stone.StoneConst;
import com.flypig.stone.ast.*;
import com.flypig.stone.exception.ParseException;
import com.flypig.stone.lexer.Lexer;
import com.flypig.stone.lexer.token.Token;
import com.flypig.stone.parser.Operators;
import com.flypig.stone.parser.Parser;

import java.util.HashSet;
import java.util.Set;

import static com.flypig.stone.parser.Parser.rule;

public class BasicParser {

    Set<String> reserved = new HashSet<>();
    Operators operators = new Operators();
    Parser expr0 = rule();
    Parser primary = rule(PrimaryExpr.class)
            .or(rule().sep("(").ast(expr0).sep(")"),
                    rule().number(NumberLiteral.class),
                    rule().identifier(Name.class, reserved),
                    rule().string(StringLiteral.class));
    Parser factor = rule().or(rule(NegativeExpr.class).sep("-").ast(primary), primary);
    Parser expr = expr0.expression(BinaryExpr.class, factor, operators);

    Parser statement0 = rule();
    Parser block = rule(BlockStmnt.class)
            .sep("{").option(statement0)
            .repeat(rule().sep(";", Token.EOL).option(statement0))
            .sep("}");
    Parser simple = rule(PrimaryExpr.class).ast(expr);
    Parser statement = statement0.or(
            rule(IfStmnt.class).sep("if").ast(expr).sep(Token.EOL).ast(block)
                            .option(rule().sep(Token.EOL).sep("else").sep(Token.EOL).ast(block)),
            rule(WhileStmnt.class).sep("while").ast(expr).sep(Token.EOL).ast(block),
            simple);

    Parser program = rule().or(statement, rule(NullStmnt.class)).sep(";", Token.EOL);

    public BasicParser(){
        reserved.add(";");
        reserved.add("}");
        reserved.add(Token.EOL);

        operators.add(StoneConst.EQUAL, 1, Operators.RIGHT);
        operators.add(StoneConst.CHECK_EQUAL, 2, Operators.LEFT);
        operators.add(StoneConst.BIGGER, 2, Operators.LEFT);
        operators.add(StoneConst.SMALLER, 2, Operators.LEFT);
        operators.add(StoneConst.ADD, 3, Operators.LEFT);
        operators.add(StoneConst.SUB, 3, Operators.LEFT);
        operators.add(StoneConst.MULT, 4, Operators.LEFT);
        operators.add(StoneConst.DIVID, 4, Operators.LEFT);
        operators.add(StoneConst.MOD, 4, Operators.LEFT);
    }

    public ASTree parse(Lexer lexer) throws ParseException{
        return program.parse(lexer);
    }
}
