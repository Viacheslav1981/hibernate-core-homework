package sorokin.java.course.helper;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class TransactionHelper {

    private final SessionFactory sessionFactory;

    public TransactionHelper(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public <T> T executeTransaction(Function<Session, T> function) {
        Session session = sessionFactory.getCurrentSession();

        Transaction transaction = session.getTransaction();
        boolean isTransactionOwner = !transaction.isActive();

        if (isTransactionOwner) {
            transaction.begin();
        }

        try {
            T result = function.apply(session);
            if (isTransactionOwner) {
                transaction.commit();
            }

            return result;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
