package com.whtc.employee.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.whtc.employee.annotation.DataMasking;
import com.whtc.employee.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏序列化器
 */
@Slf4j
public class DataMaskingSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private MaskingType maskingType;
    private boolean maskForAdmin;

    public DataMaskingSerializer() {
        this.maskingType = MaskingType.DEFAULT;
        this.maskForAdmin = false;
    }

    public DataMaskingSerializer(MaskingType maskingType, boolean maskForAdmin) {
        this.maskingType = maskingType;
        this.maskForAdmin = maskForAdmin;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }

        // 根据当前用户角色判断是否脱敏
        // 管理员（admin）或配置为不脱敏时，返回明文
        if (shouldSkipMasking()) {
            gen.writeString(value);
            return;
        }

        String maskedValue = mask(value, maskingType);
        gen.writeString(maskedValue);
    }

    /**
     * 判断是否跳过脱敏（管理员看明文）
     */
    private boolean shouldSkipMasking() {
        try {
            // 从ThreadLocal获取当前登录用户
            com.whtc.employee.context.BaseContext baseContext = new com.whtc.employee.context.BaseContext();
            com.whtc.employee.entity.SysUser currentUser = baseContext.getCurrentUser();

            if (currentUser == null) {
                return false; // 未登录用户，脱敏
            }

            // 超级管理员（roleId=1 或 roleCode=admin）不看脱敏
            if (currentUser.getRoleId() != null && currentUser.getRoleId() == 1) {
                return true;
            }
            if ("admin".equals(currentUser.getRoleCode())) {
                return true;
            }

            // 如果注解配置为不对管理员脱敏
            if (this.maskForAdmin) {
                // 这里可以扩展其他"管理员"角色的判断
                return false;
            }
        } catch (Exception e) {
            // 出现异常时，为了安全起见，进行脱敏
            log.warn("判断脱敏权限时发生异常，将进行脱敏处理", e);
        }
        return false;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        DataMasking annotation = property.getAnnotation(DataMasking.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            return new DataMaskingSerializer(annotation.value(), annotation.maskForAdmin());
        }
        return prov.findValueSerializer(property.getType(), property);
    }

    /**
     * 根据类型执行脱敏
     */
    private String mask(String value, MaskingType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (type) {
            case PHONE -> maskPhone(value);
            case EMAIL -> maskEmail(value);
            case ID_CARD -> maskIdCard(value);
            case NAME -> maskName(value);
            case ADDRESS -> maskAddress(value);
            case BANK_CARD -> maskBankCard(value);
            case DEFAULT -> maskDefault(value);
        };
    }

    /**
     * 手机号脱敏：13800138000 → 138****8000
     */
    private String maskPhone(String phone) {
        if (phone.length() != 11) {
            return maskDefault(phone);
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏：zhangsan@qq.com → zhan***@qq.com
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return maskDefault(email);
        }
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);

        if (prefix.length() <= 2) {
            return "**" + suffix;
        }
        return prefix.substring(0, 2) + "***" + suffix;
    }

    /**
     * 身份证号脱敏：110101199001011234 → 110101********1234
     */
    private String maskIdCard(String idCard) {
        if (idCard.length() != 15 && idCard.length() != 18) {
            return maskDefault(idCard);
        }
        if (idCard.length() == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        // 15位身份证号
        return idCard.substring(0, 6) + "******" + idCard.substring(12);
    }

    /**
     * 姓名脱敏：张三 → 张*
     */
    private String maskName(String name) {
        if (name.length() <= 1) {
            return "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 地址脱敏：保留前6个字符，后面用***代替
     */
    private String maskAddress(String address) {
        if (address.length() <= 6) {
            return address + "***";
        }
        return address.substring(0, 6) + "***";
    }

    /**
     * 银行卡脱敏：6222021234567890123 → 622202*********0123
     */
    private String maskBankCard(String card) {
        if (card.length() < 10) {
            return maskDefault(card);
        }
        return card.substring(0, 6) + "*".repeat(card.length() - 10) + card.substring(card.length() - 4);
    }

    /**
     * 默认脱敏：显示前3后4，中间****
     */
    private String maskDefault(String value) {
        if (value.length() <= 7) {
            return value.charAt(0) + "****" + value.substring(value.length() - 1);
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }
}
