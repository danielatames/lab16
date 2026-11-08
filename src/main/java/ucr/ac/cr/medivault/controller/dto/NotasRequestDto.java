package ucr.ac.cr.medivault.controller.dto;

import jakarta.validation.constraints.*;

public record NotasRequestDto(
        @NotBlank(message = "Notas clinicas requeridas.") @Size(min = 10, max = 4000, message = "Notas: 10 a 4000 caracteres.") String notasClinicas) {
}