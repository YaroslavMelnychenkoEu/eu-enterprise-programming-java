import java.util.*;

/** ==== Винятки ==== */
class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String msg) { super(msg); }
}

class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String msg) { super(msg); }
}

/** ==== Абстрактний рахунок ==== */
abstract class Account {
    protected final String accountNumber;
    protected double balance;
    protected final String owner;

    public Account(String accountNumber, String owner, double balance) {
        if (accountNumber == null || accountNumber.isBlank())
            throw new IllegalArgumentException("Номер рахунку не може бути порожнім");
        if (owner == null || owner.isBlank())
            throw new IllegalArgumentException("Власник не може бути порожнім");
        if (balance < 0) throw new InvalidAmountException("Баланс не може бути відʼємним");

        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getOwner() { return owner; }

    public void deposit(double amount) {
        if (amount <= 0) throw new InvalidAmountException("Сума депозиту має бути > 0");
        balance += amount;
        System.out.printf("[INFO] На рахунок %s зараховано %.2f. Баланс: %.2f%n",
                accountNumber, amount, balance);
    }

    public abstract void withdraw(double amount);

    @Override
    public String toString() {
        return String.format("Account{%s, owner='%s', balance=%.2f}", accountNumber, owner, balance);
    }
}

/** ==== Накопичувальний рахунок ==== */
class SavingsAccount extends Account {
    private final double interestRate; // у %

    public SavingsAccount(String accountNumber, String owner, double balance, double interestRate) {
        super(accountNumber, owner, balance);
        if (interestRate < 0) throw new InvalidAmountException("Ставка має бути >= 0");
        this.interestRate = interestRate;
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) throw new InvalidAmountException("Сума зняття має бути > 0");
        if (amount > balance) throw new InsufficientFundsException("Недостатньо коштів");
        balance -= amount;
        System.out.printf("[INFO] З рахунку %s знято %.2f. Баланс: %.2f%n",
                accountNumber, amount, balance);
    }

    public void calculateInterest() {
        double interest = balance * interestRate / 100;
        balance += interest;
        System.out.printf("[INFO] Нараховані відсотки %.2f на рахунок %s. Баланс: %.2f%n",
                interest, accountNumber, balance);
    }

    @Override
    public String toString() {
        return String.format("SavingsAccount{%s, owner='%s', balance=%.2f, rate=%.2f%%}",
                accountNumber, owner, balance, interestRate);
    }
}

/** ==== Поточний рахунок (з овердрафтом) ==== */
class CheckingAccount extends Account {
    private final double overdraftLimit;

    public CheckingAccount(String accountNumber, String owner, double balance, double overdraftLimit) {
        super(accountNumber, owner, balance);
        if (overdraftLimit < 0) throw new InvalidAmountException("Ліміт овердрафту має бути >= 0");
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) throw new InvalidAmountException("Сума зняття має бути > 0");
        if (amount > balance + overdraftLimit)
            throw new InsufficientFundsException("Перевищено ліміт овердрафту");
        balance -= amount;
        System.out.printf("[INFO] З рахунку %s знято %.2f. Баланс: %.2f (ліміт %.2f)%n",
                accountNumber, amount, balance, overdraftLimit);
    }

    @Override
    public String toString() {
        return String.format("CheckingAccount{%s, owner='%s', balance=%.2f, overdraft=%.2f}",
                accountNumber, owner, balance, overdraftLimit);
    }
}

/** ==== Банк ==== */
class Bank {
    private final Map<String, Account> accounts = new HashMap<>();

    public void addAccount(Account acc) {
        if (accounts.containsKey(acc.getAccountNumber()))
            throw new IllegalArgumentException("Рахунок з таким номером вже існує");
        accounts.put(acc.getAccountNumber(), acc);
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public void deposit(String accountNumber, double amount) {
        Account acc = getOrThrow(accountNumber);
        acc.deposit(amount);
    }

    public void withdraw(String accountNumber, double amount) {
        Account acc = getOrThrow(accountNumber);
        acc.withdraw(amount);
    }

    public void printAllAccounts() {
        accounts.values().forEach(System.out::println);
    }

    private Account getOrThrow(String accountNumber) {
        Account acc = accounts.get(accountNumber);
        if (acc == null) throw new NoSuchElementException("Рахунок не знайдено");
        return acc;
    }
}

/** ==== Демонстрація ==== */
public class BankDemo {
    public static void main(String[] args) {
        Bank bank = new Bank();

        SavingsAccount sa = new SavingsAccount("SA-1001", "Іван", 1000, 5);
        CheckingAccount ca = new CheckingAccount("CA-2001", "Марія", 500, 300);

        bank.addAccount(sa);
        bank.addAccount(ca);

        bank.printAllAccounts();

        System.out.println("\n--- Операції ---");
        bank.deposit("SA-1001", 200);
        bank.withdraw("CA-2001", 700); // з овердрафтом

        sa.calculateInterest();

        // Демонстрація винятків
        try {
            bank.withdraw("CA-2001", 2000);
        } catch (InsufficientFundsException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        try {
            bank.deposit("SA-1001", -50);
        } catch (InvalidAmountException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }

        System.out.println("\n--- Фінальний стан рахунків ---");
        bank.printAllAccounts();
    }
}