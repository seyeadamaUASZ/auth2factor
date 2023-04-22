package com.sid.gl.utils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class MapUtils {
    public static<T,R> List<R> buildConvertList(List<T> lists, FunMapper<T,R> funMapper){
        List<R> resultList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(lists)){
           lists.stream()
                   .forEach(t -> resultList.add(funMapper.apply(t)));
           return resultList;
        }
        return null;
    }
}
