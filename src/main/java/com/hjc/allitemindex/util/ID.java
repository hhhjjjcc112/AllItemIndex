package com.hjc.allitemindex.util;

import java.util.Objects;

public class ID {
    public long id;
    private static long nextID = 0;

    public ID() {
        synchronized (ID.class) {
            id = ++nextID;
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
        return id == id1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "id=" + id;
    }
}
