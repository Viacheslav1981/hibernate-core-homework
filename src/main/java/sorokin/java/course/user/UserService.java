package sorokin.java.course.user;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.helper.TransactionHelper;

import java.util.*;

@Component
public class UserService {

    private final Map<Integer, User> userMap;
    private final Set<String> takenLogins;
    private final AccountService accountService;
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public UserService(AccountService accountService, SessionFactory sessionFactory, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
        this.userMap = new HashMap<>();
        this.takenLogins = new HashSet<>();
        this.accountService = accountService;
    }

    public User createUser(String login) {

        String normalizedLogin = validateLogin(login);
        if (checkDoubleLogin(normalizedLogin)) {
            throw new IllegalArgumentException("Login: " + normalizedLogin + " already exists!");
        }
        return transactionHelper.executeTransaction(session -> {
            User user = new User(normalizedLogin, new ArrayList<>());
            var defaultAccount = accountService.createAccount(user);
            user.getAccountList().add(defaultAccount);
            session.persist(user);
            return user;
        });
    }

    public User findUserById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("user id must be > 0");
        }
        return transactionHelper.executeTransaction(session -> {
            User user = session.find(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("No such user with id=%s".formatted(id));
            }
            return user;
        });
    }


    public List<User> findAll() {
        return transactionHelper.executeTransaction(session ->
                session.createQuery(
                        "SELECT u FROM User u LEFT JOIN FETCH u.accounts",
                        User.class
                ).getResultList()
        );
    }

    public boolean checkDoubleLogin(String login) {
        return transactionHelper.executeTransaction(session -> {
            User user = session.createQuery(
                            "SELECT u FROM User u WHERE u.login = :login",
                            User.class
                    )
                    .setParameter("login", login)
                    .uniqueResult();
            return user != null;
        });
    }

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }
}

