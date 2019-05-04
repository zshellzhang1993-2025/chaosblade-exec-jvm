/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.chaosblade.exec.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Changjun Xiao
 */
public class ReflectUtil {

    /**
     * Invoke method by reflect
     *
     * @param obj
     * @param methodName
     * @param args
     * @param throwException
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T invokeMethod(Object obj, String methodName, Object[] args, boolean throwException)
        throws Exception {
        if (obj == null) {
            return null;
        }
        return invoke(obj.getClass(), obj, methodName, args, throwException);
    }

    /**
     * Invoke static method
     *
     * @param clazz
     * @param methodName
     * @param args
     * @param throwException
     * @param <T>
     * @return
     */
    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Object[] args, boolean throwException)
        throws Exception {
        return invoke(clazz, null, methodName, args, throwException);
    }

    private static <T> T invoke(Class<?> clazz, Object obj, String methodName, Object[] args,
                                boolean throwException)
        throws Exception {
        try {
            Method method = getMethod(clazz, methodName, args);
            method.setAccessible(true);
            return (T)method.invoke(obj, args);
        } catch (Exception e) {
            if (throwException) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Get method object
     *
     * @param clazz
     * @param methodName
     * @param args
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> clazz, String methodName, Object[] args) throws NoSuchMethodException {
        Class[] argsClass = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            if (args[i] instanceof Boolean) {
                argsClass[i] = boolean.class;
                continue;
            }
            if (args[i] instanceof Integer) {
                argsClass[i] = int.class;
                continue;
            }
            if (args[i] instanceof Long) {
                argsClass[i] = long.class;
                continue;
            }
            if (args[i] == null) {
                continue;
            }
            argsClass[i] = args[i].getClass();
        }

        return getMethod(clazz, methodName, argsClass);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        Method method;
        try {
            method = clazz.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e1) {
                return getMethodByName(clazz, methodName, parameterTypes);
            }
        }
        return method;
    }

    private static Method getMethodByName(Class<?> clazz, String methodName, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        if (clazz == Object.class) {
            throw new NoSuchMethodException();
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] methodParamTypes = method.getParameterTypes();
            if (methodParamTypes == null && parameterTypes == null) {
                return method;
            }
            if (methodParamTypes == null || parameterTypes == null) {
                continue;
            }
            if (methodParamTypes.length != parameterTypes.length) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == null) {
                    continue;
                }
                if (!methodParamTypes[i].isAssignableFrom(parameterTypes[i])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return method;
            }
        }
        return getMethodByName(clazz.getSuperclass(), methodName, parameterTypes);
    }

    /**
     * Get object fields
     *
     * @param obj
     * @param fieldName
     * @param throwException
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getFieldValue(Object obj, String fieldName, boolean throwException) throws Exception {
        Field field = null;
        try {
            try {
                if (obj == null) {
                    return null;
                }
                field = obj.getClass().getField(fieldName);
                field.setAccessible(true);
                return (T)field.get(obj);
            } catch (Exception e) {
                field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T)field.get(obj);
            }
        } catch (Exception e) {
            if (throwException) {
                throw e;
            }
        }
        return null;
    }

    public static boolean isAssignableFrom(ClassLoader classLoader, Class<?> clazz, String clazzName) {
        if (clazz == null || clazzName == null) {
            return false;
        }
        if (clazz.getName().equals(clazzName) || clazz.getSimpleName().equals(clazzName)) {
            return true;
        }
        if (classLoader == null) {
            return false;
        }
        try {
            Class<?> parentClazz = classLoader.loadClass(clazzName);
            return parentClazz.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
