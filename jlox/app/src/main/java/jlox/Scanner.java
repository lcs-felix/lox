package jlox;

import static jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Scanner {
    
    private static final Map<String, TokenType> keywords = Map.ofEntries(
        Map.entry("and", AND),
        Map.entry("class", CLASS),
        Map.entry("else", ELSE),
        Map.entry("false", FALSE),
        Map.entry("for", FOR),
        Map.entry("fun", FUN),
        Map.entry("if", IF),
        Map.entry("nil", NIL),
        Map.entry("or", OR),
        Map.entry("print", PRINT),
        Map.entry("return", RETURN),
        Map.entry("super", SUPER),
        Map.entry("this", THIS),
        Map.entry("true", TRUE),
        Map.entry("var", VAR),
        Map.entry("while", WHILE)
    );

    private final List<Token> tokens = new ArrayList<>();
    private final String source;
    private int line = 0;
    private int start = 0;
    private int current = 1;
    
    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        var c = advance();
        switch(c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL : EQUAL_EQUAL);
            case '<' -> addToken(match('=') ? LESS : LESS_EQUAL);
            case '>' -> addToken(match('=') ? GREATER : GREATER_EQUAL);
            case '/' -> {
                if(match('/')) {
                    while(peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> { return; }
            case '\n' -> line++;
            case '"' -> string();
            default -> {
                if(isDigit(c)) {
                    number();
                } else if(isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
            }
        }
    }

    private void identifier() {
        while(isAlphaNumeric(peek())) {
            advance();
        }

        var text = source.substring(start, current);
        var tokenType = keywords.get(text);
        if(tokenType == null) {
            tokenType = IDENTIFIER;
        }
        addToken(tokenType);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_');
    }

    private void number() {
        while(isDigit(peek())) {
            advance();
        }

        if(peek() == '.' && isDigit(peekNext())) {
            advance();

            while(isDigit(peek())) {
                advance();
            }
        }

        var textNumber = source.substring(start, current);
        addToken(NUMBER, Double.parseDouble(textNumber));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void string() {
        if(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') {
                line++;
            }

            advance();
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        
        var stringLiteral = source.substring(start + 1, current - 1);
        addToken(STRING, stringLiteral);
    }

    private char peek() {
        if(isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if(isAtEnd()) {
            return false;
        }

        if(source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
