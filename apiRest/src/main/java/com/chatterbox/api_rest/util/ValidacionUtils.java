package com.chatterbox.api_rest.util;

import java.util.List;

public class ValidacionUtils {

    private ValidacionUtils() {
    }

    // Revisar si lo valido de otra manera
    public static boolean camposValidos(List<String> atributos) {
        return atributos.stream()
                .allMatch(atributo -> atributo != null && !atributo.trim().isEmpty());
    }
}
