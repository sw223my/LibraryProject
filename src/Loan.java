import java.util.Date;

public class Loan {
    public int loanId;
    public String memberId;
    public String isbn;
    public Date loanDate;
    public Date dueDate;
    public Date returnDate; //null=not returned yet

    public Loan() {} //konstruktor

    public Loan(int loanId, String memberId, String isbn, Date loanDate, Date dueDate) {
        this.loanId = loanId;
        this.memberId = memberId;
        this.isbn = isbn;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = null;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    public boolean isLate(Date today) {
        if (returnDate == null) {
            return today.after(dueDate); //Not returned –> jämför today med due date
        } else {
            return returnDate.after(dueDate); //Returned –> jämför return date med due date
        }
    }
}