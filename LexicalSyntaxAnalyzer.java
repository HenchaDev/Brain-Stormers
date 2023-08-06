import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexicalAnalyzer {
    // Reserved words for "if-else" type of conditional branch statement
    private static final List<String> reservedWordsIfElse = Arrays.asList("if", "else");

    // Operators used in Java
    private static final String[] arithmeticOperators = { "+", "-", "*", "/" };
    private static final String[] relationalOperators = { "==", "!=", ">", "<", ">=", "<=" };
    private static final String[] logicalOperators = { "&&", "||", "!" };
    private static final String[] assignmentOperators = { "=" };

    // Acceptable characters for variables and constants
    private static final String acceptableChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";

    // Method to test the validity of identifiers
    private static boolean isValidIdentifier(String input) {
        if (input.isEmpty() || !Character.isJavaIdentifierStart(input.charAt(0)))
            return false;

        for (int i = 1; i < input.length(); i++) {
            if (!Character.isJavaIdentifierPart(input.charAt(i)))
                return false;
        }

        return true;
    }

    // Method to perform lexical analysis for a given statement
    private static List<String> lexicalAnalysis(String statement) {
        List<String> tokens = new ArrayList<>();

        // Split the statement into tokens based on whitespace and operators
        String[] words = statement.split("\\s+|(?<=[\\+\\-*/=<>!&|])|(?=[\\+\\-*/=<>!&|])");

        // Process each word and classify them into different categories
        for (String word : words) {
            // Check for reserved words
            if (reservedWordsIfElse.contains(word)) {
                tokens.add("RESERVED_WORD_IF_ELSE");
            }
            // Check for operators
            else if (Arrays.asList(arithmeticOperators).contains(word)) {
                tokens.add("ARITHMETIC_OP_" + word);
            } else if (Arrays.asList(relationalOperators).contains(word)) {
                tokens.add("RELATIONAL_OP_" + word);
            } else if (Arrays.asList(logicalOperators).contains(word)) {
                tokens.add("LOGICAL_OP_" + word);
            } else if (Arrays.asList(assignmentOperators).contains(word)) {
                tokens.add("ASSIGN_OP_" + word);
            }
            // Check for identifiers and constants
            else if (isValidIdentifier(word)) {
                tokens.add("IDENTIFIER");
            } else if (word.matches("-?\\d+(\\.\\d+)?")) { // Regular expression for numeric constants
                tokens.add("CONSTANT");
            }
            // If the word doesn't match any category, treat it as UNKNOWN
            else {
                tokens.add("UNKNOWN");
            }
        }

        return tokens;
    }

    public static void main(String[] args) {
        String inputFile = "input.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> tokens = lexicalAnalysis(line);
                System.out.println("Line: " + line);
                System.out.println("Tokens: " + tokens);
                System.out.println("-------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
