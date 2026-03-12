package Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Processing.*;

public class InMemoryLibraryStore implements ILibraryStore {

    private final HashMap<String, Book> books = new HashMap<>();
    private final HashMap<String, Member> members = new HashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    @Override
    public void addBook(Book newBook) {
        if (newBook == null || newBook.ISBN == null || newBook.ISBN.isBlank()) {
            return;
        }
        books.put(newBook.ISBN, newBook);
    }

    @Override
    public void updateBook(Book book) {
        if (book == null || book.ISBN == null || book.ISBN.isBlank()) {
            return;
        }
        books.put(book.ISBN, book);
    }

    @Override
    public Book getBook(String isbn) {
        return books.get(isbn);
    }

    @Override
    public void removeBook(String isbn) {
        books.remove(isbn);
    }

    @Override
    public void addMember(Member newMember) {
        if (newMember == null || newMember.id == null || newMember.id.isBlank()) {
            return;
        }
        members.put(newMember.id, newMember);
    }

    @Override
    public void updateMember(Member member) {
        if (member == null || member.id == null || member.id.isBlank()) {
            return;
        }
        members.put(member.id, member);
    }

    @Override
    public Member getMember(String memberId) {
        return members.get(memberId);
    }

    @Override
    public Member getMemberByPersonalNumber(String personalNumber) {
        for (Member member : members.values()) {
            if (member.personalNumber != null && member.personalNumber.equals(personalNumber)) {
                return member;
            }
        }
        return null;
    }

    @Override
    public void removeMember(String memberId) {
        members.remove(memberId);
    }

    @Override
    public void addLoan(Loan loan) {
        if (loan != null) {
            loans.add(loan);
        }
    }

    @Override
    public void updateLoan(Loan loan) {
        // behövs inte nu
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

    @Override
    public List<Loan> getLoansForBook(String isbn) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isbn.equals(isbn)) {
                result.add(loan);
            }
        }
        return result;
    }
}