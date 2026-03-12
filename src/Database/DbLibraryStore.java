package Database;

import java.util.ArrayList;
import java.util.List;
import Processing.*;

public class DbLibraryStore implements ILibraryStore {

    @Override
    public void addBook(Book newBook) {
        // SQL here later
    }

    @Override
    public void updateBook(Book book) {
        // SQL here later
    }

    @Override
    public Book getBook(String isbn) {
        return null;
    }

    @Override
    public void removeBook(String isbn) {
        // SQL here later
    }

    @Override
    public void addMember(Member newMember) {
        // SQL here later
    }

    @Override
    public void updateMember(Member member) {
        // SQL here later
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
        // SQL here later
    }

    @Override
    public void addLoan(Loan loan) {
        // SQL here later
    }

    @Override
    public void updateLoan(Loan loan) {
        // SQL here later
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