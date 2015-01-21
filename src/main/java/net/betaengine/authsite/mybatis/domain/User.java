package net.betaengine.authsite.mybatis.domain;

import com.google.common.base.MoreObjects;

public class User {
    private Integer id;
    private String username;
    private String password;
    private String fullName;
    private String email;

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String password) {
        if (!password.startsWith("CRYPT:")) {
            throw new IllegalArgumentException("non-CRYPT passwords are not allowed");
        }
        this.password = password;
    }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("password", password)
                .add("fullName", fullName)
                .add("email", email)
                .toString();
    }
}
