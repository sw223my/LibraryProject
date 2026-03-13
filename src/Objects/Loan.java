package Objects;

import java.util.Date;
import java.util.Objects;

public class Loan {
    public int loanId;
    public int memberId;
    public int copyId;
    public Date loanDate;
    public Date dueDate;
    public Date returnDate;

    public Loan(int loanId, int memberId, int copyId, Date loanDate, Date dueDate, Date returnDate) {
        this.loanId = loanId;
        this.memberId = memberId;
        this.copyId = copyId;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    public boolean isLate(Date today) {
        return returnDate == null && dueDate != null && today.after(dueDate);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "loanId=" + loanId +
                ", memberId=" + memberId +
                ", copyId=" + copyId +
                ", loanDate=" + loanDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Loan other)) return false;
        return loanId == other.loanId &&
                memberId == other.memberId &&
                copyId == other.copyId &&
                Objects.equals(loanDate, other.loanDate) &&
                Objects.equals(dueDate, other.dueDate) &&
                Objects.equals(returnDate, other.returnDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId, memberId, copyId, loanDate, dueDate, returnDate);
    }
}
