package com.nbugaenco.ttg.service;

import java.util.ArrayList;
import java.util.List;

import com.nbugaenco.ttg.model.Operator;

/**
 * The Tokenizer class provides methods to tokenize a logical expression
 * into a list of string tokens. It handles variables, operators, and parentheses.
 */
public class Tokenizer {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private Tokenizer() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Tokenizes the given logical expression into a list of string tokens.
   *
   * @param expression
   *     the logical expression to tokenize
   *
   * @return a list of string tokens representing the expression
   *
   * @throws IllegalArgumentException
   *     if the expression contains invalid characters or unknown tokens
   */
  public static List<String> tokenize(String expression) {
    List<String> tokens = new ArrayList<>();
    String expr = removeWhitespaceAndRedundantNegation(expression);
    int index = 0;

    while (index < expr.length()) {
      char currentChar = expr.charAt(index);
      if (Character.isLetter(currentChar)) {
        index = handleLetter(expr, tokens, index);
      } else if (currentChar == '(' || currentChar == ')') {
        index = handleParenthesis(expr, tokens, index);
      } else {
        index = handleOperator(expr, tokens, index);
      }
    }

    return tokens;
  }

  /**
   * Removes all whitespace characters and redundant negations from the given expression.
   * <p>
   * This method processes the input logical expression by removing all whitespace
   * characters and replacing double negations ("!!") with an empty string.
   *
   * @param expression
   *     the logical expression to be processed
   *
   * @return the processed expression without whitespace and redundant negations
   */
  private static String removeWhitespaceAndRedundantNegation(String expression) {
    return expression.replaceAll("\\s+", "").replace("!!", "");
  }

  /**
   * Handles a letter character in the expression, adding it as a token.
   *
   * @param expr
   *     the expression being tokenized
   * @param tokens
   *     the list of tokens to which the letter will be added
   * @param index
   *     the current index in the expression
   *
   * @return the updated index after processing the letter
   *
   * @throws IllegalArgumentException
   *     if the letter is not an uppercase character
   */
  private static int handleLetter(String expr, List<String> tokens, int index) {
    char currentChar = expr.charAt(index);
    if (!Character.isUpperCase(currentChar)) {
      throw new IllegalArgumentException(
          "Invalid character: " + currentChar + ". Variables must be uppercase letters.");
    }
    tokens.add(String.valueOf(currentChar));
    return index + 1;
  }

  /**
   * Handles a parenthesis character in the expression, adding it as a token.
   *
   * @param expr
   *     the expression being tokenized
   * @param tokens
   *     the list of tokens to which the parenthesis will be added
   * @param index
   *     the current index in the expression
   *
   * @return the updated index after processing the parenthesis
   */
  private static int handleParenthesis(String expr, List<String> tokens, int index) {
    tokens.add(String.valueOf(expr.charAt(index)));
    return index + 1;
  }

  /**
   * Handles an operator character in the expression, adding it as a token.
   *
   * @param expr
   *     the expression being tokenized
   * @param tokens
   *     the list of tokens to which the operator will be added
   * @param index
   *     the current index in the expression
   *
   * @return the updated index after processing the operator
   *
   * @throws IllegalArgumentException
   *     if the operator is unknown
   */
  private static int handleOperator(String expr, List<String> tokens, int index) {
    char currentChar = expr.charAt(index);
    String longestOp = Operator.getLongestOperatorPrefix(expr.substring(index));
    if (longestOp != null) {
      tokens.add(longestOp);
      return index + longestOp.length();
    }
    throw new IllegalArgumentException("Unknown token starting with: " + currentChar + " at position " + index);
  }

}
