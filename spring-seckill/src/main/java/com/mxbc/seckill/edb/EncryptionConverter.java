package com.mxbc.seckill.edb;

import cn.hutool.crypto.symmetric.AES;
import javax.persistence.Converter;
import javax.persistence.AttributeConverter;



@Converter
public class EncryptionConverter implements AttributeConverter<String, String> {
    private static final byte[] SALT = "1234567890123456".getBytes();
    
    @Override
    public String convertToDatabaseColumn(String attribute) {

        if (attribute == null) {
            return null;
        }
        return new AES(SALT).encryptHex(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return new AES(SALT).decryptStr(dbData);
        } catch (Exception e) {
            // 解密失败时返回原始数据，避免应用崩溃
            return dbData;
        }
    }
}
