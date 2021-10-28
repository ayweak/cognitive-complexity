package com.github.ayweak.cognitivecomplexity;

import org.antlr.v4.runtime.Token;

public class CognitiveComplexity {

    private Token funcName;
    private Token leftParen;
    private Token rightParen;
    private Token leftBrace;
    private Token rightBrace;
    private int complexity;

    public CognitiveComplexity() {

    }

    public void setFuncName(Token token) {
        funcName = token;
    }

    public void setLeftParen(Token token) {
        leftParen = token;
    }

    public void setRightParen(Token token) {
        rightParen = token;
    }

    public void setLeftBrace(Token token) {
        leftBrace = token;
    }

    public void setRightBrace(Token token) {
        rightBrace = token;
    }

    public void setComplexity(int c) {
        complexity = c;
    }

    public void addComplexity(int c) {
        complexity += c;
    }

    public boolean matchLeftParen(Token token) {
        return leftParen == token;
    }

    public boolean matchRightParen(Token token) {
        return rightParen == token;
    }

    public boolean matchLeftBrace(Token token) {
        return leftBrace == token;
    }

    public String getFuncName() {
        return funcName.getText();
    }

    public int getStartLine() {
        return funcName.getLine();
    }

    public int getEndLine() {
        return rightBrace.getLine();
    }

    public int getComplexity() {
        return complexity;
    }

}