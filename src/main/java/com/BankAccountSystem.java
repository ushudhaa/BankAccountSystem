package com;

import java.util.*;

abstract class Account {
    // ENCAPSULATION: fields are private, accessed only through methods
    private final String accountNumber;
    private final String ownerName;
    private double balance;
    private final List<String> transactionHistory = new ArrayList<>();

    public Account(String accountNumber, String ownerName, double initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        log("Account opened with balance $" + initialBalance);
    }

    // Controlled read access (encapsulation)
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }

    // Controlled write access with validation (encapsulation)
    protected void setBalance(double newBalance) {
        this.balance = newBalance;
    }

    protected void log(String message) {
        transactionHistory.add(message);
    }

    public void printHistory() {
        System.out.println("--- Transaction history for " + accountNumber + " ---");
        transactionHistory.forEach(System.out::println);
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return;
        }
        setBalance(getBalance() + amount);
        log("Deposited $" + amount + " | Balance: $" + getBalance());
    }

    // Abstract methods - each subclass MUST provide its own implementation (ABSTRACTION)
    public abstract boolean withdraw(double amount);
    public abstract double calculateInterest();
    public abstract String getAccountType();

    @Override
    public String toString() {
        return String.format("[%s] %s | Owner: %s | Balance: $%.2f",
                getAccountType(), accountNumber, ownerName, balance);
    }
}

// INHERITANCE + POLYMORPHISM
class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.04; // 4% annual
    private static final double MIN_BALANCE = 500;

    public SavingsAccount(String accountNumber, String ownerName, double initialBalance) {
        super(accountNumber, ownerName, initialBalance);
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return false;
        }
        if (getBalance() - amount < MIN_BALANCE) {
            System.out.println("Withdrawal denied: savings accounts must keep a minimum balance of $" + MIN_BALANCE);
            return false;
        }
        setBalance(getBalance() - amount);
        log("Withdrew $" + amount + " | Balance: $" + getBalance());
        return true;
    }

    @Override
    public double calculateInterest() {
        double interest = getBalance() * INTEREST_RATE;
        log("Interest calculated: $" + interest);
        return interest;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}

class CurrentAccount extends Account {
    private double overdraftLimit;

    public CurrentAccount(String accountNumber, String ownerName, double initialBalance, double overdraftLimit) {
        super(accountNumber, ownerName, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return false;
        }
        if (getBalance() - amount < -overdraftLimit) {
            System.out.println("Withdrawal denied: overdraft limit of $" + overdraftLimit + " exceeded.");
            return false;
        }
        setBalance(getBalance() - amount);
        log("Withdrew $" + amount + " | Balance: $" + getBalance());
        return true;
    }

    @Override
    public double calculateInterest() {
        // Current accounts typically earn no interest
        return 0.0;
    }

    @Override
    public String getAccountType() {
        return "Current";
    }
}

//Bank class managing accounts polymorphically
class Bank {
    private List<Account> accounts = new ArrayList<>();

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public Account findAccount(String accountNumber) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(accountNumber)) {
                return acc;
            }
        }
        return null;
    }

    // POLYMORPHISM in action: same method call, different behavior per account type
    public void applyMonthlyInterestToAll() {
        System.out.println("=== Applying Monthly Interest ===");
        for (Account acc : accounts) {
            double interest = acc.calculateInterest() / 12; // monthly portion
            if (interest > 0) {
                acc.deposit(interest);
                System.out.printf("%s: interest $%.2f applied%n", acc.getAccountNumber(), interest);
            } else {
                System.out.println(acc.getAccountNumber() + ": no interest for " + acc.getAccountType() + " accounts");
            }
        }
    }

    public void printAllAccounts() {
        System.out.println("=== All Bank Accounts ===");
        accounts.forEach(System.out::println);
    }

    public double totalBankAssets() {
        double total = 0;
        for (Account acc : accounts) {
            total += acc.getBalance();
        }
        return total;
    }
}

public class BankAccountSystem {
    public static void main(String[] args) {
        Bank bank = new Bank();

        Account savings = new SavingsAccount("SA-001", "Alice", 2000);
        Account current = new CurrentAccount("CA-002", "Bob", 500, 1000);

        bank.addAccount(savings);
        bank.addAccount(current);

        bank.printAllAccounts();

        System.out.println("\n--- Transactions ---");
        savings.deposit(300);
        savings.withdraw(2200); // should be denied (min balance rule)
        savings.withdraw(1500); // should succeed

        current.withdraw(1300); // uses overdraft, should succeed
        current.withdraw(500);  // should be denied (exceeds overdraft)

        System.out.println();
        bank.printAllAccounts();

        System.out.println();
        bank.applyMonthlyInterestToAll();

        System.out.println();
        bank.printAllAccounts();

        System.out.printf("%nTotal bank assets: $%.2f%n", bank.totalBankAssets());

        System.out.println();
        savings.printHistory();
        current.printHistory();
    }
}