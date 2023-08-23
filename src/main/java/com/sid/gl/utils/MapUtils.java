package com.sid.gl.utils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapUtils {
    public static<T,R> List<R> buildConvertList(List<T> lists, Function<T,R> funMapper){
        if(!CollectionUtils.isEmpty(lists)){
         return  lists.stream()
                  .filter(Objects::nonNull)
                  .map(funMapper)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
        }
        return null;
    }
}
