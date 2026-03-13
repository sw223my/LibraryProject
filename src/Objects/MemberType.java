package Objects;

import java.util.Objects;

public class MemberType {
    public int memberTypeId;
    public String typeName;
    public int maxLoans;

    public MemberType(int memberTypeId, String typeName, int maxLoans) {
        this.memberTypeId = memberTypeId;
        this.typeName = typeName;
        this.maxLoans = maxLoans;
    }

    @Override
    public String toString() {
        return "MemberType{" +
                "memberTypeId=" + memberTypeId +
                ", typeName='" + typeName + '\'' +
                ", maxLoans=" + maxLoans +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemberType other)) return false;
        return memberTypeId == other.memberTypeId &&
                maxLoans == other.maxLoans &&
                Objects.equals(typeName, other.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberTypeId, typeName, maxLoans);
    }
}
