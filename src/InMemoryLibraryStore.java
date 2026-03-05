import java.util.HashMap;

public class InMemoryLibraryStore implements ILibraryStore {

    private final HashMap<String, Book> books = new HashMap<>();
    private final HashMap<String, Member> members = new HashMap<>();

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
        // If service calls this, it should pass "today". But interface doesn't.
        // We'll interpret: suspended if suspendedUntil != null
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
        // Service should set suspendedUntil properly later. Keep this minimal.
        m.suspendedUntil = new java.util.Date();
    }
}