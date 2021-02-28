package nl.bvsit.coworker.exceptions;

import nl.bvsit.coworker.config.CwConstants;

public class TestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TestException(Long id) { super(String.format(CwConstants.RESOURCE_NOT_FOUND_WITH_ID,id)); }
    public TestException(String message) {
        super(message);
    }
    public TestException() {super(CwConstants.RESOURCE_NOT_FOUND); }
}