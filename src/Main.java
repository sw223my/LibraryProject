import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        //ILibraryStore store = new FileLibraryStore("myfilename.txt");
        ILibraryStore store = new InMemoryLibraryStore(); //InMemoryLibraryStore ist för DbLibraryStore (tog bort/ändrade från ILibraryStore store = new DbLibraryStore());
        seedData(store); //exempeldata

        LibraryService svc = new LibraryService(store);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Library System!");
        System.out.println("Testanvändare: 4128, 5001, 7001, 9001");
        System.out.print("Enter your user id: ");
        String userId = scanner.nextLine();

        Member m = store.getMember(userId);
        if (m == null) {
            System.out.println("Okänd användare. Testa t.ex. 4128.");
            scanner.close();
            return;
        }
        System.out.println("Hej " + m.firstName + "!");

        boolean done = false;

        while (!done) {
            System.out.println("\nMenu:");
            System.out.println("1. Lend item");
            System.out.println("2. Return item");
            System.out.println("3. Check loan time");
            System.out.println("4. Exit");
            System.out.print("Select (1-4): ");

            int selection;
                try {
                    selection = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number.");
                    continue;
                }

            switch (selection) {
                case 1: {
                    System.out.print("Enter book ISBN: ");
                    String bookId = scanner.nextLine();

                    svc.borrow(bookId, userId);
                    break;
                }

                case 2: {
                    System.out.print("Enter book ISBN: ");
                    String bookId = scanner.nextLine();
                    System.out.println("Book is returned...(inte implementerad än)");
                    break;
                }

                case 3: {
                    System.out.println("Loan time... (inte implementerad än)");
                    break;
                }

                case 4: {
                    done = true;
                    break;
                }

                default:
                    System.out.println(selection + " is not valid.");
            }
        }

        System.out.println("Bye.");
        scanner.close();
    }

    private static void seedData(ILibraryStore store) {
        //10 books (exempeldata)
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

        //Members
        store.addMember(new Member("4128", "Lisa", "Student", 1));   // undergrad
        store.addMember(new Member("5001", "Adam", "Master", 2));    // master
        store.addMember(new Member("7001", "Daniel", "PhD", 3));     // phd
        store.addMember(new Member("9001", "Eva", "Teacher", 4));    // teacher
    }
}