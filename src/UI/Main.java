package UI;

import Database.ILibraryStore;
import Database.InMemoryLibraryStore;
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
            System.out.println("Member ID: " + membership.memberId);
            System.out.println("Book: " + book.title);
            System.out.println("ISBN: " + book.isbn);
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
        for (BookTitle title : List.of(
                store.getBookTitle("238103"),
                store.getBookTitle("111111"),
                store.getBookTitle("222222"),
                store.getBookTitle("333333"),
                store.getBookTitle("444444"),
                store.getBookTitle("555555"),
                store.getBookTitle("666666"),
                store.getBookTitle("777777"),
                store.getBookTitle("888888"),
                store.getBookTitle("999999")
        )) {
            if (title == null) {
                continue;
            }
            for (BookCopy copy : store.getBookCopies(title.isbn)) {
                if (copy.copyId == copyId) {
                    return copy.isbn;
                }
            }
        }
        return null;
    }

    private static void seedData(ILibraryStore store) {
        store.addBookTitle(new BookTitle("238103", "Clean Code", "Robert C. Martin", 2008));
        store.addBookCopies("238103", 2);
        store.addBookTitle(new BookTitle("111111", "Refactoring", "Martin Fowler", 1999));
        store.addBookCopies("111111", 1);
        store.addBookTitle(new BookTitle("222222", "Effective Java", "Joshua Bloch", 2018));
        store.addBookCopies("222222", 2);
        store.addBookTitle(new BookTitle("333333", "Design Patterns", "GoF", 1994));
        store.addBookCopies("333333", 1);
        store.addBookTitle(new BookTitle("444444", "The Pragmatic Programmer", "Hunt & Thomas", 1999));
        store.addBookCopies("444444", 2);
        store.addBookTitle(new BookTitle("555555", "Introduction to Algorithms", "CLRS", 2009));
        store.addBookCopies("555555", 1);
        store.addBookTitle(new BookTitle("666666", "JUnit in Action", "Petar Tahchiev", 2010));
        store.addBookCopies("666666", 1);
        store.addBookTitle(new BookTitle("777777", "Mockito Cookbook", "Sam Edwards", 2015));
        store.addBookCopies("777777", 1);
        store.addBookTitle(new BookTitle("888888", "Java Concurrency in Practice", "Goetz", 2006));
        store.addBookCopies("888888", 1);
        store.addBookTitle(new BookTitle("999999", "Head First Design Patterns", "Freeman", 2004));
        store.addBookCopies("999999", 2);

        store.addPerson(new Objects.Person("19990101-1111", "Lisa", "Student"));
        store.addMembership(new Membership(4128, "19990101-1111", 1, null, "ACTIVE", 0, 0));

        store.addPerson(new Objects.Person("19980202-2222", "Adam", "Master"));
        store.addMembership(new Membership(5001, "19980202-2222", 2, null, "ACTIVE", 0, 0));

        store.addPerson(new Objects.Person("19970303-3333", "Daniel", "PhD"));
        store.addMembership(new Membership(7001, "19970303-3333", 3, null, "ACTIVE", 0, 0));

        store.addPerson(new Objects.Person("19850404-4444", "Eva", "Teacher"));
        store.addMembership(new Membership(9001, "19850404-4444", 4, null, "ACTIVE", 0, 0));
    }
}
