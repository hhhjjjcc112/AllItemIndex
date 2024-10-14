package com.hjc.allitemindex.exception;

import com.hjc.allitemindex.model.PinYin;

/**
 * 拼音和对应中文不匹配
 */
public class PinYinNotMatchException extends RuntimeException {

    private final PinYin wrongPinYin;

    public PinYinNotMatchException(PinYin pinyin) {
        super(String.format("%s and %s dismatch", pinyin.chineseName, pinyin.pinYin));
        wrongPinYin = pinyin;
    }

    public PinYin getWrongPinYin() {
      return wrongPinYin;
    }
}
