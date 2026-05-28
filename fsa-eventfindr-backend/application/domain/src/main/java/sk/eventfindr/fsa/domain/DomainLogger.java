package sk.eventfindr.fsa.domain;

public interface DomainLogger {

    void info(String message, Object... args);

    void warn(String message, Object... args);

    static DomainLogger noop() {
        return NoopDomainLogger.INSTANCE;
    }

    final class NoopDomainLogger implements DomainLogger {
        private static final NoopDomainLogger INSTANCE = new NoopDomainLogger();

        private NoopDomainLogger() {
        }

        @Override
        public void info(String message, Object... args) {
        }

        @Override
        public void warn(String message, Object... args) {
        }
    }
}
