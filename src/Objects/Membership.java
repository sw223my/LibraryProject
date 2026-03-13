package Objects;

import java.util.Date;
import java.util.Objects;

public class Membership {
    public int memberId;
    public String personalNumber;
    public int memberTypeId;
    public Date suspendedUntil;
    public String status;
    public int lateReturnCount;
    public int suspensionCount;

    public Membership(int memberId,
                      String personalNumber,
                      int memberTypeId,
                      Date suspendedUntil,
                      String status,
                      int lateReturnCount,
                      int suspensionCount) {
        this.memberId = memberId;
        this.personalNumber = personalNumber;
        this.memberTypeId = memberTypeId;
        this.suspendedUntil = suspendedUntil;
        this.status = status;
        this.lateReturnCount = lateReturnCount;
        this.suspensionCount = suspensionCount;
    }

    public boolean isSuspended(Date today) {
        return suspendedUntil != null && suspendedUntil.after(today);
    }

    @Override
    public String toString() {
        return "Membership{" +
                "memberId=" + memberId +
                ", personalNumber='" + personalNumber + '\'' +
                ", memberTypeId=" + memberTypeId +
                ", suspendedUntil=" + suspendedUntil +
                ", status='" + status + '\'' +
                ", lateReturnCount=" + lateReturnCount +
                ", suspensionCount=" + suspensionCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Membership other)) return false;
        return memberId == other.memberId &&
                memberTypeId == other.memberTypeId &&
                lateReturnCount == other.lateReturnCount &&
                suspensionCount == other.suspensionCount &&
                Objects.equals(personalNumber, other.personalNumber) &&
                Objects.equals(suspendedUntil, other.suspendedUntil) &&
                Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, personalNumber, memberTypeId,
                suspendedUntil, status, lateReturnCount, suspensionCount);
    }
}
