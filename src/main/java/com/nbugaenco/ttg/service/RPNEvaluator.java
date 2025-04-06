package com.nbugaenco.ttg.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.nbugaenco.ttg.model.LogicalVariable;
import com.nbugaenco.ttg.model.Operator;

/**
 * The RPNEvaluator is a utility class for evaluating logical expressions provided in
 * Reverse Polish Notation (RPN) and extracting variables from a tokenized expression.
 * <p>
 * This class should not be instantiated.
 * </p>
 */
public class RPNEvaluator {

  /**
   * Private constructor to prevent instantiation.
   */
  private RPNEvaluator() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Extracts unique variable names from the provided list of tokens.
   *
   * @param tokens
   *     the list of tokens obtained from a logical expression
   *
   * @return a set of variable names appearing in the expression
   */
  public static Set<String> extractVariables(List<String> tokens) {
    return tokens.stream().filter(RPNEvaluator::isVariable).collect(Collectors.toSet());
  }

  /**
   * Checks if the provided token is a valid variable.
   *
   * @param token
   *     the token to be checked
   *
   * @return true if token is a single uppercase character, false otherwise
   */
  private static boolean isVariable(String token) {
    return token.length() == 1 && Character.isUpperCase(token.charAt(0));
  }

  /**
   * Evaluates a logical expression written in Reverse Polish Notation.
   * <p>
   * The method uses a stack-based approach to compute the result of the expression,
   * tracking the evaluation path by appending intermediate results.
   * </p>
   *
   * @param rpnTokens
   *     the tokens representing the RPN expression
   * @param variableValues
   *     the list of LogicalVariable objects holding variable values
   *
   * @return a list of LogicalVariable objects detailing the evaluation process and final result
   *
   * @throws IllegalArgumentException
   *     if an unexpected token is encountered or operand count is insufficient
   */
  public static List<LogicalVariable> evaluate(List<String> rpnTokens, List<LogicalVariable> variableValues) {
    Deque<LogicalVariable> operandStack = new ArrayDeque<>();
    List<LogicalVariable> calculationPath = new ArrayList<>(variableValues);

    rpnTokens.forEach(token -> processToken(token, operandStack, variableValues, calculationPath));

    validateFinalStack(operandStack);

    return calculationPath;
  }

  /**
   * Processes each token during the evaluation of the RPN expression.
   *
   * @param token
   *     the token to process (either a variable or an operator)
   * @param operandStack
   *     the stack containing intermediate LogicalVariable results
   * @param variableValues
   *     the list of LogicalVariable objects with initial variable assignments
   * @param calculationPath
   *     the list where the evaluation path is recorded
   *
   * @throws IllegalArgumentException
   *     if token is unrecognized or operand count for an operator is insufficient
   */
  private static void processToken(String token, Deque<LogicalVariable> operandStack,
      List<LogicalVariable> variableValues, final List<LogicalVariable> calculationPath) {
    if (isVariable(token)) {
      handleVariableToken(token, operandStack, variableValues);
    } else if (Operator.isOperatorSymbol(token)) {
      handleOperatorToken(token, operandStack, calculationPath);
    } else {
      throw new IllegalArgumentException("Unexpected token in RPN evaluation: " + token);
    }
  }

  /**
   * Validates that the operand stack contains exactly one result after evaluation.
   *
   * @param operandStack
   *     the stack containing evaluation results
   *
   * @throws IllegalArgumentException
   *     if the stack does not contain exactly one LogicalVariable result
   */
  private static void validateFinalStack(Deque<LogicalVariable> operandStack) {
    if (operandStack.size() != 1) {
      throw new IllegalArgumentException("Evaluation error: stack did not end with a single result.");
    }
  }

  /**
   * Processes a variable token by verifying its existence.
   *
   * @param token
   *     the variable token to process
   * @param operandStack
   *     the current stack of operands
   * @param variableValues
   *     the list of known LogicalVariable assignments
   *
   * @throws IllegalArgumentException
   *     if the variable is not part of the provided assignments
   */
  private static void handleVariableToken(String token, Deque<LogicalVariable> operandStack,
      List<LogicalVariable> variableValues) {
    if (variableValues.stream().map(LogicalVariable::expression).noneMatch(token::equals)) {
      throw new IllegalArgumentException("Unknown variable during evaluation: " + token);
    }

    operandStack.push(variableValues
        .stream()
        .filter(logicalVariable -> logicalVariable.expression().equals(token))
        .findFirst()
        .orElse(null));
  }

  /**
   * Processes an operator token by applying it to the required number of operands.
   *
   * @param token
   *     the operator token to process
   * @param operandStack
   *     the stack containing operands
   * @param calculationPath
   *     the list recording the steps of evaluation
   *
   * @throws IllegalArgumentException
   *     if insufficient operands are available for the operator
   */
  private static void handleOperatorToken(String token, Deque<LogicalVariable> operandStack,
      final List<LogicalVariable> calculationPath) {
    Operator op = Operator.fromSymbol(token);

    if (operandStack.size() < op.getArity()) {
      throw new IllegalArgumentException("Insufficient operands for operator: " + op.getSymbol());
    }

    LogicalVariable[] args = new LogicalVariable[op.getArity()];
    for (int i = op.getArity() - 1; i >= 0; i--) {
      args[i] = operandStack.pop();
    }

    boolean result = op.apply(args);
    final LogicalVariable logicalVariable = buildLogicalVariable(op, args, result);
    calculationPath.add(logicalVariable);
    operandStack.push(logicalVariable);
  }

  /**
   * Constructs a LogicalVariable representing the result of applying an operator to its arguments.
   *
   * @param op
   *     the operator being applied
   * @param args
   *     the array of LogicalVariable operands for the operator
   * @param result
   *     the boolean result from applying the operator
   *
   * @return a new LogicalVariable encapsulating the expression and its evaluated result
   */
  private static LogicalVariable buildLogicalVariable(final Operator op, final LogicalVariable[] args,
      final boolean result) {
    StringBuilder expression = new StringBuilder();

    if (op.getArity() == 1) {
      expression.append("!").append(args[0].expression());
    } else {
      if (args.length > 1) {
        expression.append("(");
      }
      for (int i = 0; i < args.length; i++) {
        expression.append(args[i].expression());
        if (i < args.length - 1) {
          expression.append(" ").append(op.getSymbol()).append(" ");
        }
      }
      if (args.length > 1) {
        expression.append(")");
      }
    }

    return new LogicalVariable(expression.toString(), result);
  }

}
