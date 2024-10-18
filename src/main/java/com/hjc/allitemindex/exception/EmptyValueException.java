package com.hjc.allitemindex.exception;

public class EmptyValueException extends Exception {

    public EmptyValueException(Object object) {
        super(String.format("对象包含空值: %s", object));
    }
}
