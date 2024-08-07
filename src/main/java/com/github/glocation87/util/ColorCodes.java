package com.github.glocation87.util;

public class ColorCodes {
    public static String getColorCode(String color) {
        switch (color.toUpperCase()) {
            case "RED": return "c";
            case "CYAN": return "b";
            case "PURPLE": return "5";
            default: return "f";
        }
    }
}
