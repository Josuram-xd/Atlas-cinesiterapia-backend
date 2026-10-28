package co.edu.atlas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(@NotBlank(message = "El refresh token es obligatorio") @Size(max = 100) String refreshToken) {}
