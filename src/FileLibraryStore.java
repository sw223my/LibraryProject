import java.util.ArrayList;
import java.util.List;

public class FileLibraryStore implements ILibraryStore {

    String fileName;

    public FileLibraryStore(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void addBook(Book newBook) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMember(Member newMember) {
        // TODO Auto-generated method stub

    }

    @Override
    public Book getBook(String id) {
        System.out.println("FileLibraryStore::getBook()");
        return null;
    }

    @Override
    public Member getMember(String id) {
        System.out.println("FileLibraryStore::getMember()");
        return null;
    }

    @Override
    public boolean isSuspendedMember(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeMember(String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void suspendMember(String id) {
        // TODO Auto-generated method stub

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
