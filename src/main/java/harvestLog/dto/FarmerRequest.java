package harvestLog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// Optional -
public record FarmerRequest(
        @NotBlank(message = "Name is required")
        String name,
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        List<Long> harvestRecordIds,

        List<Long> cropIds,

        List<Long> fieldIds
) {
}
