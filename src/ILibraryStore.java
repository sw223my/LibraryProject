import java.util.List;

public interface ILibraryStore {
    public void addBook(Book newBook);
    public void addMember(Member newMember);
    public Book getBook(String id);
    public Member getMember(String id);
    public boolean isSuspendedMember(String id);
    public void removeMember(String id);
    public void suspendMember(String id);
    void addLoan(Loan loan);
    Loan getActiveLoan(String memberId, String isbn);
    List<Loan> getLoansForMember(String memberId);
}
