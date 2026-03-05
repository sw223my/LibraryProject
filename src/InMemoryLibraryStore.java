import java.util.ArrayList;
import java.util.List;

public class InMemoryLibraryStore implements ILibraryStore {

    private List<Book> books = new ArrayList<>();
    private List<Member> members = new ArrayList<>();

    @Override
    public void addBook(Book newBook) {
        books.add(newBook);
    }

    @Override
    public void addMember(Member newMember) {
        members.add(newMember);
    }

    @Override
    public Book getBook(String id) {
        for (Book b : books) {
            if (b.ISBN.equals(id)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public Member getMember(String id) {
        for (Member m : members) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public boolean isSuspendedMember(String id) {
        Member m = getMember(id);
        if (m == null) return false;
        return m.suspendedUntil != null;
    }

    @Override
    public void removeMember(String id) {
        members.removeIf(m -> m.id.equals(id));
    }

    @Override
    public void suspendMember(String id) {
        Member m = getMember(id);
        if (m != null) {
            m.suspendedUntil = new java.util.Date();
            m.suspensionCount++;
        }
    }
}