package com.hjc.allitemindex.util;

import java.util.Objects;

public class ID {
    public long value;
    private static long nextID = 0;

    public ID() {
        synchronized (ID.class) {
            value = ++nextID;
        }
    }

    public static void reset() {
        synchronized (ID.class) {
            nextID = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ID id1)) return false;
        return value == id1.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "id=" + value;
    }
}
