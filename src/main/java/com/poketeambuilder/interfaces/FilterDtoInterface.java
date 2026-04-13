package com.poketeambuilder.interfaces;

import java.lang.reflect.Field;

public interface FilterDtoInterface {
    
    default boolean hasAnyCriteria() {
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                if (field.get(this) != null){
                    return true;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
