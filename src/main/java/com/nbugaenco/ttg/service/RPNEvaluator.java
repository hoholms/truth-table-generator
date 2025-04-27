package com.nbugaenco.ttg.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.nbugaenco.ttg.model.Associativity;
import com.nbugaenco.ttg.model.LogicalVariable;
import com.nbugaenco.ttg.model.Operator;

/**
 * The RPNEvaluator is a utility class for evaluating logical expressions provided in
 * Reverse Polish Notation (RPN) and extracting variables from a tokenized expression.
 * <p>
 * Includes logic to build intermediate expression strings with minimal parentheses,
 * respecting operator precedence and associativity.
 */
public class RPNEvaluator {

  public static final String PARENTHESIZED_EXPRESSION_FORMAT = "(%s)";
  public static final String SPACES_AROUND_FORMAT            = " %s ";

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
   * Checks if the provided token is a valid variable (single uppercase character).
   *
   * @param token
   *     the token to be checked
   *
   * @return true if token is a single uppercase character, false otherwise
   */
  private static boolean isVariable(String token) {
    return token != null && token.length() == 1 && Character.isUpperCase(token.charAt(0));
  }

  /**
   * Evaluates a logical expression written in Reverse Polish Notation.
   * <p>
   * <b>IMPORTANT: Assumes the input {@code variableValues} list contains {@link LogicalVariable}
   * instances created using the
   * {@link LogicalVariable#LogicalVariable(String, boolean) new LogicalVariable(String expression, boolean value)}
   * constructor, which assigns maximum precedence internally.</b>
   *
   * @param rpnTokens
   *     the tokens representing the RPN expression
   * @param variableValues
   *     a list of {@link LogicalVariable} objects representing the initial variable states for the current row.
   *     These should have maximum precedence.
   *
   * @return A list containing the initial variables and all intermediate calculation steps as
   * {@link LogicalVariable} objects.
   *
   * @throws IllegalArgumentException
   *     if an unexpected token is encountered, if there are insufficient operands,
   *     or if the final stack state is invalid.
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
   * Pushes variables onto the stack or applies operators to operands from the stack.
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
      // This usually indicates a malformed RPN expression or an issue in processing
      throw new IllegalArgumentException(
          "Evaluation error: stack did not end with a single result. Size: " + operandStack.size());
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
    LogicalVariable variable = variableValues
        .stream()
        .filter(lv -> lv.expression().equals(token))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown variable during evaluation: " + token));

    operandStack.push(variable);
  }

  /**
   * Processes an operator token by applying it to the required number of operands.
   * <p>
   * Pops the required number of operands, applies the operator, builds a new
   * LogicalVariable representing the result (with minimal parentheses),
   * adds it to the calculation path, and pushes it back onto the stack.
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
    Operator op = Optional
        .of(token)
        .map(Operator::fromSymbol)
        .orElseThrow(() -> new IllegalArgumentException("Unknown operator symbol: " + token));

    if (operandStack.size() < op.getArity()) {
      throw new IllegalArgumentException("Insufficient operands for operator: " + op.getSymbol());
    }

    LogicalVariable[] args = new LogicalVariable[op.getArity()];
    for (int i = op.getArity() - 1; i >= 0; i--) {
      if (operandStack.isEmpty()) {
        throw new IllegalArgumentException(
            "Internal evaluation error: Operand stack empty unexpectedly for operator " + op.getSymbol());
      }
      args[i] = operandStack.pop();
    }

    Optional
        .of(args)
        .map(op::apply)
        .map(res -> buildLogicalVariable(op, args, res))
        .filter(calculationPath::add)
        .ifPresent(operandStack::push);
  }

  /**
   * Builds a new LogicalVariable representing the result of an operation.
   * Constructs the expression string intelligently to avoid redundant parentheses
   * based on operator precedence and associativity.
   * <p>
   * Uses {@code getPrecedence()} from {@link LogicalVariable#getPrecedence() LogicalVariable} and
   * {@link Operator#getPrecedence() Operator}.
   * Uses {@code getAssociativity()} from {@link Operator#getAssociativity() Operator}.
   *
   * @param op
   *     The operator being applied.
   * @param args
   *     The operands ({@link LogicalVariable} objects) for the operator.
   * @param result
   *     The boolean result of applying the operator.
   *
   * @return A new {@link LogicalVariable} representing the operation's result.
   */
  private static LogicalVariable buildLogicalVariable(final Operator op, final LogicalVariable[] args,
      final boolean result) {
    StringBuilder expression = new StringBuilder();
    int currentPrecedence = op.getPrecedence(); // Precedence of the current operation
    Associativity currentAssociativity = op.getAssociativity(); // Associativity of the current op

    if (op.getArity() == 1) {
      LogicalVariable operand = args[0];
      String operandExpr = operand.expression();

      // Parenthesize operand if its precedence is lower than the current operator's precedence
      // Example: !(A & B) needs parentheses because & (4) < ! (5).
      // Example: !!A does not need !(!A) because ! (5) is not < ! (5).
      if (operand.getPrecedence() < currentPrecedence) {
        expression.append(op.getSymbol()).append(parenthesizeExpression(operandExpr));
      } else {
        expression.append(op.getSymbol()).append(operandExpr);
      }
    } else if (op.getArity() == 2) {
      LogicalVariable left = args[0];
      LogicalVariable right = args[1];
      String leftExpr = left.expression();
      String rightExpr = right.expression();

      /*
       Add parentheses around the left operand if EITHER:
       1. It was formed by an operator with strictly lower precedence.
          Example: (A | B) & C needs parens because | (3) < & (4).
       2. It was formed by an operator with the SAME precedence, AND the current
          operator is RIGHT-associative.
          Example: (A -> B) -> C needs parens because -> (2) == -> (2) and -> is RIGHT-associative.
          Example: A & B & C does not need parens on (A & B) because & (4) is not < & (4).
      */
      if (left.getPrecedence() < currentPrecedence ||
          left.getPrecedence() == currentPrecedence && currentAssociativity == Associativity.RIGHT) {
        leftExpr = parenthesizeExpression(leftExpr);
      }

      /*
       Add parentheses around the right operand if EITHER:
       1. It was formed by an operator with strictly lower precedence.
          Example: A & (B | C) needs parens because | (3) < & (4).
       2. It was formed by an operator with the SAME precedence, AND the current
          operator is RIGHT-associative.
          Example: A -> (B -> C) will add parens because -> (2) == -> (2) and -> is RIGHT-associative.
                   This is the only case where this method can add parens which were not entered initially.
                   Example: A -> B -> C -> D will become A -> (B -> (C -> D)) to showcase RIGHT-associativity by the
                   Curry-Howard Correspondence.
          Example: A | B | C does not need parens on (B | C) because | is LEFT-associative.
      */
      if (right.getPrecedence() < currentPrecedence ||
          right.getPrecedence() == currentPrecedence && currentAssociativity == Associativity.RIGHT) {
        rightExpr = parenthesizeExpression(rightExpr);
      }

      expression.append(leftExpr).append(SPACES_AROUND_FORMAT.formatted(op.getSymbol())).append(rightExpr);
    }

    return new LogicalVariable(expression.toString(), result, currentPrecedence);
  }

  /**
   * Wraps the given expression in parentheses.
   * <p>
   * This method formats the input expression using a predefined format
   * to ensure it is enclosed in parentheses.
   *
   * @param expression
   *     the expression to be parenthesized
   *
   * @return the parenthesized expression as a string
   */
  private static String parenthesizeExpression(final String expression) {
    return PARENTHESIZED_EXPRESSION_FORMAT.formatted(expression);
  }

}
