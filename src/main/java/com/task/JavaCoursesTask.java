package com.task;

import java.sql.*;
import java.util.*;

public class JavaCoursesTask {
    private static final String url = "jdbc:mysql://localhost:3306/calculate";
    private static final String user = "root";
    private static final String password = "OlegMart7751098!";


    public static void main(String[] args) {
        int action;
        String expression, temp;
        Scanner in = new Scanner(System.in);
        do {
            System.out.print("\nChose the action:\n1 - Entering an expression\n2 - Show database\n3 - Edit records\n4 - Search in DB\n0 - Exit program\n");
            action = in.nextInt();
            temp = in.nextLine();
            switch (action) {
                case 1:
                    System.out.println("Enter an expression (separated by a space, thats look like (2 + 2)): ");
                    expression = in.nextLine();
                    boolean brackets = isValidBrackets(expression),
                            exp = isValidExpression(expression);
                    if (brackets && exp) {
                        double result = evaluate(expression);
                        System.out.printf("Result of your expression: %.2f", result);
                        System.out.println("\nCount of numbers in your expression:  " + countOfNumbers(expression));
                        writeInDb(expression,result);
                    } else if (!brackets)
                        System.out.print("Expression are wrong! (Look om the brackets\n");
                    else if (!exp)
                        System.out.print("Expression are wrong! (Look on the operators)\n");
                    break;
                case 2:
                    System.out.print("Showing data base\n");
                    showDataBase();
                    break;
                case 3:
                    int index;
                    showDataBase();
                    System.out.print("Choose index for edit\n");
                    index = in.nextInt();
                    System.out.println("Enter an expression (separated by a space, thats look like (2 + 2)): ");
                    temp = in.nextLine();
                    expression = in.nextLine();
                    brackets = isValidBrackets(expression);
                    exp = isValidExpression(expression);
                    if (brackets && exp) {
                        double result = evaluate(expression);
                        System.out.printf("Result of your expression: %.2f", result);
                        System.out.println("\nCount of numbers in your expression:  " + countOfNumbers(expression));
                        editRecInDb(index, expression, result);
                    } else if (!brackets)
                        System.out.print("Expression are wrong! (Look om the brackets\n");
                    else if (!exp)
                        System.out.print("Expression are wrong! (Look on the operators)\n");
                    break;
                case 4:
                    String expr;
                    double number;
                    boolean expIsTrue = false;
                    do {
                        System.out.println("Enter the expression (<,>,<=,>=,=): ");
                        expr = in.nextLine();
                        expr = expr.trim();
                        if (!expr.equals("<") && !expr.equals(">") && !expr.equals("<=") && !expr.equals(">=") && !expr.equals("=")) {
                            System.out.print("Invalid expression!\n");
                            expIsTrue = false;
                        } else expIsTrue = true;
                    }while (!expIsTrue);
                    System.out.println("Enter the number: ");
                    number = in.nextDouble();
                    searchInDB(expr, number);
                    break;
                default: System.out.print("Exiting program, thanks for using!\n");
            }
        }while (action != 0);
    }

