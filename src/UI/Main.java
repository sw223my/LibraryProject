package UI;

import Database.DbLibraryStore;
import Database.ILibraryStore;
import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.Membership;
import Processing.LibraryService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/library_system?sslMode=DISABLED&serverTimezone=UTC";
        String user = "root";
        String password = "Fotboll_0533!";

        ILibraryStore store = new DbLibraryStore(url, user, password);
        LibraryService svc = new LibraryService(store);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Library Lending Management System");
        System.out.println("Logged in as librarian.");

        boolean done = false;

        while (!done) {
            printMenu();
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
                case 3 -> handleShowLoans(scanner, svc, store);
                case 4 -> handleCheckBook(scanner, svc, store);
                case 5 -> handleAddBook(scanner, svc);
                case 6 -> handleDeleteBook(scanner, svc);
                case 7 -> handleRegisterMember(scanner, svc);
                case 8 -> handleDeleteMember(scanner, svc);
                case 9 -> handleSuspendMember(scanner, svc);
                case 0 -> done = true;
                default -> System.out.println("Invalid menu choice.");
            }
        }

        System.out.println("Bye.");
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("Menu:");
        System.out.println("1. Lend item");
        System.out.println("2. Return item");
        System.out.println("3. Show member loans");
        System.out.println("4. Check book status");
        System.out.println("5. Add book");
        System.out.println("6. Delete book");
        System.out.println("7. Register member");
        System.out.println("8. Delete member");
        System.out.println("9. Suspend member");
        System.out.println("0. Exit");
        System.out.print("Select: ");
    }

    private static void handleLendBook(Scanner scanner, LibraryService svc) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        System.out.print("Enter book ISBN: ");
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

    private static void handleReturnBook(Scanner scanner, LibraryService svc) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        LibraryService.ReturnResult result = svc.returnBook(memberId, isbn);

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

    private static void handleShowLoans(Scanner scanner, LibraryService svc, ILibraryStore store) {
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
            BookTitle book = isbn == null ? null : svc.getBookTitle(isbn);
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

    private static void handleCheckBook(Scanner scanner, LibraryService svc, ILibraryStore store) {
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

    private static void handleAddBook(Scanner scanner, LibraryService svc) {
        try {
            System.out.print("Enter ISBN: ");
            String isbn = scanner.nextLine();

            System.out.print("Enter title: ");
            String title = scanner.nextLine();

            System.out.print("Enter author: ");
            String author = scanner.nextLine();

            System.out.print("Enter year: ");
            int year = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter number of copies: ");
            int copies = Integer.parseInt(scanner.nextLine());

            svc.addBookTitle(isbn, title, author, year, copies);
            System.out.println("Book added.");
        } catch (Exception e) {
            System.out.println("Could not add book: " + e.getMessage());
        }
    }

    private static void handleDeleteBook(Scanner scanner, LibraryService svc) {
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine();

        boolean ok = svc.deleteBookTitle(isbn);
        System.out.println(ok ? "Book deleted." : "Could not delete book. It may not exist or still have active loans.");
    }

    private static void handleRegisterMember(Scanner scanner, LibraryService svc) {
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
            System.out.println("Member registered or already exists. Member ID: " + memberId);
        } catch (Exception e) {
            System.out.println("Could not register member: " + e.getMessage());
        }
    }

    private static void handleDeleteMember(Scanner scanner, LibraryService svc) {
        int memberId = readMemberId(scanner);
        if (memberId < 0) {
            return;
        }

        boolean ok = svc.deleteMember(memberId);
        System.out.println(ok ? "Member deleted." : "Could not delete member. Member may not exist or still has active loans.");
    }

    private static void handleSuspendMember(Scanner scanner, LibraryService svc) {
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
        String[] knownIsbns = {
                "238103", "111111", "222222", "333333", "444444",
                "555555", "666666", "777777", "888888", "999999"
        };

        for (String isbn : knownIsbns) {
            BookTitle title = store.getBookTitle(isbn);
            if (title == null) {
                continue;
            }

            for (BookCopy copy : store.getBookCopies(isbn)) {
                if (copy.copyId == copyId) {
                    return copy.isbn;
                }
            }
        }
        return null;
    }
}