import java.util.Date;

public class Member {
    public String firstName;
    public String lastName;
    public String id;
    public int level; //1=undergrad, 2=master, 3=phd, 4=teacher
    public int borrowedCount;
    public int lateReturnCount;
    public int suspensionCount;
    public Date suspendedUntil; //If null => not suspended

    public Member() {}

    public Member(String id, String firstName, String lastName, int level) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.level = level;
        this.borrowedCount = 0;
        this.lateReturnCount = 0;
        this.suspensionCount = 0;
        this.suspendedUntil = null;
    }

    public int maxLoans() {
        if (level == 1) return 3;   //undergrad
        if (level == 2) return 5;   //master
        if (level == 3) return 7;   //phd
        if (level == 4) return 10;  //teacher
        return 0; //unknown type
    }

    public boolean isSuspended(Date today) {
        if (suspendedUntil == null) {
            return false;
        }
        if (today.before(suspendedUntil)) {
            return true;
        }
        return false;
    }

    public boolean canBorrow() {
        return borrowedCount < maxLoans();
    }
}