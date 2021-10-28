package com.github.ayweak.cognitivecomplexity;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

public class Main {

    public static void main(String[] args) throws Exception {
        CharStream stream = CharStreams.fromFileName(args[0]);
        CognitiveComplexityCalculator calculator = new CognitiveComplexityCalculator(stream);

        calculator.calculate();

        CognitiveComplexity[] complexities = calculator.getCognitiveComplexities();

        if (complexities.length == 0) {
            System.out.println("There are no functions in target file.");
        } else {
            for (CognitiveComplexity c : complexities) {
                System.out.println(c.getFuncName() + ":" + c.getStartLine() + ": " + c.getComplexity());
            }
        }
    }

}