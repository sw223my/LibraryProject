import java.util.Date;

public class Member {
    public String firstName;
    public String lastName;
    public String id;              // library member id, e.g. 4128
    public String personalNumber;
    public int level;              // 1=undergrad, 2=postgrad, 3=phd, 4=teacher

    public int borrowedCount;
    public int lateReturnCount;
    public int suspensionCount;
    public Date suspendedUntil;    // null = not suspended

    public Member() {}

    public Member(String id, String firstName, String lastName, String personalNumber, int level) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalNumber = personalNumber;
        this.level = level;
        this.borrowedCount = 0;
        this.lateReturnCount = 0;
        this.suspensionCount = 0;
        this.suspendedUntil = null;
    }

    public int maxLoans() {
        if (level == 1) return 3;   // undergraduate
        if (level == 2) return 5;   // postgraduate/master
        if (level == 3) return 7;   // phd
        if (level == 4) return 10;  // teacher
        return 0;
    }

    public boolean isSuspended(Date today) {
        if (suspendedUntil == null) {
            return false;
        }
        return today.before(suspendedUntil);
    }

    public boolean canBorrow(Date today) {
        return !isSuspended(today) && borrowedCount < maxLoans();
    }

    public String fullName() {
        String ln = (lastName == null) ? "" : (" " + lastName);
        return (firstName == null ? "" : firstName) + ln;
    }

    public String levelName() {
        if (level == 1) return "Undergraduate";
        if (level == 2) return "Postgraduate";
        if (level == 3) return "PhD";
        if (level == 4) return "Teacher";
        return "Unknown";
    }
}