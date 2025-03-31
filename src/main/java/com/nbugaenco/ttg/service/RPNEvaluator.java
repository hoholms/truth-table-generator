package com.nbugaenco.ttg.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.nbugaenco.ttg.model.Operator;

/**
 * The RPNEvaluator class provides methods to evaluate a logical expression
 * in Reverse Polish Notation (RPN) and extract variables from a list of tokens.
 */
public class RPNEvaluator {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private RPNEvaluator() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Extracts variables from a list of tokens.
   *
   * @param tokens
   *     the list of tokens to process
   *
   * @return a set of unique variable names found in the tokens
   */
  public static Set<String> extractVariables(List<String> tokens) {
    return tokens.stream().filter(RPNEvaluator::isVariable).collect(Collectors.toSet());
  }

  /**
   * Checks if a token is a variable.
   *
   * @param token
   *     the token to check
   *
   * @return true if the token is a variable, false otherwise
   */
  private static boolean isVariable(String token) {
    return token.length() == 1 && Character.isUpperCase(token.charAt(0));
  }

  /**
   * Evaluates a logical expression in Reverse Polish Notation (RPN).
   *
   * @param rpnTokens
   *     the list of tokens in RPN order
   * @param variableValues
   *     a map of variable names to their boolean values
   *
   * @return the result of the evaluation as a boolean
   *
   * @throws IllegalArgumentException
   *     if an unexpected token is encountered or if there are insufficient operands
   */
  public static boolean evaluate(List<String> rpnTokens, Map<String, Boolean> variableValues) {
    Deque<Boolean> operandStack = new ArrayDeque<>();

    rpnTokens.forEach(token -> processToken(token, operandStack, variableValues));

    validateFinalStack(operandStack);

    return operandStack.pop();
  }

  /**
   * Processes a token during RPN evaluation.
   *
   * @param token
   *     the token to process
   * @param operandStack
   *     the stack of operands
   * @param variableValues
   *     a map of variable names to their boolean values
   *
   * @throws IllegalArgumentException
   *     if an unexpected token is encountered
   */
  private static void processToken(String token, Deque<Boolean> operandStack, Map<String, Boolean> variableValues) {
    if (isVariable(token)) {
      handleVariableToken(token, operandStack, variableValues);
    } else if (Operator.isOperatorSymbol(token)) {
      handleOperatorToken(token, operandStack);
    } else {
      throw new IllegalArgumentException("Unexpected token in RPN evaluation: " + token);
    }
  }

  /**
   * Validates the final state of the operand stack after RPN evaluation.
   *
   * @param operandStack
   *     the stack of operands
   *
   * @throws IllegalArgumentException
   *     if the stack does not end with a single result
   */
  private static void validateFinalStack(Deque<Boolean> operandStack) {
    if (operandStack.size() != 1) {
      throw new IllegalArgumentException("Evaluation error: stack did not end with a single result.");
    }
  }

  /**
   * Handles a variable token during RPN evaluation.
   *
   * @param token
   *     the variable token to handle
   * @param operandStack
   *     the stack of operands
   * @param variableValues
   *     a map of variable names to their boolean values
   *
   * @throws IllegalArgumentException
   *     if the variable is unknown
   */
  private static void handleVariableToken(String token, Deque<Boolean> operandStack,
      Map<String, Boolean> variableValues) {
    if (!variableValues.containsKey(token)) {
      throw new IllegalArgumentException("Unknown variable during evaluation: " + token);
    }

    operandStack.push(variableValues.get(token));
  }

  /**
   * Handles an operator token during RPN evaluation.
   *
   * @param token
   *     the operator token to handle
   * @param operandStack
   *     the stack of operands
   *
   * @throws IllegalArgumentException
   *     if there are insufficient operands for the operator
   */
  private static void handleOperatorToken(String token, Deque<Boolean> operandStack) {
    Operator op = Operator.fromSymbol(token);

    if (operandStack.size() < op.getArity()) {
      throw new IllegalArgumentException("Insufficient operands for operator: " + op.getSymbol());
    }

    boolean[] args = new boolean[op.getArity()];
    for (int i = op.getArity() - 1; i >= 0; i--) {
      args[i] = operandStack.pop();
    }

    boolean result = op.apply(args);
    operandStack.push(result);
  }

}
