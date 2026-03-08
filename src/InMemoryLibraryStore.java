import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryLibraryStore implements ILibraryStore {

    private final HashMap<String, Book> books = new HashMap<>();
    private final HashMap<String, Member> members = new HashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    @Override
    public void addBook(Book newBook) {
        if (newBook == null || newBook.ISBN == null) return;
        books.put(newBook.ISBN, newBook);
    }

    @Override
    public void addMember(Member newMember) {
        if (newMember == null || newMember.id == null) return;
        members.put(newMember.id, newMember);
    }

    @Override
    public Book getBook(String id) {
        return books.get(id);
    }

    @Override
    public Member getMember(String id) {
        return members.get(id);
    }

    @Override
    public boolean isSuspendedMember(String id) {
        Member m = members.get(id);
        if (m == null) return false;
        return m.suspendedUntil != null;
    }

    @Override
    public void removeMember(String id) {
        members.remove(id);
    }

    @Override
    public void suspendMember(String id) {
        Member m = members.get(id);
        if (m == null) return;
        m.suspendedUntil = new java.util.Date();
    }

    @Override
    public void addLoan(Loan loan) {
        if (loan != null) {
            loans.add(loan);
        }
    }

    @Override
    public Loan getActiveLoan(String memberId, String isbn) {
        for (Loan loan : loans) {
            if (loan.memberId.equals(memberId)
                    && loan.isbn.equals(isbn)
                    && loan.isActive()) {
                return loan;
            }
        }
        return null;
    }

    @Override
    public List<Loan> getLoansForMember(String memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.memberId.equals(memberId)) {
                result.add(loan);
            }
        }
        return result;
    }
}