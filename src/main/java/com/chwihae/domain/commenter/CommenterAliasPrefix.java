package com.chwihae.domain.commenter;

public enum CommenterAliasPrefix {
    별랑이;

    public static String getAlias(int sequence) {
        return 별랑이.name() + sequence;
    }
}
