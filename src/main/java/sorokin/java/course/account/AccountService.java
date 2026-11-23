package sorokin.java.course.account;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.helper.TransactionHelper;
import sorokin.java.course.user.User;

import java.util.Comparator;
import java.util.Optional;

@Component
public class AccountService {

    private final AccountProperties accountProperties;
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public AccountService(AccountProperties accountProperties, SessionFactory sessionFactory, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
        this.accountProperties = accountProperties;
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        return transactionHelper.executeTransaction(session -> {
            Account newAccount = new Account(user, accountProperties.getDefaultAmount());
            session.persist(newAccount);
            return newAccount;
        });
    }

    public Optional<Account> findAccountById(Integer id) {
        validatePositiveId(id, "account id");
        return transactionHelper.executeTransaction(session -> {
            Account account = session.find(Account.class, id);
            return Optional.ofNullable(account);
        });
    }

    public void withdraw(Integer fromAccountId, Integer amount) {
        validatePositiveId(fromAccountId, "account id");
        validatePositiveAmount(amount);
        transactionHelper.executeTransaction(session -> {
            Account account = findAccountById(fromAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId)));

            if (amount > account.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                                .formatted(account.getId(), account.getMoneyAmount(), amount)
                );
            }
            account.setMoneyAmount(account.getMoneyAmount() - amount);
            session.persist(account);
            return account;
        });

    }

    public void deposit(Integer toAccountId, Integer amount) {
        validatePositiveId(toAccountId, "account id");
        validatePositiveAmount(amount);
        transactionHelper.executeTransaction(session -> {
            Account account = findAccountById(toAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(toAccountId)));

            account.setMoneyAmount(account.getMoneyAmount() + amount);
            session.persist(account);
            return account;
        });
    }

    public Account closeAccount(Integer accountId) {
        validatePositiveId(accountId, "account id");
        return transactionHelper.executeTransaction(session -> {
            Account accountToClose = findAccountById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(accountId)));
            var userId = accountToClose.getUser().getId();
            User user = session.find(User.class, userId);
            var userAccounts = user.getAccountList();
            if (userAccounts.size() == 1) {
                throw new IllegalStateException("Can't close the only one account");
            }
            var accountToTransferMoney = userAccounts.stream()
                    .filter(it -> it.getId() != accountId)
                    .min(Comparator.comparingInt(Account::getId))
                    .orElseThrow();

            var newAmount = Math.addExact(accountToTransferMoney.getMoneyAmount(), accountToClose.getMoneyAmount());
            accountToTransferMoney.setMoneyAmount(newAmount);
            userAccounts.remove(accountToClose);
            return accountToClose;
        });
    }

    public void transfer(int fromAccountId, int toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("source and target account id must be different");
        }
        transactionHelper.executeTransaction(session -> {
            Account accountFrom = findAccountById(fromAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId)));
            Account accountTo = findAccountById(toAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("No such account: id=%s".formatted(toAccountId)));

            if (amount > accountFrom.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted transfer=%s"
                                .formatted(accountFrom.getId(), accountFrom.getMoneyAmount(), amount)
                );
            }
            int amountToTransfer = accountTo.getUser().getId() == accountFrom.getUser().getId()
                    ? amount
                    : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));
            int sourceAfter = Math.subtractExact(accountFrom.getMoneyAmount(), amount);
            int targetAfter = Math.addExact(accountTo.getMoneyAmount(), amountToTransfer);
            accountFrom.setMoneyAmount(sourceAfter);
            accountTo.setMoneyAmount(targetAfter);
            session.persist(accountFrom);
            session.persist(accountTo);
            return accountFrom;

        });

    }

    private void validatePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}
