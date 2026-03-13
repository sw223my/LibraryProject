package Database;

import Objects.BookCopy;
import Objects.BookTitle;
import Objects.Loan;
import Objects.MemberType;
import Objects.Membership;
import Objects.Person;
import Objects.Suspension;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    private Date toSqlDate(java.util.Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    private java.util.Date toUtilDate(Date date) {
        return date == null ? null : new java.util.Date(date.getTime());
    }

    @Override
    public void addBookTitle(BookTitle bookTitle) {
        String sql = """
                INSERT INTO book_title (isbn, title, author, publish_year)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bookTitle.isbn);
            stmt.setString(2, bookTitle.title);
            stmt.setString(3, bookTitle.author);
            stmt.setInt(4, bookTitle.publishYear);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add book title.", e);
        }
    }

    @Override
    public BookTitle getBookTitle(String isbn) {
        String sql = """
                SELECT isbn, title, author, publish_year
                FROM book_title
                WHERE isbn = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new BookTitle(
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("publish_year")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch book title.", e);
        }
    }

    @Override
    public void removeBookTitle(String isbn) {
        String sql = """
                DELETE FROM book_title
                WHERE isbn = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove book title.", e);
        }
    }

    @Override
    public void addBookCopies(String isbn, int count) {
        String sql = """
                INSERT INTO book_copy (isbn, status)
                VALUES (?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < count; i++) {
                stmt.setString(1, isbn);
                stmt.setString(2, "AVAILABLE");
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add book copies.", e);
        }
    }

    @Override
    public List<BookCopy> getBookCopies(String isbn) {
        String sql = """
                SELECT copy_id, isbn, status
                FROM book_copy
                WHERE isbn = ?
                ORDER BY copy_id
                """;

        List<BookCopy> copies = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    copies.add(new BookCopy(
                            rs.getInt("copy_id"),
                            rs.getString("isbn"),
                            rs.getString("status")
                    ));
                }
            }

            return copies;
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch book copies.", e);
        }
    }

    @Override
    public BookCopy getAvailableBookCopy(String isbn) {
        String sql = """
                SELECT copy_id, isbn, status
                FROM book_copy
                WHERE isbn = ? AND status = 'AVAILABLE'
                ORDER BY copy_id
                LIMIT 1
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new BookCopy(
                            rs.getInt("copy_id"),
                            rs.getString("isbn"),
                            rs.getString("status")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch available book copy.", e);
        }
    }

    @Override
    public void updateBookCopy(BookCopy copy) {
        String sql = """
                UPDATE book_copy
                SET isbn = ?, status = ?
                WHERE copy_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, copy.isbn);
            stmt.setString(2, copy.status);
            stmt.setInt(3, copy.copyId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update book copy.", e);
        }
    }

    @Override
    public void removeBookCopiesByIsbn(String isbn) {
        String sql = """
                DELETE FROM book_copy
                WHERE isbn = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove book copies.", e);
        }
    }

    @Override
    public void addPerson(Person person) {
        String sql = """
                INSERT INTO person (personal_number, first_name, last_name)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, person.personalNumber);
            stmt.setString(2, person.firstName);
            stmt.setString(3, person.lastName);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add person.", e);
        }
    }

    @Override
    public Person getPerson(String personalNumber) {
        String sql = """
                SELECT personal_number, first_name, last_name
                FROM person
                WHERE personal_number = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personalNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Person(
                            rs.getString("personal_number"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch person.", e);
        }
    }

    @Override
    public void addMembership(Membership membership) {
        String sql = """
                INSERT INTO membership (
                    member_id,
                    personal_number,
                    member_type_id,
                    suspended_until,
                    status,
                    late_return_count,
                    suspension_count
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, membership.memberId);
            stmt.setString(2, membership.personalNumber);
            stmt.setInt(3, membership.memberTypeId);
            stmt.setDate(4, toSqlDate(membership.suspendedUntil));
            stmt.setString(5, membership.status);
            stmt.setInt(6, membership.lateReturnCount);
            stmt.setInt(7, membership.suspensionCount);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add membership.", e);
        }
    }

    @Override
    public Membership getMembership(int memberId) {
        String sql = """
                SELECT
                    member_id,
                    personal_number,
                    member_type_id,
                    suspended_until,
                    status,
                    late_return_count,
                    suspension_count
                FROM membership
                WHERE member_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Membership(
                            rs.getInt("member_id"),
                            rs.getString("personal_number"),
                            rs.getInt("member_type_id"),
                            toUtilDate(rs.getDate("suspended_until")),
                            rs.getString("status"),
                            rs.getInt("late_return_count"),
                            rs.getInt("suspension_count")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch membership.", e);
        }
    }

    @Override
    public Membership getMembershipByPersonalNumber(String personalNumber) {
        String sql = """
                SELECT
                    member_id,
                    personal_number,
                    member_type_id,
                    suspended_until,
                    status,
                    late_return_count,
                    suspension_count
                FROM membership
                WHERE personal_number = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personalNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Membership(
                            rs.getInt("member_id"),
                            rs.getString("personal_number"),
                            rs.getInt("member_type_id"),
                            toUtilDate(rs.getDate("suspended_until")),
                            rs.getString("status"),
                            rs.getInt("late_return_count"),
                            rs.getInt("suspension_count")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch membership by personal number.", e);
        }
    }

    @Override
    public void updateMembership(Membership membership) {
        String sql = """
                UPDATE membership
                SET personal_number = ?,
                    member_type_id = ?,
                    suspended_until = ?,
                    status = ?,
                    late_return_count = ?,
                    suspension_count = ?
                WHERE member_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, membership.personalNumber);
            stmt.setInt(2, membership.memberTypeId);
            stmt.setDate(3, toSqlDate(membership.suspendedUntil));
            stmt.setString(4, membership.status);
            stmt.setInt(5, membership.lateReturnCount);
            stmt.setInt(6, membership.suspensionCount);
            stmt.setInt(7, membership.memberId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update membership.", e);
        }
    }

    @Override
    public void removeMembership(int memberId) {
        String sql = """
                DELETE FROM membership
                WHERE member_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove membership.", e);
        }
    }

    @Override
    public MemberType getMemberType(int memberTypeId) {
        String sql = """
                SELECT member_type_id, type_name, max_loans
                FROM member_type
                WHERE member_type_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberTypeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MemberType(
                            rs.getInt("member_type_id"),
                            rs.getString("type_name"),
                            rs.getInt("max_loans")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch member type.", e);
        }
    }

    @Override
    public List<MemberType> getAllMemberTypes() {
        String sql = """
                SELECT member_type_id, type_name, max_loans
                FROM member_type
                ORDER BY member_type_id
                """;

        List<MemberType> types = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                types.add(new MemberType(
                        rs.getInt("member_type_id"),
                        rs.getString("type_name"),
                        rs.getInt("max_loans")
                ));
            }

            return types;
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch member types.", e);
        }
    }

    @Override
    public void addLoan(Loan loan) {
        String sql = """
                INSERT INTO loan (
                    loan_id,
                    member_id,
                    copy_id,
                    loan_date,
                    due_date,
                    return_date
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loan.loanId);
            stmt.setInt(2, loan.memberId);
            stmt.setInt(3, loan.copyId);
            stmt.setDate(4, toSqlDate(loan.loanDate));
            stmt.setDate(5, toSqlDate(loan.dueDate));
            stmt.setDate(6, toSqlDate(loan.returnDate));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not add loan.", e);
        }
    }

    @Override
    public void updateLoan(Loan loan) {
        String sql = """
                UPDATE loan
                SET member_id = ?,
                    copy_id = ?,
                    loan_date = ?,
                    due_date = ?,
                    return_date = ?
                WHERE loan_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loan.memberId);
            stmt.setInt(2, loan.copyId);
            stmt.setDate(3, toSqlDate(loan.loanDate));
            stmt.setDate(4, toSqlDate(loan.dueDate));
            stmt.setDate(5, toSqlDate(loan.returnDate));
            stmt.setInt(6, loan.loanId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update loan.", e);
        }
    }

    @Override
    public Loan getActiveLoan(int memberId, String isbn) {
        String sql = """
                SELECT
                    l.loan_id,
                    l.member_id,
                    l.copy_id,
                    l.loan_date,
                    l.due_date,
                    l.return_date
                FROM loan l
                INNER JOIN book_copy bc ON l.copy_id = bc.copy_id
                WHERE l.member_id = ?
                  AND bc.isbn = ?
                  AND l.return_date IS NULL
                LIMIT 1
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            stmt.setString(2, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Loan(
                            rs.getInt("loan_id"),
                            rs.getInt("member_id"),
                            rs.getInt("copy_id"),
                            toUtilDate(rs.getDate("loan_date")),
                            toUtilDate(rs.getDate("due_date")),
                            toUtilDate(rs.getDate("return_date"))
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch active loan.", e);
        }
    }

    @Override
    public List<Loan> getLoansForMember(int memberId) {
        String sql = """
                SELECT loan_id, member_id, copy_id, loan_date, due_date, return_date
                FROM loan
                WHERE member_id = ?
                ORDER BY loan_date DESC, loan_id DESC
                """;

        List<Loan> loans = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(new Loan(
                            rs.getInt("loan_id"),
                            rs.getInt("member_id"),
                            rs.getInt("copy_id"),
                            toUtilDate(rs.getDate("loan_date")),
                            toUtilDate(rs.getDate("due_date")),
                            toUtilDate(rs.getDate("return_date"))
                    ));
                }
            }

            return loans;
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch loans for member.", e);
        }
    }

    @Override
    public List<Loan> getLoansForBook(String isbn) {
        String sql = """
                SELECT
                    l.loan_id,
                    l.member_id,
                    l.copy_id,
                    l.loan_date,
                    l.due_date,
                    l.return_date
                FROM loan l
                INNER JOIN book_copy bc ON l.copy_id = bc.copy_id
                WHERE bc.isbn = ?
                ORDER BY l.loan_date DESC, l.loan_id DESC
                """;

        List<Loan> loans = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(new Loan(
                            rs.getInt("loan_id"),
                            rs.getInt("member_id"),
                            rs.getInt("copy_id"),
                            toUtilDate(rs.getDate("loan_date")),
                            toUtilDate(rs.getDate("due_date")),
                            toUtilDate(rs.getDate("return_date"))
                    ));
                }
            }

            return loans;
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch loans for book.", e);
        }
    }

    @Override
    public void addSuspension(Suspension suspension) {
        String sql = """
            
                INSERT INTO suspension (
                suspension_id,
                member_id,
                start_date,
                end_date
                            )
                            VALUES (
                ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, suspension.suspensionId);
            stmt.setInt(2, suspension.memberId);
            stmt.setDate(3, toSqlDate(suspension.startDate));
            stmt.setDate(4, toSqlDate(suspension.endDate));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not suspension.", e);
        }
    }
    @Override
    public List<Suspension> getSuspensionsForMember(int memberId) {
        String sql = """
            SELECT suspension_id, member_id, start_date, end_date
            FROM suspension
            WHERE member_id = ?
            ORDER BY start_date DESC, suspension_id DESC
            """;

        List<Suspension> suspensions = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suspensions.add(new Suspension(
                            rs.getInt("suspension_id"),
                            rs.getInt("member_id"),
                            toUtilDate(rs.getDate("start_date")),
                            toUtilDate(rs.getDate("end_date"))
                    ));
                }
            }

            return suspensions;
        } catch (SQLException e) {
            throw new RuntimeException("Could not fetch suspensions for member.", e);
        }
    }
}