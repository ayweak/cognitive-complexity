package com.github.ayweak.cognitivecomplexity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

public class CognitiveComplexityCalculator {

    private BufferedTokenStream stream;

    private List<CognitiveComplexity> results;

    private NestingState state;
    private Deque<NestingState> stateStack;
    private Deque<List<NestingState>> statePpBranchStack;
    private CognitiveComplexity complexity;
    private CognitiveComplexity invalidComplexity;
    private LinkedHashMap<Token, PreprocessorCognitiveComplexity> ppComplexities;

    private NestingState ppState;
    private boolean inPpIf;
    private Token currentPp;

    private Map<Token, Token> pairedParens;

    public CognitiveComplexityCalculator(CharStream input) {
        stream = new BufferedTokenStream(new CLexer(input));
    }

    public void calculate() {
        if (results != null) {
            return;
        }

        results = new ArrayList<>();

        state = new NestingState(new HashSet<>(Arrays.asList(
            CLexer.If, CLexer.Else, CLexer.For, CLexer.While, CLexer.Do, CLexer.Switch, CLexer.Question, CLexer.Colon, CLexer.Identifier
        )));
        stateStack = new ArrayDeque<>();
        statePpBranchStack = new ArrayDeque<>();

        ppState = new NestingState(new HashSet<>(Arrays.asList(
            CLexer.PpIf, CLexer.PpIfdef, CLexer.PpIfndef, CLexer.PpElif, CLexer.PpElse, CLexer.PpEndif
        )));
        inPpIf = false;

        pairedParens = new HashMap<>();

        invalidComplexity = createInvalidComplexity();
        complexity = invalidComplexity;
        ppComplexities = new LinkedHashMap<>();

        stream.fill();

        for (int i = 0; i < stream.size(); i++) {
            Token token = stream.get(i);
            switch (token.getChannel()) {
                case CLexer.DEFAULT_TOKEN_CHANNEL: {
                    handleToken(token);
                    break;
                }
                case CLexer.PREPROCESSOR_CHANNEL: {
                    handlePpToken(token);
                    break;
                }
                default: {
                    // wrong
                    break;
                }
            }
        }

        addPreprocessorComplexity();

        if (!state.isEmpty()) {
            // wrong
        }
    }

    public CognitiveComplexity[] getCognitiveComplexities() {
        return results.toArray(new CognitiveComplexity[0]);
    }

    private CognitiveComplexity createInvalidComplexity() {
        CognitiveComplexity c = new CognitiveComplexity();
        c.setFuncName(new CommonToken(CLexer.Identifier, "!"));
        c.setLeftParen(new CommonToken(CLexer.LeftParen));
        c.setRightParen(new CommonToken(CLexer.RightParen));
        c.setLeftBrace(new CommonToken(CLexer.LeftBrace));
        c.setRightBrace(new CommonToken(CLexer.RightBrace));
        c.setComplexity(0);
        return c;
    }

    private void addPreprocessorComplexity() {
        Token[] ppTokens = ppComplexities.keySet().toArray(new Token[0]);
        int ppc = 0;
        int k = 0;
        for (int i = 0; i < ppTokens.length - 1; i++) {
            if (ppTokens[i].getType() == CLexer.PpIf
                || ppTokens[i].getType() == CLexer.PpIfdef
                || ppTokens[i].getType() == CLexer.PpIfndef) {
                ppc += ppComplexities.get(ppTokens[i]).getComplexity();
            } else if (ppTokens[i].getType() == CLexer.PpEndif) {
                ppc -= ppComplexities.get(ppTokens[i]).getComplexity();
            }
            int j;
            for (j = k; j < results.size(); j++) {
                CognitiveComplexity c = results.get(j);
                if (ppTokens[i].getLine() > c.getEndLine()) {
                    continue;
                }
                if (ppTokens[i + 1].getLine() < c.getEndLine()) {
                    break;
                }
                c.addComplexity(ppc);
            }
            if (j == results.size()) {
                break;
            } else {
                k = j;
            }
        }
    }

