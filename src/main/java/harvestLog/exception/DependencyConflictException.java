package harvestLog.exception;

import harvestLog.dto.DependencyConflictResponse;

public class DependencyConflictException extends RuntimeException {

    private final DependencyConflictResponse conflictData;

    public DependencyConflictException(DependencyConflictResponse conflictData) {
        super(conflictData.message());
        this.conflictData = conflictData;
    }

    public DependencyConflictResponse getConflictData() {
        return conflictData;
    }
}
