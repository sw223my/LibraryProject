package UI;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;
import Processing.*;
import Database.*;

public class Main {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        ILibraryStore store = new InMemoryLibraryStore();
        seedData(store);

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
                case 1:
                    handleLendBook(scanner, svc);
                    break;
                case 2:
                    handleReturnBook(scanner, svc);
                    break;
                case 3:
                    handleShowLoans(scanner, svc);
                    break;
                case 4:
                    handleCheckBook(scanner, svc);
                    break;
                case 5:
                    handleAddBook(scanner, svc);
                    break;
                case 6:
                    handleDeleteBook(scanner, svc);
                    break;
                case 7:
                    handleRegisterMember(scanner, svc);
                    break;
                case 8:
                    handleDeleteMember(scanner, svc);
                    break;
                case 9:
                    handleSuspendMember(scanner, svc);
                    break;
                case 0:
                    done = true;
                    break;
                default:
                    System.out.println("Invalid menu choice.");
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
        System.out.print("Enter member ID: ");
        String memberId = scanner.nextLine();

        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        boolean ok = svc.lendBook(memberId, isbn);
        if (ok) {
            Book book = svc.getBook(isbn);
            Member member = svc.getMember(memberId);

            System.out.println("Processing.Loan successful.");
            System.out.println("Processing.Member: " + member.fullName() + " (" + member.id + ")");
            System.out.println("Processing.Book: " + book.title);
            System.out.println("ISBN: " + book.ISBN);
            System.out.println("Processing.Loan period: 15 days");
        } else {
            System.out.println("Could not lend book. Check member status, loan limit, ISBN, or availability.");
        }
    }

    private static void handleReturnBook(Scanner scanner, LibraryService svc) {
        System.out.print("Enter member ID: ");
        String memberId = scanner.nextLine();

        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        LibraryService.ReturnResult result = svc.returnBook(memberId, isbn);

        if (!result.success) {
            System.out.println(result.message);
            return;
        }

        System.out.println("Processing.Book returned successfully.");

        if (!result.late) {
            System.out.println("Returned in time. Thank you.");
        } else {
            System.out.println("Processing.Book was returned late.");

            if (result.suspendedUntil != null) {
                System.out.println("Processing.Member suspended until: " + DATE_FORMAT.format(result.suspendedUntil));
            }

            if (result.memberDeleted) {
                System.out.println("Processing.Member account deleted because suspension count exceeded 2.");
            }
        }
    }

    private static void handleShowLoans(Scanner scanner, LibraryService svc) {
        System.out.print("Enter member ID: ");
        String memberId = scanner.nextLine();

        Member member = svc.getMember(memberId);
        if (member == null) {
            System.out.println("Processing.Member not found.");
            return;
        }

        List<Loan> loans = svc.getLoansForMember(memberId);

        System.out.println("Loans for " + member.fullName() + " (" + member.id + "):");

        if (loans.isEmpty()) {
            System.out.println("No loans found.");
            return;
        }

        for (Loan loan : loans) {
            Book book = svc.getBook(loan.isbn);
            String title = (book == null) ? "Unknown title" : book.title;

            System.out.println("---------------------------------");
            System.out.println("Processing.Loan ID: " + loan.loanId);
            System.out.println("Processing.Book: " + title);
            System.out.println("ISBN: " + loan.isbn);
            System.out.println("Processing.Loan date: " + DATE_FORMAT.format(loan.loanDate));
            System.out.println("Due date: " + DATE_FORMAT.format(loan.dueDate));
            System.out.println("Status: " + (loan.isActive() ? "Active" : "Returned"));
            if (loan.returnDate != null) {
                System.out.println("Return date: " + DATE_FORMAT.format(loan.returnDate));
            }
        }
    }

    private static void handleCheckBook(Scanner scanner, LibraryService svc) {
        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        Book book = svc.getBook(isbn);
        if (book == null) {
            System.out.println("Processing.Book not found.");
            return;
        }

        System.out.println("Title: " + book.title);
        System.out.println("Author: " + book.author);
        System.out.println("ISBN: " + book.ISBN);
        System.out.println("Year: " + book.year);
        System.out.println("Total copies: " + book.totalCopies);
        System.out.println("Available copies: " + book.availableCopies);
        System.out.println("Status: " + (book.isAvailable() ? "Available" : "Not available"));
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
            System.out.println("Processing.Book added.");
        } catch (Exception e) {
            System.out.println("Could not add book: " + e.getMessage());
        }
    }

    private static void handleDeleteBook(Scanner scanner, LibraryService svc) {
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine();

        boolean ok = svc.deleteBook(isbn);
        System.out.println(ok ? "Processing.Book deleted." : "Could not delete book. It may not exist or still have active loans.");
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
            System.out.println("Processing.Member registered or already exists. Processing.Member ID: " + memberId);
        } catch (Exception e) {
            System.out.println("Could not register member: " + e.getMessage());
        }
    }

    private static void handleDeleteMember(Scanner scanner, LibraryService svc) {
        System.out.print("Enter member ID: ");
        String memberId = scanner.nextLine();

        boolean ok = svc.deleteMember(memberId);
        System.out.println(ok ? "Processing.Member deleted." : "Could not delete member. Processing.Member may not exist or still has active loans.");
    }

    private static void handleSuspendMember(Scanner scanner, LibraryService svc) {
        System.out.print("Enter member ID: ");
        String memberId = scanner.nextLine();

        System.out.print("Enter number of suspension days: ");
        int days;
        try {
            days = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }

        boolean ok = svc.suspendMember(memberId, days);
        System.out.println(ok ? "Processing.Member suspended." : "Could not suspend member.");
    }

    private static void seedData(ILibraryStore store) {
        store.addBook(new Book("238103", "Clean Code", "Robert C. Martin", 2008, 2));
        store.addBook(new Book("111111", "Refactoring", "Martin Fowler", 1999, 1));
        store.addBook(new Book("222222", "Effective Java", "Joshua Bloch", 2018, 2));
        store.addBook(new Book("333333", "Design Patterns", "GoF", 1994, 1));
        store.addBook(new Book("444444", "The Pragmatic Programmer", "Hunt & Thomas", 1999, 2));
        store.addBook(new Book("555555", "Introduction to Algorithms", "CLRS", 2009, 1));
        store.addBook(new Book("666666", "JUnit in Action", "Petar Tahchiev", 2010, 1));
        store.addBook(new Book("777777", "Mockito Cookbook", "Sam Edwards", 2015, 1));
        store.addBook(new Book("888888", "Java Concurrency in Practice", "Goetz", 2006, 1));
        store.addBook(new Book("999999", "Head First Design Patterns", "Freeman", 2004, 2));

        store.addMember(new Member("4128", "Lisa", "Student", "19990101-1111", 1));
        store.addMember(new Member("5001", "Adam", "Master", "19980202-2222", 2));
        store.addMember(new Member("7001", "Daniel", "PhD", "19970303-3333", 3));
        store.addMember(new Member("9001", "Eva", "Teacher", "19850404-4444", 4));
    }
}