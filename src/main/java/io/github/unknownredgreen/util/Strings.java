package io.github.unknownredgreen.util;

public class Strings {
    public static String cutString(String str, int maxChars) {
        if (str == null) return null;
        if (str.length() <= maxChars) return str;

        maxChars -= 3; //so we have space for ...

        return str.substring(0, maxChars) + "...";
    }
}