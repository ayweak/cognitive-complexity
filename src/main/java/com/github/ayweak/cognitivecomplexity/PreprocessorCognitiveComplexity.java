package com.github.ayweak.cognitivecomplexity;

import org.antlr.v4.runtime.Token;

public class PreprocessorCognitiveComplexity {

    private Token ppIf;
    private Token ppEndif;
    private int complexity;

    public PreprocessorCognitiveComplexity() {

    }

    public void setIf(Token token) {
        ppIf = token;
    }

    public void setEndif(Token token) {
        ppEndif = token;
    }

    public void setComplexity(int c) {
        complexity = c;
    }

    public void addComplexity(int c) {
        complexity += c;
    }

    public int getStartLine() {
        return ppIf.getLine();
    }

    public int getEndLine() {
        return ppEndif.getLine();
    }

    public int getComplexity() {
        return complexity;
    }

}
