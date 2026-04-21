package sk.eventfindr.fsa.domain;

public class User {

    private Long id;
    private String name;
    private UserRole rola;
    private String email;

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

    public UserRole getRola() {
        return rola;
    }

    public void setRola(UserRole rola) {
        this.rola = rola;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
