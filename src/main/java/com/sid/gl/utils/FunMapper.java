package com.sid.gl.utils;

public interface FunMapper<T,R> {
    R apply(T source);
}