    private void handleToken(Token token) {
        switch (token.getType()) {
            case CLexer.LeftParen: {
                state.addLast(token);
                break;
            }
            case CLexer.RightParen: {
                removeLastOps();
                Token t = state.removeLast();
                if (t.getType() != CLexer.LeftParen) {
                    // wrong
                }
                pairedParens.put(token, t);
                break;
            }
            case CLexer.LeftBracket: {
                state.addLast(token);
                break;
            }
            case CLexer.RightBracket: {
                removeLastOps();
                Token t = state.removeLast();
                if (t.getType() != CLexer.LeftBracket) {
                    // wrong
                }
                break;
            }
            case CLexer.LeftBrace: {
                Map<Integer, Token> funcDef = findFuncDef(token);
                if (funcDef != null) {
                    if (complexity == invalidComplexity) {
                        complexity = new CognitiveComplexity();
                        complexity.setFuncName(funcDef.get(CLexer.Identifier));
                        complexity.setLeftParen(funcDef.get(CLexer.LeftParen));
                        complexity.setRightParen(funcDef.get(CLexer.RightParen));
                        complexity.setLeftBrace(funcDef.get(CLexer.LeftBrace));
                        complexity.setComplexity(0);
                    } else {
                        state.addLast(funcDef.get(CLexer.Identifier));
                    }
                }
                state.addLast(token);
                break;
            }
            case CLexer.RightBrace: {
                removeLastStmts();
                Token t = state.removeLast();
                if (t.getType() != CLexer.LeftBrace) {
                    // wrong
                }
                if (complexity.matchLeftBrace(t)) {
                    complexity.setRightBrace(token);
                    results.add(complexity);
                    complexity = invalidComplexity;
                }
                removeLastStmt();
                break;
            }
            case CLexer.If: {
                removeLastStmts();
                int type = state.getLast().getType();
                if (type == CLexer.Else) {
                    state.removeLast();
                } else {
                    complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                }
                state.addLast(token);
                break;
            }
            case CLexer.Else: {
                state.setReachedBottomStmt(false);
                complexity.addComplexity(1);
                state.addLast(token);
                break;
            }
            case CLexer.For: {
                removeLastStmts();
                complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                state.addLast(token);
                break;
            }
            case CLexer.While: {
                removeLastStmts();
                complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                state.addLast(token);
                break;
            }
            case CLexer.Do: {
                removeLastStmts();
                state.addLast(token);
                break;
            }
            case CLexer.Switch: {
                removeLastStmts();
                complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                state.addLast(token);
                break;
            }
            case CLexer.Goto: {
                complexity.addComplexity(1);
                break;
            }
            case CLexer.AndAnd: {
                handleBinaryLogicalOp(token);
                break;
            }
            case CLexer.OrOr: {
                handleBinaryLogicalOp(token);
                break;
            }
            case CLexer.Question: {
                removeLastBinaryLogicalOp();
                complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                state.addLast(token);
                break;
            }
            case CLexer.Colon: {
                removeLastBinaryLogicalOp();
                handleCondOpColon(token);
                break;
            }
            case CLexer.Semi: {
                removeLastOps();
                removeLastStmts();
                removeLastStmt();
                break;
            }
            case CLexer.Comma: {
                removeLastOps();
                break;
            }
            default: {
                break;
            }
        }
    }

    private void handleBinaryLogicalOp(Token token) {
        if (!state.isEmpty()) {
            int type = state.getLast().getType();
            if (type == CLexer.OrOr || type == CLexer.AndAnd) {
                if (type == token.getType()) {
                    // do nothing
                } else {
                    state.removeLast();
                    complexity.addComplexity(1);
                    state.addLast(token);
                }
            } else {
                complexity.addComplexity(1);
                state.addLast(token);
            }
        }
    }

    private void handleCondOpColon(Token token) {
        while (!state.isEmpty()) {
            int type = state.getLast().getType();
            if (type == CLexer.Colon) {
                state.removeLast();
            } else if (type == CLexer.Question) {
                state.removeLast();
                state.addLast(token);
                break;
            } else {
                break;
            }
        }
    }

    private void removeLastBinaryLogicalOp() {
        if (!state.isEmpty()) {
            int type = state.getLast().getType();
            if (type == CLexer.AndAnd || type == CLexer.OrOr) {
                state.removeLast();
            }
        }
    }

