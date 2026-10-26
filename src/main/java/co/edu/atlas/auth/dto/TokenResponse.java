package co.edu.atlas.auth.dto;

public record TokenResponse(
        String accessToken, String refreshToken, String tokenType, long expiresIn, UsuarioResponse usuario) {

    public static TokenResponse bearer(
            String accessToken, String refreshToken, long expiresIn, UsuarioResponse usuario) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, usuario);
    }
}
