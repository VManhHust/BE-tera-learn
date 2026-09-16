package vn.tera.learn.security;

public class JwtClaims {

    private Long userId;
    private String email;
    private String role;
    private String displayName;
    private String status;
    private String tokenType;

    public JwtClaims() {
    }

    public JwtClaims(Long userId, String email, String role, String displayName, String status, String tokenType) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.displayName = displayName;
        this.status = status;
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
