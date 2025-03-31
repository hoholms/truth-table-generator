package com.nbugaenco.ttg.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The TruthTableGenerator class is responsible for generating a truth table
 * for a given logical expression. It tokenizes the expression, parses it into
 * Reverse Polish Notation (RPN), and evaluates the expression for all possible
 * combinations of variable assignments.
 */
public class TruthTableGenerator {

  private final String       expression;
  private final List<String> tokens;
  private final List<String> rpnExpression;
  private final List<String> variables;

  /**
   * Constructs a new TruthTableGenerator with a given logical expression.
   * Tokenizes the expression, parses it into RPN form, and extracts found variables.
   *
   * @param expression
   *     the logical expression to be processed
   *
   * @throws IllegalArgumentException
   *     if the expression is invalid or contains unexpected tokens
   */
  public TruthTableGenerator(String expression) throws IllegalArgumentException {
    this.expression = expression;
    try {
      this.tokens = Tokenizer.tokenize(expression);
      this.rpnExpression = ShuntingYardParser.parse(this.tokens);
      Set<String> vars = RPNEvaluator.extractVariables(this.tokens);
      this.variables = new ArrayList<>(vars);
      Collections.sort(this.variables);
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new IllegalArgumentException("Invalid expression: " + e.getMessage(), e);
    }
  }

  /**
   * Generates and prints the truth table for the logical expression.
   * It prints the header and iterates over all possible variable assignments
   * to evaluate and print the result for each combination.
   */
  public void generateTable() {
    int numVars = variables.size();

    printHeader(numVars);
    printRows(numVars);
  }

  /**
   * Prints the header of the truth table, including variable names and the expression.
   *
   * @param numVars
   *     the number of variables in the expression
   */
  private void printHeader(int numVars) {
    for (String variable : variables) {
      System.out.printf("%-5s | ", variable);
    }
    System.out.printf("%s%n", expression);
    printSeparator(numVars);
  }

  /**
   * Prints the rows of the truth table, evaluating the expression for each combination
   * of variable assignments.
   *
   * @param numVars
   *     the number of variables in the expression
   */
  private void printRows(int numVars) {
    int numRows = 1 << numVars;
    for (int i = 0; i < numRows; i++) {
      Map<String, Boolean> currentValues = new HashMap<>();
      for (int j = 0; j < numVars; j++) {
        boolean value = ((i >> (numVars - 1 - j)) & 1) == 1;
        currentValues.put(variables.get(j), value);
        System.out.printf("%-5s | ", value ? "1" : "0");
      }

      try {
        boolean result = RPNEvaluator.evaluate(rpnExpression, currentValues);
        System.out.printf("%s%n", result ? "1" : "0");
      } catch (Exception e) {
        System.out.printf("Eval Error: %s%n", e.getMessage());
      }
    }
  }

  /**
   * Prints a separator line for the truth table header.
   *
   * @param numVars
   *     the number of variables in the expression
   */
  private void printSeparator(int numVars) {
    for (int j = 0; j < numVars; j++) {
      System.out.print("------+-");
    }
    int exprWidth = Math.max(expression.length(), 10);
    for (int k = 0; k < exprWidth; k++) {
      System.out.print("-");
    }
    System.out.println();
  }

}
