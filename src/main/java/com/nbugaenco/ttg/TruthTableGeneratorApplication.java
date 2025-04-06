package com.nbugaenco.ttg;

import java.util.Scanner;

import com.nbugaenco.ttg.service.TruthTableGenerator;

/**
 * The TruthTableGeneratorApplication class provides a command-line interface
 * for generating a truth table from a logical expression. It uses:
 * <ul>
 *   <li>The Shunting Yard algorithm to parse the expression into Reverse Polish Notation (RPN).</li>
 *   <li>An RPN evaluator to compute truth values for each combination of variable assignments.</li>
 * </ul>
 */
public class TruthTableGeneratorApplication {

  /**
   * Instructions displayed to the user when the program starts.
   */
  public static final String INSTRUCTIONS = """
                                            Enter a logical expression (up to 3 variables A, B, C):
                                            Operators: ! & | / \\ ^ -> <-> ( )
                                              ! (NOT), & (AND), | (OR), ^ (XOR)
                                              / (NAND), \\ (NOR)
                                              -> (Implies), <-> (Equivalent)
                                            Expression:\s""";

  /**
   * Main entry point for the truth table generator application.
   * Reads user input for a logical expression, constructs a TruthTableGenerator,
   * and displays the resulting truth table in the console.
   *
   * @param args
   *     command-line arguments (unused).
   */
  public static void main(String[] args) {
    String inputExpression = "";
    try (Scanner scanner = new Scanner(System.in)) {
      System.out.print(INSTRUCTIONS);
      inputExpression = scanner.nextLine();
    } catch (Exception e) {
      System.err.println("\nAn unexpected error occurred: " + e.getMessage());
      e.printStackTrace();
    }

    if (inputExpression.isBlank()) {
      System.out.println("No expression entered.");
      return;
    }

    try {
      TruthTableGenerator generator = new TruthTableGenerator(inputExpression);
      generator.generateTable();
    } catch (IllegalArgumentException e) {
      System.err.println("\nError: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("\nAn unexpected error occurred: " + e.getMessage());
      e.printStackTrace();
    }
  }

}
