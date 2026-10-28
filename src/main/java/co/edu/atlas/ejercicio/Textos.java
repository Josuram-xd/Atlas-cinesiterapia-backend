package co.edu.atlas.ejercicio;

import java.util.List;

/** Limpieza de texto de entrada: recorta y convierte vacio en null. */
final class Textos {

    private Textos() {}

    static String limpiar(String s) {
        if (s == null) {
            return null;
        }
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }

    static List<String> limpiarLista(List<String> lista) {
        if (lista == null) {
            return List.of();
        }
        return lista.stream().map(Textos::limpiar).filter(x -> x != null).toList();
    }
}
