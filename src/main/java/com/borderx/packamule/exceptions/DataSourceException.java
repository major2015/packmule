package com.borderx.packamule.exceptions;

public class DataSourceException extends RuntimeException {

    private static final long serialVersionUID = -1L;

    public DataSourceException(String m) {
        super(m);
    }

    public DataSourceException(Throwable t) {
        super(t);
    }

    public DataSourceException(String m, Throwable t) {
        super(m, t);
    }
}
