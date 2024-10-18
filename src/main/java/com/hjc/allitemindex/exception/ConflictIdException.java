package com.hjc.allitemindex.exception;

import com.hjc.allitemindex.model.ItemInfo;

public class ConflictIdException extends RuntimeException {
    public ConflictIdException(ItemInfo item1, ItemInfo item2) {
        super(String.format("ID冲突: %s, %s", item1, item2));
    }
}
