package Database;

import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.MemberType;
import Objects.Membership;
import Objects.Person;
import Objects.Suspension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbLibraryStore implements ILibraryStore {

    private final String url;
    private final String user;
    private final String password;

    public DbLibraryStore(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void addBookTitle(BookTitle bookTitle) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public BookTitle getBookTitle(String isbn) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void removeBookTitle(String isbn) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void addBookCopies(String isbn, int count) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<BookCopy> getBookCopies(String isbn) {
        return new ArrayList<>();
    }

    @Override
    public BookCopy getAvailableBookCopy(String isbn) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void updateBookCopy(BookCopy copy) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void removeBookCopiesByIsbn(String isbn) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void addPerson(Person person) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Person getPerson(String personalNumber) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void addMembership(Membership membership) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Membership getMembership(int memberId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Membership getMembershipByPersonalNumber(String personalNumber) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void updateMembership(Membership membership) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void removeMembership(int memberId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public MemberType getMemberType(int memberTypeId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<MemberType> getAllMemberTypes() {
        return new ArrayList<>();
    }

    @Override
    public void addLoan(Loan loan) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void updateLoan(Loan loan) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Loan getActiveLoan(int memberId, String isbn) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<Loan> getLoansForMember(int memberId) {
        return new ArrayList<>();
    }

    @Override
    public List<Loan> getLoansForBook(String isbn) {
        return new ArrayList<>();
    }

    @Override
    public void addSuspension(Suspension suspension) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<Suspension> getSuspensionsForMember(int memberId) {
        return new ArrayList<>();
    }
}