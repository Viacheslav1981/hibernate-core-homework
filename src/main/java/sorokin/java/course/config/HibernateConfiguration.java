package sorokin.java.course.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;

@Configuration
public class HibernateConfiguration {

    @Bean
    public SessionFactory sessionFactory() {
        org.hibernate.cfg.Configuration configuration =
                new org.hibernate.cfg.Configuration();

        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Account.class);
        configuration.setProperty("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        configuration.setProperty("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/bank");
        configuration.setProperty("jakarta.persistence.jdbc.user", "postgres");
        configuration.setProperty("jakarta.persistence.jdbc.password", "root");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.current_session_context_class", "thread");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        return configuration.buildSessionFactory();
    }
}
