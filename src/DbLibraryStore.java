import java.util.ArrayList;
import java.util.List;

public class DbLibraryStore implements ILibraryStore {

    @Override
    public void addBook(Book newBook) {
        // SQL code here
    }

    @Override
    public void addMember(Member newMember) {
    }

    @Override
    public Book getBook(String id) {
        // SQL code here
        System.out.println("DbLibraryStore::getBook()");
        return new Book();
    }

    @Override
    public Member getMember(String id) {
        System.out.println("DbLibraryStore::getMember()");
        return new Member();
    }

    @Override
    public boolean isSuspendedMember(String id) {

        return false;
    }

    @Override
    public void removeMember(String id) {
    }

    @Override
    public void suspendMember(String id) {
    }

    @Override
    public void addLoan(Loan loan) {
    }

    @Override
    public Loan getActiveLoan(String memberId, String isbn) {
        return null;
    }

    @Override
    public List<Loan> getLoansForMember(String memberId) {
        return new ArrayList<>();
    }
}
