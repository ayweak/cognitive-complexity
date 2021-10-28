package com.github.ayweak.cognitivecomplexity;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.Token;

public class NestingState {

    private Deque<Token> stack;

    private int nestingLevel;

    private Set<Integer> nestingStructures;

    private boolean reachedBottomStmt;

    public NestingState(HashSet<Integer> nestingStructures) {
        stack = new ArrayDeque<>();
        nestingLevel = 0;
        this.nestingStructures = new HashSet<>(nestingStructures);
        reachedBottomStmt = false;
    }

    public NestingState(NestingState state) {
        stack = new ArrayDeque<>(state.stack);
        nestingLevel = state.nestingLevel;
        nestingStructures = new HashSet<>(state.nestingStructures);
        reachedBottomStmt = state.reachedBottomStmt;
    }

    public void addLast(Token token) {
        if (nestingStructures.contains(token.getType())) {
            nestingLevel++;
        }
        stack.addLast(token);
    }

    public Token getLast() {
        return stack.getLast();
    }

    public int getNestingLevel() {
        return nestingLevel;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public Token removeLast() {
        Token token = stack.removeLast();
        if (nestingStructures.contains(token.getType())) {
            nestingLevel--;
        }
        return token;
    }

    public boolean hasReachedBottomStmt() {
        return reachedBottomStmt;
    }

    public void setReachedBottomStmt(boolean b) {
        reachedBottomStmt = b;
    }

}
