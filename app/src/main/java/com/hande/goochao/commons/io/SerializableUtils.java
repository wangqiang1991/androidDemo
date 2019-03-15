package com.hande.goochao.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by yanshen on 2015/11/24.
 */
public class SerializableUtils {

    /**
     * 序列化
     * @param serializable
     * @return
     * @throws Exception
     */
    public static byte[] serializer(Serializable serializable) throws Exception {
        ByteArrayOutputStream out = null;
        ObjectOutputStream outputStream = null;
        try {
            out = new ByteArrayOutputStream();
            outputStream = new ObjectOutputStream(out);
            outputStream.writeObject(serializable);
            return out.toByteArray();
        } catch (Exception ex){
            throw ex;
        } finally {
            outputStream.flush();
            outputStream.close();
            out.flush();
            out.close();
        }
    }

    /**
     * 反序列化
     * @param bytes
     * @return
     * @throws Exception
     */
    public static Serializable unSerializer(byte[] bytes) throws Exception {
        ByteArrayInputStream in = null;
        ObjectInputStream inputStream = null;
        try {
            in = new ByteArrayInputStream(bytes);
            inputStream = new ObjectInputStream(in);
            Serializable serializable = (Serializable) inputStream.readObject();
            return serializable;
        } catch (Exception ex){
            throw ex;
        } finally {
            inputStream.close();
            in.close();
        }
    }

}