    private void removeLastOps() {
        while (!state.isEmpty()) {
            int type = state.getLast().getType();
            if (type == CLexer.AndAnd || type == CLexer.OrOr) {
                state.removeLast();
            } else if (type == CLexer.Colon) {
                state.removeLast();
            } else {
                break;
            }
        }
    }

    private void removeLastStmt() {
        if (!state.isEmpty()) {
            switch (state.getLast().getType()) {
                case CLexer.LeftParen: {
                    // do nothing
                    break;
                }
                case CLexer.LeftBrace: {
                    // do nothing
                    break;
                }
                case CLexer.Do: {
                    state.setReachedBottomStmt(true);
                    break;
                }
                default: {
                    state.removeLast();
                    state.setReachedBottomStmt(true);
                    break;
                }
            }
        }
    }

    private void removeLastStmts() {
        if (state.hasReachedBottomStmt()) {
            while (!state.isEmpty()) {
                int type = state.getLast().getType();
                if (type == CLexer.LeftBrace) {
                    break;
                } else if (type == CLexer.Do) {
                    state.removeLast();
                    break;
                } else {
                    state.removeLast();
                }
            }
            state.setReachedBottomStmt(false);
        }
    }

    private Map<Integer, Token> findFuncDef(Token leftBrace) {
        if (leftBrace.getType() != CLexer.LeftBrace) {
            return null;
        }

        Token rightParen = stream.get(leftBrace.getTokenIndex() - 1);
        if (rightParen.getType() != CLexer.RightParen) {
            return null;
        }

        Token leftParen = pairedParens.get(rightParen);
        Token funcName = null;

        Token bar = stream.get(leftParen.getTokenIndex() - 1);
        switch (bar.getType()) {
            case CLexer.Identifier: {
                funcName = bar;
                break;
            }
            case CLexer.RightParen: {
                Token baz = pairedParens.get(bar);
                for (int i = baz.getTokenIndex() + 1; i < bar.getTokenIndex(); i++) {
                    Token t = stream.get(i);
                    if (t.getType() == CLexer.Identifier && t.getChannel() != CLexer.PREPROCESSOR_CHANNEL) {
                        funcName = t;
                        break;
                    }
                }
                break;
            }
            default: {
                break;
            }
        }

        Map <Integer, Token> funcDef = null;
        if (funcName != null) {
            funcDef = new HashMap<>();
            funcDef.put(funcName.getType(), funcName);
            funcDef.put(leftParen.getType(), leftParen);
            funcDef.put(rightParen.getType(), rightParen);
            funcDef.put(leftBrace.getType(), leftBrace);
        }

        return funcDef;
    }

