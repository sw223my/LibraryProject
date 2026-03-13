package Objects;

import java.util.Date;
import java.util.Objects;

public class Suspension {
    public int suspensionId;
    public int memberId;
    public Date startDate;
    public Date endDate;
    public String reason;

    public Suspension(int suspensionId, int memberId, Date startDate, Date endDate, String reason) {
        this.suspensionId = suspensionId;
        this.memberId = memberId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Suspension{" +
                "suspensionId=" + suspensionId +
                ", memberId=" + memberId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Suspension other)) return false;
        return suspensionId == other.suspensionId &&
                memberId == other.memberId &&
                Objects.equals(startDate, other.startDate) &&
                Objects.equals(endDate, other.endDate) &&
                Objects.equals(reason, other.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suspensionId, memberId, startDate, endDate, reason);
    }
}
