package com.chatterbox.api_rest.util;

import java.util.List;

public class ValidacionUtils {

    private ValidacionUtils() {
    }

    public static boolean camposValidos(List<String> atributos) {
        return atributos.stream()
                .allMatch(atributo -> atributo != null && !atributo.isBlank());
    }
}
