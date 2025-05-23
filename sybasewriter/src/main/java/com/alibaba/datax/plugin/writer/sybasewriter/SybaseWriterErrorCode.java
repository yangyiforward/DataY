package com.alibaba.datax.plugin.writer.sybasewriter;

import com.alibaba.datax.common.spi.ErrorCode;

public enum SybaseWriterErrorCode implements ErrorCode {
    ;

    private final String code;
    private final String describe;

    private SybaseWriterErrorCode(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.describe;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Describe:[%s]. ", this.code,
                this.describe);
    }
}
