package com.whtc.employee.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 员工导出VO
 * 支持动态字段导出
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@HeadRowHeight(25)
@ContentRowHeight(20)
public class EmployeeExportVO {

    @ExcelProperty(value = "姓名", index = 0)
    @ColumnWidth(15)
    private String name;

    @ExcelProperty(value = "性别", index = 1, converter = GenderConverter.class)
    @ColumnWidth(10)
    private Integer gender;

    @ExcelProperty(value = "手机号", index = 2)
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty(value = "邮箱", index = 3)
    @ColumnWidth(25)
    private String email;

    @ExcelProperty(value = "身份证号", index = 4)
    @ColumnWidth(20)
    private String idCard;

    @ExcelProperty(value = "部门", index = 5)
    @ColumnWidth(15)
    private String deptName;

    @ExcelProperty(value = "职位", index = 6)
    @ColumnWidth(15)
    private String position;

    @ExcelProperty(value = "入职日期", index = 7)
    @ColumnWidth(15)
    private LocalDate entryDate;

    @ExcelProperty(value = "状态", index = 8, converter = StatusConverter.class)
    @ColumnWidth(10)
    private Integer status;

    @ExcelProperty(value = "家庭住址", index = 9)
    @ColumnWidth(40)
    private String address;

    /**
     * 性别转换器
     * 数据库定义：1=男, 2=女(部分历史数据可能为0)
     */
    public static class GenderConverter implements com.alibaba.excel.converters.Converter<Integer> {
        @Override
        public com.alibaba.excel.metadata.data.WriteCellData<?> convertToExcelData(Integer value,
                                                                                      com.alibaba.excel.metadata.property.ExcelContentProperty contentProperty,
                                                                                      com.alibaba.excel.metadata.GlobalConfiguration globalConfiguration) {
            String text = switch (value) {
                case 1 -> "男";
                case 0, 2 -> "女";
                default -> "未知";
            };
            return new com.alibaba.excel.metadata.data.WriteCellData<>(text);
        }

        @Override
        public Integer convertToJavaData(com.alibaba.excel.metadata.data.ReadCellData<?> cellData,
                                          com.alibaba.excel.metadata.property.ExcelContentProperty contentProperty,
                                          com.alibaba.excel.metadata.GlobalConfiguration globalConfiguration) {
            // 导入场景使用：与数据库保持一致，1=男，2=女
            String text = cellData.getStringValue();
            if (text == null) return null;
            return switch (text.trim()) {
                case "男" -> 1;
                case "女" -> 2;  // 统一使用2，与前端和后端统计保持一致
                default -> null;
            };
        }
    }

    /**
     * 状态转换器
     */
    public static class StatusConverter implements com.alibaba.excel.converters.Converter<Integer> {
        @Override
        public com.alibaba.excel.metadata.data.WriteCellData<?> convertToExcelData(Integer value,
                                                                                      com.alibaba.excel.metadata.property.ExcelContentProperty contentProperty,
                                                                                      com.alibaba.excel.metadata.GlobalConfiguration globalConfiguration) {
            String text = switch (value) {
                case 1 -> "在职";
                case 0 -> "离职";
                default -> "未知";
            };
            return new com.alibaba.excel.metadata.data.WriteCellData<>(text);
        }

        @Override
        public Integer convertToJavaData(com.alibaba.excel.metadata.data.ReadCellData<?> cellData,
                                          com.alibaba.excel.metadata.property.ExcelContentProperty contentProperty,
                                          com.alibaba.excel.metadata.GlobalConfiguration globalConfiguration) {
            return null;
        }
    }
}
