package harvestLog.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}
