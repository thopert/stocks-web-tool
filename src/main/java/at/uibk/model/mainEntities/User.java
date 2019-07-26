package at.uibk.model.mainEntities;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name="user")
public class User implements Serializable {
    private static final long serialVersionUID = 7119069490351388531L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Email(message = "Keine korrekte E-Mail-Adresse!")
    @NotBlank(message = "E-Mail-Adresse leer!")
    private String email;
    @NotBlank(message = "Passwort leer!")
    @Size(min = 7, message = "Passwort zu kurz!")
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @NotBlank(message = "Vorname leer!")
    @Size(max = 25)
    private String firstName;
    @NotBlank(message = "Nachname leer!")
    @Size(max = 25)
    private String lastName;
    private Long lastPortfolioId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public Long getLastPortfolioId() {
        return lastPortfolioId;
    }

    public void setLastPortfolioId(Long lastPortfolioId) {
        this.lastPortfolioId = lastPortfolioId;
    }

    public boolean hasLastPortfolioId(){
        return this.lastPortfolioId != null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", lastPortfolioId=" + lastPortfolioId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
