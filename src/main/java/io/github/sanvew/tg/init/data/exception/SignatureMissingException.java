package io.github.sanvew.tg.init.data.exception;

import io.github.sanvew.tg.init.data.type.InitData;

public class SignatureMissingException extends PropertyMissingException {
    public SignatureMissingException(boolean isThirdParty) {
        super(isThirdParty ? "signature" : InitData.Param.HASH.value);
    }

    public SignatureMissingException() {
        this(false);
    }
}
