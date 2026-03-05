
public class LibraryService {

    ILibraryStore store;

    public LibraryService(ILibraryStore store) {
        this.store = store;
    }

    public boolean borrow(String isbn, String memberId) {
        boolean status = false;

        Member memberInfo = store.getMember(memberId);
        Book book = store.getBook(isbn);

        // more code here...

        return status;
    }
}
