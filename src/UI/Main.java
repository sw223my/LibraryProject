package UI;

import Database.DbLibraryStore;
import Database.ILibraryStore;
import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.Membership;
import Processing.ILibraryService;
import Processing.LibraryService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        Properties props = loadDatabaseProperties();

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        ILibraryStore store = new DbLibraryStore(url, user, password);
        ILibraryService svc = new LibraryService(store);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Library Lending Management System");

        boolean done = false;

        while (!done) {
            printMainMenu();
            String input = scanner.nextLine();

            int selection;
            try {
                selection = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (selection) {
                case 1 -> handleLendBook(scanner, svc);
                case 2 -> handleReturnBook(scanner, svc);
                case 3 -> manageLoans(scanner, svc, store);
                case 4 -> manageMembers(scanner, svc);
                case 5 -> manageBooks(scanner, svc);
                case 0 -> done = true;
                default -> System.out.println("Invalid menu choice.");
            }
        }

        System.out.println("Bye.");
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("Main menu:");
        System.out.println("1. Loan book");
        System.out.println("2. Return book");
        System.out.println("3. Manage loans");
        System.out.println("4. Manage members");
        System.out.println("5. Manage books");
        System.out.println("0. Exit");
        System.out.print("Select: ");
    }

    private static void manageLoans(Scanner scanner, ILibraryService svc, ILibraryStore store) {
        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("Manage loans:");
            System.out.println("1. Show member loans");
            System.out.println("2. Check book status");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String input = scanner.nextLine();
            int selection;

            try {
                selection = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (selection) {
                case 1 -> handleShowLoans(scanner, svc, store);
                case 2 -> handleCheckBook(scanner, svc, store);
                case 0 -> back = true;
                default -> System.out.println("Invalid menu choice.");
            }
        }
    }

    private static void manageMembers(Scanner scanner, ILibraryService svc) {
        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("Manage members:");
            System.out.println("1. Register member");
            System.out.println("2. Delete member");
            System.out.println("3. Suspend member");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String input = scanner.nextLine();
            int selection;

            try {
                selection = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (selection) {
                case 1 -> handleRegisterMember(scanner, svc);
                case 2 -> handleDeleteMember(scanner, svc);
                case 3 -> handleSuspendMember(scanner, svc);
                case 0 -> back = true;
                default -> System.out.println("Invalid menu choice.");
            }
        }
    }

    private static void handleLendBook(Scanner scanner, ILibraryService svc) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        System.out.print("Enter book title to search: ");
        String titleQuery = scanner.nextLine();

        List<BookTitle> matches = svc.searchBookTitles(titleQuery);
        if (matches.isEmpty()) {
            System.out.println("No books found matching: " + titleQuery);
            return;
        }

        System.out.println("Found " + matches.size() + " result(s):");
        for (BookTitle b : matches) {
            System.out.println("  ISBN: " + b.isbn + " | " + b.title + " by " + b.author + " (" + b.publishYear + ")");
        }

        System.out.print("Enter ISBN to borrow: ");
        String isbn = scanner.nextLine();

        boolean ok = svc.lendBook(memberId, isbn);
        if (ok) {
            BookTitle book = svc.getBookTitle(isbn);
            Membership membership = svc.getMembership(memberId);

            System.out.println("Loan successful.");
            if (membership != null) {
                System.out.println("Member ID: " + membership.memberId);
            }
            if (book != null) {
                System.out.println("Book: " + book.title);
                System.out.println("ISBN: " + book.isbn);
            }
            System.out.println("Loan period: 15 days");
        } else {
            System.out.println("Could not lend book. Check member status, loan limit, ISBN, or availability.");
        }
    }

    private static void handleReturnBook(Scanner scanner, ILibraryService svc) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        LibraryService.ReturnResult result = svc.returnBook(memberId, isbn);

        if (result == null) {
            System.out.println("Could not return book.");
            return;
        }

        if (!result.success) {
            System.out.println(result.message);
            return;
        }

        System.out.println("Book returned successfully.");

        if (!result.late) {
            System.out.println("Returned in time. Thank you.");
        } else {
            System.out.println("Book was returned late.");

            if (result.suspendedUntil != null) {
                System.out.println("Member suspended until: " + DATE_FORMAT.format(result.suspendedUntil));
            }

            if (result.memberDeleted) {
                System.out.println("Member account deleted because suspension count exceeded 2.");
            }
        }
    }

    private static void handleShowLoans(Scanner scanner, ILibraryService svc, ILibraryStore store) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        Membership membership = svc.getMembership(memberId);
        if (membership == null) {
            System.out.println("Member not found.");
            return;
        }

        List<Loan> loans = svc.getLoansForMember(memberId);

        System.out.println("Loans for member " + membership.memberId + ":");

        if (loans.isEmpty()) {
            System.out.println("No loans found.");
            return;
        }

        for (Loan loan : loans) {
            String isbn = findIsbnForCopy(store, loan.copyId);
            BookTitle book = (isbn == null) ? null : svc.getBookTitle(isbn);
            String title = (book == null) ? "Unknown title" : book.title;

            System.out.println("---------------------------------");
            System.out.println("Loan ID: " + loan.loanId);
            System.out.println("Book: " + title);
            System.out.println("ISBN: " + (isbn == null ? "Unknown" : isbn));
            System.out.println("Loan date: " + DATE_FORMAT.format(loan.loanDate));
            System.out.println("Due date: " + DATE_FORMAT.format(loan.dueDate));
            System.out.println("Status: " + (loan.isActive() ? "Active" : "Returned"));

            if (loan.returnDate != null) {
                System.out.println("Return date: " + DATE_FORMAT.format(loan.returnDate));
            }
        }
    }

    private static void handleCheckBook(Scanner scanner, ILibraryService svc, ILibraryStore store) {
        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        BookTitle book = svc.getBookTitle(isbn);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        List<BookCopy> copies = store.getBookCopies(isbn);
        long availableCopies = copies.stream().filter(BookCopy::isAvailable).count();

        System.out.println("Title: " + book.title);
        System.out.println("Author: " + book.author);
        System.out.println("ISBN: " + book.isbn);
        System.out.println("Year: " + book.publishYear);
        System.out.println("Total copies: " + copies.size());
        System.out.println("Available copies: " + availableCopies);
        System.out.println("Status: " + (availableCopies > 0 ? "Available" : "Not available"));
    }

    private static void handleRegisterMember(Scanner scanner, ILibraryService svc) {
        try {
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter last name: ");
            String lastName = scanner.nextLine();

            System.out.print("Enter personal number: ");
            String personalNumber = scanner.nextLine();

            System.out.print("Enter level (1=Undergraduate, 2=Postgraduate, 3=PhD, 4=Teacher): ");
            int level = Integer.parseInt(scanner.nextLine());

            String memberId = svc.registerMember(firstName, lastName, personalNumber, level);

            System.out.println("Registration completed.");
            System.out.println("Member ID: " + memberId);
            System.out.println("Please give this ID to the member.");
        } catch (Exception e) {
            System.out.println("Could not register member: " + e.getMessage());
        }
    }

    private static void handleDeleteMember(Scanner scanner, ILibraryService svc) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        boolean ok = svc.deleteMember(memberId);
        System.out.println(ok
                ? "Member deleted."
                : "Could not delete member. Member may not exist or still has active loans.");
    }

    private static void handleSuspendMember(Scanner scanner, ILibraryService svc) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        System.out.print("Enter number of suspension days: ");
        int days;
        try {
            days = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }

        boolean ok = svc.suspendMember(memberId, days);
        System.out.println(ok ? "Member suspended." : "Could not suspend member.");
    }

    private static void manageBooks(Scanner scanner, ILibraryService svc) {
        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("Manage books:");
            System.out.println("1. Add book title");
            System.out.println("2. Delete book title");
            System.out.println("0. Back");
            System.out.print("Select: ");

            String input = scanner.nextLine();
            int selection;

            try {
                selection = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (selection) {
                case 1 -> handleAddBookTitle(scanner, svc);
                case 2 -> handleDeleteBookTitle(scanner, svc);
                case 0 -> back = true;
                default -> System.out.println("Invalid menu choice.");
            }
        }
    }

    private static void handleAddBookTitle(Scanner scanner, ILibraryService svc) {
        try {
            System.out.print("Enter ISBN (6 digits): ");
            String isbn = scanner.nextLine();

            System.out.print("Enter title: ");
            String title = scanner.nextLine();

            System.out.print("Enter author: ");
            String author = scanner.nextLine();

            System.out.print("Enter publish year: ");
            int year = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter number of copies: ");
            int copies = Integer.parseInt(scanner.nextLine());

            svc.addBookTitle(isbn, title, author, year, copies);
            System.out.println("Book title added successfully.");
            System.out.println("ISBN: " + isbn + " | " + title + " (" + copies + " cop" + (copies == 1 ? "y" : "ies") + ")");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered. Year and copies must be integers.");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add book: " + e.getMessage());
        }
    }

    private static void handleDeleteBookTitle(Scanner scanner, ILibraryService svc) {
        System.out.print("Enter ISBN (6 digits): ");
        String isbn = scanner.nextLine();

        boolean ok = svc.deleteBookTitle(isbn);
        System.out.println(ok
                ? "Book title deleted."
                : "Could not delete book. It may not exist or still has active loans.");
    }

    private static int readMemberId(Scanner scanner) {
        System.out.print("Enter member ID: ");
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid member ID.");
            return -1;
        }
    }

    private static String findIsbnForCopy(ILibraryStore store, int copyId) {
        BookCopy copy = store.getBookCopy(copyId);
        return copy == null ? null : copy.isbn;
    }

    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Could not read db.properties", e);
        }

        return props;
    }
}