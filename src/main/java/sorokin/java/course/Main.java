package sorokin.java.course;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sorokin.java.course.config.HibernateConfiguration;
import sorokin.java.course.console.OperationsConsoleListener;
import sorokin.java.course.user.User;

public class Main {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("sorokin.java.course")) {
            OperationsConsoleListener consoleListener = context.getBean(OperationsConsoleListener.class);
            consoleListener.runBank();

            SessionFactory sessionFactory = context.getBean(SessionFactory.class);
//            Session session = sessionFactory.openSession();
//            session.beginTransaction();
//
//            User user = new User();
//            user.setLogin("admin");
//
//            session.persist(user);
//            session.getTransaction().commit();
        }







    }
}
