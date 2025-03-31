package com.nbugaenco.ttg.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.nbugaenco.ttg.model.Associativity;
import com.nbugaenco.ttg.model.Operator;

/**
 * The ShuntingYardParser class provides a method to parse a list of tokens
 * representing a logical expression into Reverse Polish Notation (RPN) using
 * the Shunting Yard algorithm.
 */
public class ShuntingYardParser {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private ShuntingYardParser() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Parses a list of tokens into Reverse Polish Notation (RPN) using the Shunting Yard algorithm.
   *
   * @param tokens
   *     the list of tokens to parse
   *
   * @return a list of tokens in RPN order
   *
   * @throws IllegalArgumentException
   *     if an unexpected token is encountered or if there are mismatched parentheses
   */
  public static List<String> parse(List<String> tokens) {
    List<String> outputQueue = new ArrayList<>();
    Deque<String> operatorStack = new ArrayDeque<>();

    for (String token : tokens) {
      if (isVariable(token)) {
        outputQueue.add(token);
      } else if (Operator.isOperatorSymbol(token)) {
        handleOperatorToken(token, outputQueue, operatorStack);
      } else if ("(".equals(token)) {
        operatorStack.push(token);
      } else if (")".equals(token)) {
        handleRightParenthesis(outputQueue, operatorStack);
      } else {
        throw new IllegalArgumentException("Unexpected token: " + token);
      }
    }

    drainOperatorStack(outputQueue, operatorStack);
    return outputQueue;
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
   * Handles an operator token, managing the operator stack and output queue.
   *
   * @param token
   *     the operator token to handle
   * @param outputQueue
   *     the output queue to which tokens are added
   * @param operatorStack
   *     the stack of operators
   */
  private static void handleOperatorToken(String token, List<String> outputQueue, Deque<String> operatorStack) {
    Operator currentOp = Operator.fromSymbol(token);
    while (!operatorStack.isEmpty() && Operator.isOperatorSymbol(operatorStack.peek())) {
      Operator topOp = Operator.fromSymbol(operatorStack.peek());
      if (topOp.getPrecedence() > currentOp.getPrecedence() ||
          (topOp.getPrecedence() == currentOp.getPrecedence() && topOp.getAssociativity() == Associativity.LEFT)) {
        outputQueue.add(operatorStack.pop());
      } else {
        break;
      }
    }
    operatorStack.push(token);
  }

  /**
   * Handles a right parenthesis token, managing the operator stack and output queue.
   *
   * @param outputQueue
   *     the output queue to which tokens are added
   * @param operatorStack
   *     the stack of operators
   *
   * @throws IllegalArgumentException
   *     if there are mismatched parentheses
   */
  private static void handleRightParenthesis(List<String> outputQueue, Deque<String> operatorStack) {
    while (!operatorStack.isEmpty() && !"(".equals(operatorStack.peek())) {
      outputQueue.add(operatorStack.pop());
    }
    if (operatorStack.isEmpty()) {
      throw new IllegalArgumentException("Mismatched parentheses: no matching '(' found.");
    }
    operatorStack.pop();
  }

  /**
   * Drains the operator stack, adding remaining operators to the output queue.
   *
   * @param outputQueue
   *     the output queue to which tokens are added
   * @param operatorStack
   *     the stack of operators
   *
   * @throws IllegalArgumentException
   *     if there are mismatched parentheses
   * @throws IllegalStateException
   *     if a non-operator is found in the stack
   */
  private static void drainOperatorStack(List<String> outputQueue, Deque<String> operatorStack) {
    while (!operatorStack.isEmpty()) {
      String top = operatorStack.pop();
      if ("(".equals(top)) {
        throw new IllegalArgumentException("Mismatched parentheses: '(' was not closed.");
      }
      if (!Operator.isOperatorSymbol(top)) {
        throw new IllegalStateException("Non-operator found in stack: " + top);
      }
      outputQueue.add(top);
    }
  }

}
