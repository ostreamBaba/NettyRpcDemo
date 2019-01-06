package com.viscu.rpc.utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ Create by ostreamBaba on 19-1-2
 * @ 序列化工具类
 */

public class SerializationUtil {

    private static Map<Class<?>, Schema<?>> cacheSchema = new ConcurrentHashMap<>();

    /*比Java反射更加强大*/
    private static Objenesis objenesis = new ObjenesisStd(true);

    private static ThreadLocal<LinkedBuffer> threadLocal = ThreadLocal.withInitial(()->
            LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

    private SerializationUtil(){}

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz){
        return (Schema<T>) cacheSchema.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    /**
     * @描述 序列化
     * @param obj
     * @return byte[]
     * @create by ostreamBaba on 下午8:33 19-1-2
     */

    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj){
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = threadLocal.get();
        try {
            Schema<T> schema = getSchema(clazz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            buffer.clear();
        }
        return null;
    }

    /**
     * @描述 反序列化
     * @param data
     * @param clazz
     * @return T
     * @create by ostreamBaba on 下午8:35 19-1-2
     */

    public static <T> T deserialize(byte[] data, Class<T> clazz){
        try {
            T obj = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(data, obj, schema);
            return obj;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
