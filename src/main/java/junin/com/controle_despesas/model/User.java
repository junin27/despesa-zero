package junin.com.controle_despesas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {

    private Long id;

    private String name;

    private String email;

    private String password;

    @JsonIgnore
    private String confirmPassword;

    public User() {


    }

    public Long getId() {

        return id;

    }

    public void setId(Long id) {

        this.id = id;

    }

    public String getName() {

        return name;

    }

    public void setName(String name) {

        this.name = name;

    }

    public String getEmail() {

        return email;

    }

    public void setEmail(String email) {

        this.email = email;

    }

    public String getPassword() {

        return password;

    }

    public void setPassword(String password) {

        this.password = password;

    }

    public String getConfirmPassword() {

        return confirmPassword;

    }

    public void setConfirmPassword(String confirmPassword) {

        this.confirmPassword = confirmPassword;

    }
}
