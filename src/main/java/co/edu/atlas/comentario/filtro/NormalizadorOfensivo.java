package co.edu.atlas.comentario.filtro;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Deteccion de palabras ofensivas tolerante a evasiones habituales: mayusculas, tildes, sustituciones
 * (4 por a, 3 por e, 0 por o, 1 por i, 5 por s, 7 por t, @ por a, $ por s), separadores entre letras
 * ("p.u.t.a", "p u t a", "p-u-t-a"), letras repetidas ("puuuuta") y caracteres invisibles.
 *
 * <p>La coincidencia es por palabra completa (no por subcadena) para no marcar palabras legitimas que contienen
 * una ofensiva dentro ("reputacion"). Las entradas de la lista pueden ser frases de varias palabras.
 */
public final class NormalizadorOfensivo {

    private static final Pattern NO_LETRAS = Pattern.compile("[^a-z]+");
    private static final Pattern RACHA = Pattern.compile("([a-z])\\1{2,}");
    private static final int MAX_RACHA_SUELTAS = 40;
    private static final int MIN_LARGO_PALABRA = 3;

    private NormalizadorOfensivo() {}

    /** Forma comparable de una entrada de la lista: solo letras minusculas separadas por un espacio. */
    public static String normalizarPalabra(String palabra) {
        return String.join(" ", piezas(plegar(palabra)));
    }

    public static boolean contieneOfensiva(String texto, Set<String> lista) {
        if (texto == null || lista.isEmpty()) {
            return false;
        }
        String plegado = plegar(texto);
        List<String> piezas = piezas(plegado);
        if (piezas.isEmpty()) {
            return false;
        }
        // 1) palabra completa
        for (String p : piezas) {
            if (lista.contains(p)) {
                return true;
            }
        }
        // 2) separadores dentro de una palabra: "p.u.t.a", "pu*ta"
        for (String token : plegado.split("\\s+")) {
            String pegado = colapsar(token.replaceAll("[^a-z]", ""));
            if (!pegado.isEmpty() && lista.contains(pegado)) {
                return true;
            }
        }
        // 3) letras sueltas separadas: "p u t a"
        if (contieneEnRachaDeSueltas(piezas, lista)) {
            return true;
        }
        // 4) frases de varias palabras
        String corrido = " " + String.join(" ", piezas) + " ";
        for (String entrada : lista) {
            if (entrada.indexOf(' ') > 0 && corrido.contains(" " + entrada + " ")) {
                return true;
            }
        }
        return false;
    }

    private static boolean contieneEnRachaDeSueltas(List<String> piezas, Set<String> lista) {
        StringBuilder racha = new StringBuilder();
        for (int i = 0; i <= piezas.size(); i++) {
            String p = i < piezas.size() ? piezas.get(i) : "";
            if (p.length() == 1 && racha.length() < MAX_RACHA_SUELTAS) {
                racha.append(p);
                continue;
            }
            if (racha.length() >= MIN_LARGO_PALABRA && subcadenaEnLista(racha.toString(), lista)) {
                return true;
            }
            racha.setLength(0);
            if (p.length() == 1) {
                racha.append(p);
            }
        }
        return false;
    }

    private static boolean subcadenaEnLista(String racha, Set<String> lista) {
        for (int i = 0; i < racha.length(); i++) {
            for (int j = i + MIN_LARGO_PALABRA; j <= racha.length(); j++) {
                if (lista.contains(racha.substring(i, j))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Minusculas, sin tildes ni caracteres invisibles y con las sustituciones aplicadas. */
    private static String plegar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("[\\p{M}\\p{Cf}]+", "");
        StringBuilder sb = new StringBuilder(sinTildes.length());
        for (char c : sinTildes.toLowerCase(Locale.ROOT).toCharArray()) {
            sb.append(switch (c) {
                case '4', '@' -> 'a';
                case '3' -> 'e';
                case '1' -> 'i';
                case '0' -> 'o';
                case '5', '$' -> 's';
                case '7' -> 't';
                default -> c;
            });
        }
        return sb.toString();
    }

    private static List<String> piezas(String plegado) {
        List<String> piezas = new ArrayList<>();
        for (String p : NO_LETRAS.split(plegado)) {
            if (!p.isEmpty()) {
                piezas.add(colapsar(p));
            }
        }
        return piezas;
    }

    /** "puuuuta" -> "puta": tres o mas letras iguales seguidas se reducen a una. */
    private static String colapsar(String palabra) {
        return RACHA.matcher(palabra).replaceAll("$1");
    }
}
