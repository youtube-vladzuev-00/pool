import java.util.Objects;

public final class Connection {
    private final long id;
    private boolean autoCommit;

    public Connection(final long id, final boolean autoCommit) {
        this.id = id;
        this.autoCommit = autoCommit;
    }

    public long getId() {
        return this.id;
    }

    public void setAutoCommit(final boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean isAutoCommit() {
        return this.autoCommit;
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null) {
            return false;
        }
        if (this.getClass() != otherObject.getClass()) {
            return false;
        }
        final Connection other = (Connection) otherObject;
        return this.id == other.id && this.autoCommit == other.autoCommit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.autoCommit);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[id = " + this.id + ", autoCommit = " + this.autoCommit + "]";
    }
}
