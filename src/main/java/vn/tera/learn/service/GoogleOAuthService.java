package vn.tera.learn.service;

public interface GoogleOAuthService {
    String buildAuthorizationUrl();

    GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode);

    boolean isValidState(String state);

    class GoogleUserInfo {

        private String email;
        private String name;
        private String googleId;

        public GoogleUserInfo() {
        }

        public GoogleUserInfo(String email, String name, String googleId) {
            this.email = email;
            this.name = name;
            this.googleId = googleId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGoogleId() {
            return googleId;
        }

        public void setGoogleId(String googleId) {
            this.googleId = googleId;
        }
    }
}
