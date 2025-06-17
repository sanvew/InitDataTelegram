package io.github.sanvew.tg.init.data.exception;

import io.github.sanvew.tg.init.data.type.InitData;

public class AuthDateMissingException extends PropertyMissingException {
    public AuthDateMissingException() {
        super(InitData.Param.AUTH_DATE.value);
    }
}
