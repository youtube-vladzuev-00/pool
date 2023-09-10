import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import static java.util.stream.IntStream.range;

public abstract class AbstractPool<T> {
    private final List<PoolObject<T>> poolObjects;
    private final Semaphore semaphore;

    public AbstractPool(final Supplier<T> objectSupplier, final int size) {
        this.poolObjects = createPoolObjects(objectSupplier, size);
        this.semaphore = new Semaphore(size);
    }

    public final T acquire() {
        this.semaphore.acquireUninterruptibly();
        return this.acquireObject();
    }

    public final void release(final T object) {
        if (this.releaseObject(object)) {
            this.semaphore.release();
        }
    }

    protected abstract void cleanObject(final T object);

    private static <T> List<PoolObject<T>> createPoolObjects(final Supplier<T> objectSupplier, final int size) {
        return range(0, size)
                .mapToObj(i -> objectSupplier.get())
                .map(object -> new PoolObject<>(object, false))
                .toList();
    }

    private synchronized T acquireObject() {
        return this.poolObjects.stream()
                .filter(poolObject -> !poolObject.isIssued())
                .findFirst()
                .map(AbstractPool::markAsIssued)
                .map(PoolObject::getObject)
                .orElseThrow(IllegalStateException::new);
    }

    private static <T> PoolObject<T> markAsIssued(final PoolObject<T> poolObject) {
        poolObject.setIssued(true);
        return poolObject;
    }

    private synchronized boolean releaseObject(final T object) {
        return this.poolObjects.stream()
                .filter(PoolObject::isIssued)
                .filter(poolObject -> Objects.equals(poolObject.getObject(), object))
                .findFirst()
                .map(this::cleanPoolObject)
                .isPresent();
    }

    private PoolObject<T> cleanPoolObject(final PoolObject<T> poolObject) {
        poolObject.setIssued(false);
        this.cleanObject(poolObject.getObject());
        return poolObject;
    }

    private static final class PoolObject<T> {
        private final T object;
        private boolean issued;

        public PoolObject(final T object, final boolean issued) {
            this.object = object;
            this.issued = issued;
        }

        public T getObject() {
            return this.object;
        }

        public boolean isIssued() {
            return this.issued;
        }

        public void setIssued(final boolean issued) {
            this.issued = issued;
        }
    }
}
