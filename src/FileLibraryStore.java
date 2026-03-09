import java.util.ArrayList;
import java.util.List;

public class FileLibraryStore implements ILibraryStore {

    String fileName;

    public FileLibraryStore(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void addBook(Book newBook) {
    }

    @Override
    public Book getBook(String isbn) {
        return null;
    }

    @Override
    public void removeBook(String isbn) {
    }

    @Override
    public void addMember(Member newMember) {
    }

    @Override
    public Member getMember(String memberId) {
        return null;
    }

    @Override
    public Member getMemberByPersonalNumber(String personalNumber) {
        return null;
    }

    @Override
    public void removeMember(String memberId) {
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

    @Override
    public List<Loan> getLoansForBook(String isbn) {
        return new ArrayList<>();
    }
}