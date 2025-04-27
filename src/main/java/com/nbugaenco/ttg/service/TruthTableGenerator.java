package com.nbugaenco.ttg.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.freva.asciitable.AsciiTable;
import com.nbugaenco.ttg.model.LogicalVariable;

/**
 * The TruthTableGenerator class generates a truth table for a given logical expression.
 * It tokenizes the input, converts it to Reverse Polish Notation (RPN), and evaluates the
 * expression for every possible combination of logical variable assignments.
 */
public class TruthTableGenerator {

  private final String       expression;
  private final List<String> tokens;
  private final List<String> rpnExpression;
  private final List<String> variables;

  /**
   * Constructs a TruthTableGenerator that parses the provided logical expression.
   * <p>
   * The expression is tokenized, converted to RPN, and variables are identified and sorted.
   * </p>
   *
   * @param expression
   *     the logical expression to process; must be non-null and well-formed
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
   * <p>
   * This method outputs the table header and evaluates the expression for all possible
   * truth assignments, printing the resulting table using ASCII formatting.
   * </p>
   */
  public void generateTable() {
    System.out.printf("%nGenerating Truth Table for: %s%n%n", expression);

    int numVars = variables.size();

    final List<List<LogicalVariable>> result = calculateTable(numVars);

    if (!result.isEmpty()) {
      AsciiTable
          .builder()
          .border(AsciiTable.FANCY_ASCII)
          .header(toHeaders(result))
          .data(toValues(result))
          .writeTo(System.out);
    }
  }

  /**
   * Calculates the truth table for the logical expression.
   * <p>
   * This method generates all possible combinations of logical variable assignments
   * and evaluates the expression for each combination.
   * </p>
   *
   * @param numVars
   *     the number of logical variables in the expression
   *
   * @return a list of lists containing the evaluated logical variables for each combination
   */
  private List<List<LogicalVariable>> calculateTable(final int numVars) {
    final List<List<LogicalVariable>> resultList = new ArrayList<>();

    int numRows = 1 << numVars;
    for (int i = 0; i < numRows; i++) {
      List<LogicalVariable> currentValues = new ArrayList<>();
      for (int j = 0; j < numVars; j++) {
        boolean value = ((i >> (numVars - 1 - j)) & 1) == 1;
        currentValues.add(new LogicalVariable(variables.get(j), value));
      }

      try {
        List<LogicalVariable> result = RPNEvaluator.evaluate(rpnExpression, currentValues);
        resultList.add(result);
      } catch (Exception e) {
        System.err.printf("Eval Error: %s%n", e.getMessage());
      }
    }

    return resultList;
  }

  /**
   * Extracts the headers for the truth table from the evaluated logical variables.
   * <p>
   * This method retrieves the expressions of the logical variables to be used as headers
   * for the truth table.
   * </p>
   *
   * @param result
   *     the list of lists containing the evaluated logical variables
   *
   * @return an array of strings representing the headers of the truth table
   */
  private String[] toHeaders(final List<List<LogicalVariable>> result) {
    return result
        .stream()
        .findFirst()
        .orElseGet(Collections::emptyList)
        .stream()
        .map(LogicalVariable::expression)
        .toArray(String[]::new);
  }

  /**
   * Converts the evaluated logical variables to a 2D array of values.
   * <p>
   * This method maps the boolean values of the logical variables to integers (1 for true, 0 for false)
   * and collects them into a 2D array.
   * </p>
   *
   * @param result
   *     the list of lists containing the evaluated logical variables
   *
   * @return a 2D array of integer values representing the truth table
   */
  private Integer[][] toValues(final List<List<LogicalVariable>> result) {
    return result
        .stream()
        .map(list -> list
            .stream()
            .map(LogicalVariable::value)
            .map(val -> Boolean.TRUE.equals(val) ? 1 : 0)
            .toArray(Integer[]::new))
        .toArray(Integer[][]::new);
  }

}
