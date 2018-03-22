package cn.bumo.access.utils;

import cn.bumo.access.utils.spring.TypeUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class DataTypeUtils{

    private static Map<Class<?>, Class<?>> wrapperTypes = new HashMap<>();

    static{
        wrapperTypes.put(long.class, Long.class);
        wrapperTypes.put(int.class, Integer.class);
        wrapperTypes.put(char.class, Character.class);
        wrapperTypes.put(byte.class, Byte.class);
        wrapperTypes.put(boolean.class, Boolean.class);
    }

    public static boolean isAssignable(Type lhsType, Type rhsType){
        boolean assignable = TypeUtils.isAssignable(lhsType, rhsType);
        if (assignable) {
            return true;
        }
        if (lhsType instanceof Class) {
            Class<?> lhsClass = (Class<?>) lhsType;

        }

        return false;
    }

}
