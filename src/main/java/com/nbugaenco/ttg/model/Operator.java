package com.nbugaenco.ttg.model;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Operator enum represents logical operators used in logical expressions.
 * Each operator has a symbol, precedence, associativity, arity, and a boolean operation.
 */
public enum Operator {
  // Unary operator
  NOT("!", 5, Associativity.RIGHT, 1, args -> !args[0].value()),

  // Binary operators (higher precedence -> evaluated earlier)
  AND("&", 4, Associativity.LEFT, 2, args -> args[0].value() && args[1].value()),
  NAND("/", 4, Associativity.LEFT, 2, args -> !(args[0].value() && args[1].value())), // Sheffer stroke

  OR("|", 3, Associativity.LEFT, 2, args -> args[0].value() || args[1].value()),
  XOR("^", 3, Associativity.LEFT, 2, args -> args[0].value() ^ args[1].value()),      // Exclusive OR
  NOR("\\", 3, Associativity.LEFT, 2, args -> !(args[0].value() || args[1].value())), // Peirce's arrow

  IMPLIES("->", 2, Associativity.RIGHT, 2, args -> !args[0].value() ||
      args[1].value()), // Implication (right associativity is important!)

  EQUIV("<->", 1, Associativity.LEFT, 2, args -> args[0].value() == args[1].value()); // Equivalence

  public static final int MAX_OPERATOR_LENGTH = 3;

  // Map for quick lookup of operators by symbol
  private static final Map<String, Operator> symbolMap = Stream
      .of(values())
      .collect(Collectors.toMap(Operator::getSymbol, Function.identity()));

  final String           symbol;
  final int              precedence; // Precedence
  final Associativity    associativity; // Associativity (LEFT or RIGHT)
  final int              arity; // Arity (number of operands: 1 for unary, 2 for binary)
  final BooleanOperation operation; // Lambda for evaluation

  /**
   * Constructs an Operator with the given properties.
   *
   * @param symbol
   *     the symbol representing the operator
   * @param precedence
   *     the precedence of the operator
   * @param associativity
   *     the associativity of the operator (LEFT or RIGHT)
   * @param arity
   *     the arity of the operator (number of operands)
   * @param operation
   *     the boolean operation performed by the operator
   */
  Operator(String symbol, int precedence, Associativity associativity, int arity, BooleanOperation operation) {
    this.symbol = symbol;
    this.precedence = precedence;
    this.associativity = associativity;
    this.arity = arity;
    this.operation = operation;
  }

  /**
   * Returns the Operator corresponding to the given symbol.
   *
   * @param symbol
   *     the symbol of the operator
   *
   * @return the Operator corresponding to the symbol
   */
  public static Operator fromSymbol(String symbol) {
    return symbolMap.get(symbol);
  }

  /**
   * Returns the longest operator symbol that starts with the given prefix.
   * Necessary for parsing multi-character operators like "->".
   *
   * @param text
   *     the text to check for operator symbols
   *
   * @return the longest operator symbol starting with the given prefix, or null if none found
   */
  public static String getLongestOperatorPrefix(String text) {
    for (int len = MAX_OPERATOR_LENGTH; len >= 1; len--) { // Check lengths 3, 2, 1
      if (text.length() >= len) {
        String prefix = text.substring(0, len);
        if (isOperatorSymbol(prefix)) {
          return prefix;
        }
      }
    }

    return null; // No operator found with the given prefix
  }

  /**
   * Checks if the given symbol is an operator symbol.
   *
   * @param symbol
   *     the symbol to check
   *
   * @return true if the symbol is an operator symbol, false otherwise
   */
  public static boolean isOperatorSymbol(String symbol) {
    return symbolMap.containsKey(symbol);
  }

  /**
   * Returns the symbol of the operator.
   *
   * @return the symbol of the operator
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the precedence of the operator.
   *
   * @return the precedence of the operator
   */
  public int getPrecedence() {
    return precedence;
  }

  /**
   * Returns the associativity of the operator.
   *
   * @return the associativity of the operator
   */
  public Associativity getAssociativity() {
    return associativity;
  }

  /**
   * Returns the arity of the operator.
   *
   * @return the arity of the operator
   */
  public int getArity() {
    return arity;
  }

  /**
   * Applies the boolean operation of the operator to the given arguments.
   *
   * @param args
   *     the boolean arguments
   *
   * @return the result of the boolean operation
   *
   * @throws IllegalArgumentException
   *     if the number of arguments does not match the arity of the operator
   */
  public boolean apply(LogicalVariable... args) {
    if (args.length != arity) {
      throw new IllegalArgumentException("Incorrect number of arguments for operator " + symbol);
    }
    return operation.apply(args);
  }

  /**
   * Functional interface for boolean operations.
   */
  @FunctionalInterface
  interface BooleanOperation {

    /**
     * Applies the boolean operation to the given arguments.
     *
     * @param args
     *     the boolean arguments
     *
     * @return the result of the boolean operation
     */
    boolean apply(LogicalVariable... args);

  }
}
