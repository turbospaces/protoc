package com.turbospaces.protoc.gen;

@SuppressWarnings("serial")
public abstract class AbstractGeneratedException extends Exception implements GeneratedMessage {
    @Override
    public AbstractGeneratedException clone() {
        return (AbstractGeneratedException) GeneratedMessage.Util.clone( this );
    }
    @Override
    public int hashCode() {
        return GeneratedMessage.Util.hashCode( this );
    }
    @Override
    public boolean equals(Object obj) {
        return GeneratedMessage.Util.equals( this, obj );
    }
    @Override
    public String toString() {
        return GeneratedMessage.Util.toString( this );
    }
}
