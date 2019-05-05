package ru.protal.b2b.exceptions;

import java.util.Map;

public class UserValidationException extends RuntimeException {

    private Map conflicts;

    public UserValidationException(Map conflicts) {
        this.conflicts = conflicts;
    }

    public Map getConflicts() {
        return conflicts;
    }
}
