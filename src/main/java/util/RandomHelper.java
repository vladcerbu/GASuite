package util;

import chromosome.action.Action;

import java.util.ArrayList;
import java.util.Random;

public class RandomHelper {
    private final Random random = new Random();
    private final int minNr;
    private final int maxNr;
    private final int maxStringLength;
    private final int stringType;

    public RandomHelper(int minNr, int maxNr, int maxStringLength, int stringType) {
        this.minNr = minNr;
        this.maxNr = maxNr;
        this.maxStringLength = maxStringLength;
        this.stringType = stringType;
    }

    /**
     * @return a random Integer between minNr (inclusive) and maxNr (inclusive)
     */
    public int generateRandomInteger(int minNr, int maxNr) {
        return random.nextInt(maxNr + 1 - minNr) + minNr;
    }

    /**
     * @return a random Integer represented as a String between minNr (inclusive) and maxNr (inclusive)
     */
    public String generateRandomIntegerString(int minNr, int maxNr) {
        return Integer.toString(generateRandomInteger(minNr, maxNr));
    }

    /**
     * @return a random Float between minNr (inclusive) and maxNr (inclusive)
     */
    public float generateRandomFloat(int minNr, int maxNr) {
        return minNr + (maxNr - minNr) * random.nextFloat();
    }

    /**
     * @return a random Float represented as a String between minNr (inclusive) and maxNr (inclusive)
     */
    public String generateRandomFloatString(int minNr, int maxNr) {
        return Float.toString(generateRandomFloat(minNr, maxNr));
    }

    /**
     * @return a random Double between minNr (inclusive) and maxNr (inclusive)
     */
    public double generateRandomDouble(int minNr, int maxNr) {
        return minNr + (maxNr - minNr) * random.nextDouble();
    }

    /**
     * @return a random Double represented as a String between minNr (inclusive) and maxNr (inclusive)
     */
    public String generateRandomDoubleString(int minNr, int maxNr) {
        return Double.toString(generateRandomDouble(minNr, maxNr));
    }

    /**
     * @return a random Boolean
     */
    public boolean generateRandomBoolean() {
        return random.nextBoolean();
    }

    /**
     * @return "true" or "false" at random
     */
    public String generateRandomBooleanString() {
        return generateRandomBoolean() ? "true" : "false";
    }

    /**
     * Generates a String
     * @param maxStringLength The maximum length of the desired String
     * @param stringType The type of the String. 1 = small letters; 2 = 1+big letters; 3 = 2+numbers; 4 = 3+other characters
     * @return a randomly generated String
     */
    public String generateRandomString(int maxStringLength, int stringType) {
        switch (stringType) {
            case 1 -> {
                return generateRandomStringSmall(maxStringLength);
            }
            case 2 -> {
                return generateRandomStringBig(maxStringLength);
            }
            case 3 -> {
                return generateRandomStringNumbers(maxStringLength);
            }
            case 4 -> {
                return generateRandomStringAll(maxStringLength);
            }
            default -> {
                return "";
            }
        }
    }

    /**
     * Get random values according to the given Actions List
     * @param actions List of Actions
     * @return A list with the generated values, represented as Strings
     */
    public ArrayList<String> getRandomValues(ArrayList<Action> actions) {
        ArrayList<String> values = new ArrayList<>();
        for (Action action : actions)
            values.addAll(getRandomValues(action));
        return values;
    }

    /**
     * Get random values according to the given Action
     * @param action The Action
     * @return A list with the generated values, represented as Strings
     */
    public ArrayList<String> getRandomValues(Action action) {
        ArrayList<String> values = new ArrayList<>();
        for (String p : action.getParamTypes()) {
            switch (p) {
                case "int", "Integer" -> values.add(generateRandomIntegerString(minNr, maxNr));
                case "float", "Float" -> values.add(generateRandomFloatString(minNr, maxNr));
                case "double", "Double" -> values.add(generateRandomDoubleString(minNr, maxNr));
                case "boolean", "Boolean" -> values.add(generateRandomBooleanString());
                case "String" -> values.add(generateRandomString(maxStringLength, stringType));
                default -> values.add("null");
            }
        }
        return values;
    }

    /**
     * Generate a random variable name of given length (only small letters)
     * @param length The desired length of the variable
     * @return The generated variable name
     */
    public String generateRandomVarName(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Generates a random String of small letters and of variable size
     * @param maxStringLength The maximum size of the generated String
     * @return The randomly generated String
     */
    public String generateRandomStringSmall(int maxStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        maxStringLength = generateRandomInteger((maxStringLength + 1) / 2, maxStringLength + 1);
        return random.ints(leftLimit, rightLimit + 1)
                .limit(maxStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Generates a random String of small and big letters and of variable size
     * @param maxStringLength The maximum size of the generated String
     * @return The randomly generated String
     */
    public String generateRandomStringBig(int maxStringLength) {
        int leftLimit = 65; // letter 'A'
        int rightLimit = 122; // letter 'z'
        maxStringLength = generateRandomInteger((maxStringLength + 1) / 2, maxStringLength + 1);
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i >= 65 && i <= 90) || i >= 97)
                .limit(maxStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Generates a random String of alphanumeric characters and of variable size
     * @param maxStringLength The maximum size of the generated String
     * @return The randomly generated String
     */
    public String generateRandomStringNumbers(int maxStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        maxStringLength = generateRandomInteger((maxStringLength + 1) / 2, maxStringLength + 1);
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(maxStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Generates a random String of variable size
     * @param maxStringLength The maximum size of the generated String
     * @return The randomly generated String
     */
    public String generateRandomStringAll(int maxStringLength) {
        int leftLimit = 35; // '#'
        int rightLimit = 126; // '~'
        maxStringLength = generateRandomInteger((maxStringLength + 1) / 2, maxStringLength + 1);
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> i != 92) // no '\'
                .limit(maxStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
