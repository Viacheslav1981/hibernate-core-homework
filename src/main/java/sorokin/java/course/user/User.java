package sorokin.java.course.user;

import jakarta.persistence.*;
import sorokin.java.course.account.Account;

import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "login")
    private String login;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER,  orphanRemoval = true)
    private List<Account> accounts;

    public User(String login, List<Account> accountList) {
        this.login = login;
        this.accounts = accountList;
    }

    public User() {

    }

    public int getId() {
        return id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public List<Account> getAccountList() {
        return accounts;
    }

    public void setAccountList(List<Account> accountList) {
        this.accounts = accountList;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", accountList=" + accounts +
                '}';
    }
}