    public static void writeInDb(String expr, double res){
        String query = "INSERT expressions(expression, result) VALUES (?,?)";
        try (Connection conn = DriverManager.getConnection(url, user, password)){

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, expr);
            preparedStatement.setDouble(2, res);

            preparedStatement.executeUpdate();
            System.out.println("Yours expression was added at DB");
        }catch(Exception ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }
    public static void showDataBase(){
        try (Connection conn = DriverManager.getConnection(url, user, password)){
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM expressions");
            while(resultSet.next()){
                int id = resultSet.getInt(1);
                String expression = resultSet.getString(2);
                double result = resultSet.getDouble(3);
                System.out.printf("%d. %s = %.2f \n", id, expression, result);
            }
        } catch(Exception ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }
    public static void editRecInDb(int index, String expr, double res){
        String query = "UPDATE expressions SET expression = ?, result = ? WHERE idexpressions = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password)){
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, expr);
            preparedStatement.setDouble(2, res);
            preparedStatement.setInt(3, index);
            int rows = preparedStatement.executeUpdate();
            System.out.printf("Id " + index + " edited");
        }catch(Exception ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }
    public static void searchInDB(String expression, double value){
        String query = "SELECT * FROM expressions WHERE result < ?";
        if (expression.equals("="))
            query = "SELECT * FROM expressions WHERE result = ?";
        else if (expression.equals(">"))
            query = "SELECT * FROM expressions WHERE result > ?";
        else if (expression.equals("<="))
            query = "SELECT * FROM expressions WHERE result <= ?";
        else if (expression.equals(">="))
            query = "SELECT * FROM expressions WHERE result >= ?";
        try (Connection conn = DriverManager.getConnection(url, user, password)){
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setDouble(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                int id = resultSet.getInt(1);
                String expr = resultSet.getString(2);
                double res = resultSet.getDouble(3);
                System.out.printf("%d. %s = %.2f \n", id, expr, res);
            }
        }catch(Exception ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }

    public static int countOfNumbers(String expression){
        int count = 0;
        boolean hasNumber = false;
        for (int i = 0; i < expression.length(); i++) {
            if (Character.isDigit(expression.charAt(i))) {
                hasNumber = true;
            }
            if (!Character.isDigit(expression.charAt(i)) && hasNumber) {
                count++;
                hasNumber = false;
            }
        }
        if (hasNumber) {
            count++;
        }
        return count;
    }
    public static boolean isValidBrackets(String expression) {
        HashMap<Character, Character> brackets = new HashMap<>();
        brackets.put(')', '(');
        brackets.put('}', '{');
        brackets.put(']', '[');
        Deque<Character> stack = new LinkedList<>();
        for (char c : expression.toCharArray()) {
            if (brackets.containsValue(c)) {
                stack.push(c);
            } else if (brackets.containsKey(c)) {
                if (stack.isEmpty() || stack.pop() != brackets.get(c)) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }
    public static boolean isValidExpression(String expression){
        expression = expression.replaceAll ("\\ s*","");
        char [] tokens = expression.toCharArray();
        for (int i = 0; i < tokens.length; i++){
            if ((tokens[i] == '+' || tokens[i] == '-') && (tokens[i++] == '/' || tokens[i++] == '*')) {
                return false;
            }
        }
        return true;
    }
    public static Double evaluate(String expression) {

        List<String> strList = new ArrayList<>();
        int countOfNumbers;
        for (String listElement : expression.trim().split(" ")) {
            strList.add(listElement);
            strList.add(" ");
        }
        strList.remove(strList.size() - 1);
        if (strList.indexOf("(") != -1) {

            for (int i = strList.indexOf("(") + 1; i < strList.size() - 1; i++) {
                String recursion = "";
                if (strList.get(i).equals("(")) {
                    for (int j = i; j < strList.lastIndexOf(")"); j++) {
                        recursion += strList.get(j);
                    }
                    String test = expression.substring(expression.indexOf("("), expression.lastIndexOf(")") + 1);
                    String testRecursion = String.valueOf(evaluate(recursion));
                    expression = expression.replace(test, testRecursion);
                    strList.removeAll(strList);
                    for (String newElement : expression.trim().split(" ")) {
                        strList.add(newElement);
                        strList.add(" ");
                    }
                }
                String recursion2 = "";
                if (strList.get(i).equals(")")) {

                    for (int j = strList.indexOf("(") + 1; j < strList.indexOf(")"); j++) {
                        recursion2 += strList.get(j);
                    }
                    String test2 = expression.substring(expression.indexOf("("), expression.lastIndexOf(")") + 1);
                    String testRecursion2 = String.valueOf(evaluate(recursion2));
                    expression = expression.replace(test2, testRecursion2);
                    for (String newElement : expression.trim().split(" ")) {
                        strList.add(newElement);
                        strList.add(" ");
                    }
                }
            }
        }

        List<String> stringList2 = new ArrayList<>();
        for (String element : expression.trim().split(" ")) {
            stringList2.add(element);
        }


        while (stringList2.size() != 0) {
            Double result = 0d;
            if (stringList2.indexOf("/") != -1) {
                int index = stringList2.indexOf("/");
                result = Double.valueOf(stringList2.get(index - 1)) / Double.valueOf(stringList2.get(index + 1));
                stringList2.add(index - 1, String.valueOf(result));
                stringList2.remove(index + 2);
                stringList2.remove(index + 1);
                stringList2.remove(index);
            }
            else if (stringList2.indexOf("*") != -1) {
                int index = stringList2.indexOf("*");
                result = Double.valueOf(stringList2.get(index - 1)) * Double.valueOf(stringList2.get(index + 1));
                stringList2.add(index - 1, String.valueOf(result));
                stringList2.remove(index + 2);
                stringList2.remove(index + 1);
                stringList2.remove(index);
            }
            else if (stringList2.indexOf("-") != -1) {
                int index = stringList2.indexOf("-");
                int lastIndex = stringList2.lastIndexOf("-");
                if (index == 0) {
                    result = 0.0 - Double.valueOf(stringList2.get(index + 1));
                    stringList2.add(0, String.valueOf(result));
                    stringList2.remove(2);
                    stringList2.remove(1);
                }
                else if ((lastIndex-2>0) && (stringList2.get(lastIndex-2).equals("-"))){
                    result = Double.valueOf(stringList2.get(lastIndex + 1)) + Double.valueOf(stringList2.get(lastIndex - 1));
                    stringList2.add(lastIndex - 1, String.valueOf(result));
                    stringList2.remove(lastIndex + 2);
                    stringList2.remove(lastIndex + 1);
                    stringList2.remove(lastIndex);
                }
                else {
                    result = Double.valueOf(stringList2.get(index - 1)) - Double.valueOf(stringList2.get(index + 1));
                    stringList2.add(index - 1, String.valueOf(result));
                    stringList2.remove(index + 2);
                    stringList2.remove(index + 1);
                    stringList2.remove(index);
                }
            }
            else if (stringList2.indexOf("+") != -1) {
                int index = stringList2.indexOf("+");
                result = Double.valueOf(stringList2.get(index - 1)) + Double.valueOf(stringList2.get(index + 1));
                stringList2.add(index - 1, String.valueOf(result));
                stringList2.remove(index + 2);
                stringList2.remove(index + 1);
                stringList2.remove(index);
            }
            if ((stringList2.indexOf("*") == -1) && (stringList2.indexOf("/") == -1) && (stringList2.indexOf("+") == -1) && (stringList2.indexOf("-") == -1)) {
                return result;
            }
        }
        return Double.valueOf(stringList2.get(0));
    }
}
