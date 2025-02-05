package src.main.java.backend.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomUtils {

    private static final String NUMBERS = "0123456789";

    private static final String LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz";

    private static final String UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String OTHER_CHARS = "!@#$%-+?";

    private static final String ALL_CHARS = NUMBERS + LOWER_CHARS + UPPER_CHARS + OTHER_CHARS;

    private static final String ALL_CHARS_WITHOUT_OTHER = NUMBERS + LOWER_CHARS + UPPER_CHARS;

    public static String genRandom(int len) {
        return genRandom(len, false);
    }
    
    public static String genRandom(int len, boolean hasSpec) {
        StringBuilder sb = new StringBuilder();

        List<String> prefixlist = Arrays.asList("0", "1", "2");
        Collections.shuffle(prefixlist);

        for (int i = 0; i < len; i++) {
            if (prefixlist.size() > i) {
                String prefix = prefixlist.get(i);
                switch (prefix) {
                    case "0":
                        sb.append(NUMBERS.charAt((int) (Math.random() * NUMBERS.length())));
                        break;
                    case "1":
                        sb.append(LOWER_CHARS.charAt((int) (Math.random() * LOWER_CHARS.length())));
                        break;
                    case "2":
                        sb.append(UPPER_CHARS.charAt((int) (Math.random() * UPPER_CHARS.length())));
                        break;
                }
            } else {
                if (hasSpec) {
                    sb.append(ALL_CHARS.charAt((int) (Math.random() * ALL_CHARS.length())));
                } else {
                    sb.append(ALL_CHARS_WITHOUT_OTHER.charAt((int) (Math.random() * ALL_CHARS_WITHOUT_OTHER.length())));
                }

            }
        }
        List<Character> charList = sb.toString().chars().mapToObj(x -> Character.valueOf((char) x)).collect(Collectors.toList());
        Collections.shuffle(charList);
        String result = charList.stream().map(Object::toString).collect(Collectors.joining());
        return result;
    }
    
    public static String genRandomNumbers(int len) {
        return generateRandomString(NUMBERS, len);
    }

    public static String genRandomLowerCase(int len) {
        return generateRandomString(LOWER_CHARS, len);
    }

    public static String genRandomUpperCase(int len) {
        return generateRandomString(UPPER_CHARS, len);
    }

    public static String genRandomAlphabetic(int len) {
        return generateRandomString(LOWER_CHARS + UPPER_CHARS, len);
    }

    private static String generateRandomString(String chars, int len) {
        return IntStream.range(0, len)
                .mapToObj(i -> String.valueOf(chars.charAt((int) (Math.random() * chars.length()))))
                .collect(Collectors.joining());
    }

}