    private void handlePpToken(Token token) {
        switch (token.getType()) {
            case CLexer.PpIf: {
                if (complexity == invalidComplexity) {
                    PreprocessorCognitiveComplexity c = new PreprocessorCognitiveComplexity();
                    c.setIf(token);
                    c.setComplexity(0);
                    c.addComplexity(ppState.getNestingLevel() + 1);
                    ppComplexities.put(token, c);
                } else {
                    complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                }
                ppState.addLast(token);
                stateStack.addLast(new NestingState(state));
                statePpBranchStack.addLast(new ArrayList<NestingState>());
                inPpIf = true;
                currentPp = token;
                break;
            }
            case CLexer.PpIfdef: {
                if (complexity == invalidComplexity) {
                    PreprocessorCognitiveComplexity c = new PreprocessorCognitiveComplexity();
                    c.setIf(token);
                    c.setComplexity(0);
                    c.addComplexity(ppState.getNestingLevel() + 1);
                    ppComplexities.put(token, c);
                } else {
                    complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                }
                ppState.addLast(token);
                stateStack.addLast(new NestingState(state));
                statePpBranchStack.addLast(new ArrayList<NestingState>());
                currentPp = token;
                break;
            }
            case CLexer.PpIfndef: {
                if (complexity == invalidComplexity) {
                    PreprocessorCognitiveComplexity c = new PreprocessorCognitiveComplexity();
                    c.setIf(token);
                    c.setComplexity(0);
                    c.addComplexity(ppState.getNestingLevel() + 1);
                    ppComplexities.put(token, c);
                } else {
                    complexity.addComplexity(state.getNestingLevel() + ppState.getNestingLevel() + 1);
                }
                ppState.addLast(token);
                stateStack.addLast(new NestingState(state));
                statePpBranchStack.addLast(new ArrayList<NestingState>());
                currentPp = token;
                break;
            }
            case CLexer.PpElif: {
                Token t = ppState.removeLast();
                if (complexity == invalidComplexity) {
                    PreprocessorCognitiveComplexity c = ppComplexities.get(t);
                    c.addComplexity(1);
                    ppComplexities.put(token, c);
                } else {
                    complexity.addComplexity(1);
                }
                ppState.addLast(token);
                statePpBranchStack.getLast().add(state);
                state = new NestingState(stateStack.getLast());
                inPpIf = true;
                currentPp = token;
                break;
            }
            case CLexer.PpElse: {
                Token t = ppState.removeLast();
                if (complexity == invalidComplexity) {
                    PreprocessorCognitiveComplexity c = ppComplexities.get(t);
                    c.addComplexity(1);
                    ppComplexities.put(token, c);
                } else {
                    complexity.addComplexity(1);
                }
                ppState.addLast(token);
                statePpBranchStack.getLast().add(state);
                state = new NestingState(stateStack.getLast());
                currentPp = token;
                break;
            }
            case CLexer.PpEndif: {
                Token t = ppState.removeLast();
                if (complexity == invalidComplexity) {
                    PreprocessorCognitiveComplexity c = ppComplexities.get(t);
                    ppComplexities.put(token, c);
                }
                for (NestingState s : statePpBranchStack.removeLast()) {
                    if (s.getNestingLevel() > state.getNestingLevel()) {
                        state = s;
                    }
                }
                stateStack.removeLast();
                currentPp = token;
                break;
            }
            case CLexer.LeftParen: {
                if (inPpIf) {
                    ppState.addLast(token);
                }
                break;
            }
            case CLexer.RightParen: {
                if (inPpIf) {
                    Token t = ppState.removeLast();
                    if (t.getType() == CLexer.AndAnd || t.getType() == CLexer.OrOr) {
                        t = ppState.removeLast();
                    }
                    if (t.getType() != CLexer.LeftParen) {
                        // wrong
                    }
                }
                break;
            }
            case CLexer.AndAnd: {
                if (inPpIf) {
                    int type = ppState.getLast().getType();
                    if (type == CLexer.AndAnd) {
                        // do nothing
                    } else if (type == CLexer.OrOr) {
                        ppState.removeLast();
                        if (complexity == invalidComplexity) {
                            ppComplexities.get(currentPp).addComplexity(1);
                        } else {
                            complexity.addComplexity(1);
                        }
                        ppState.addLast(token);
                    } else {
                        if (complexity == invalidComplexity) {
                            ppComplexities.get(currentPp).addComplexity(1);
                        } else {
                            complexity.addComplexity(1);
                        }
                        ppState.addLast(token);
                    }
                }
                break;
            }
            case CLexer.OrOr: {
                if (inPpIf) {
                    int type = ppState.getLast().getType();
                    if (type == CLexer.AndAnd) {
                        ppState.removeLast();
                        if (complexity == invalidComplexity) {
                            ppComplexities.get(currentPp).addComplexity(1);
                        } else {
                            complexity.addComplexity(1);
                        }
                        ppState.addLast(token);
                    } else if (type == CLexer.OrOr) {
                        // do nothing
                    } else {
                        if (complexity == invalidComplexity) {
                            ppComplexities.get(currentPp).addComplexity(1);
                        } else {
                            complexity.addComplexity(1);
                        }
                        ppState.addLast(token);
                    }
                }
                break;
            }
            case CLexer.Newline: {
                if (!ppState.isEmpty()) {
                    int type = ppState.getLast().getType();
                    if (type == CLexer.AndAnd || type == CLexer.OrOr) {
                        ppState.removeLast();
                    }
                }
                inPpIf = false;
                break;
            }
            default: {
                break;
            }
        }
    }

}
