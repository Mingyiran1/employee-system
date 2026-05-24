package com.whtc.employee.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.dto.InsuranceEmployeeImportDTO;
import com.whtc.employee.entity.InsuredCompany;
import com.whtc.employee.entity.InsuranceEmployee;
import com.whtc.employee.entity.PremiumConfig;
import com.whtc.employee.entity.Supplier;
import com.whtc.employee.mapper.InsuredCompanyMapper;
import com.whtc.employee.mapper.InsuranceEmployeeMapper;
import com.whtc.employee.mapper.PremiumConfigMapper;
import com.whtc.employee.mapper.SupplierMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保险员工Excel导入监听器
 * 注意：此类不是Spring Bean，每次导入需要创建新实例
 */
@Slf4j
public class InsuranceEmployeeImportListener implements ReadListener<InsuranceEmployeeImportDTO> {

    private static final int BATCH_COUNT = 100;
    private static final int MAX_ERROR_MESSAGES = 100;

    private List<InsuranceEmployee> cachedDataList;
    private final List<String> errorMessages = new ArrayList<>();

    private int successCount = 0;
    private int errorCount = 0;

    // Mapper依赖通过构造函数传入
    private final InsuranceEmployeeMapper insuranceEmployeeMapper;
    private final InsuredCompanyMapper insuredCompanyMapper;
    private final SupplierMapper supplierMapper;
    private final PremiumConfigMapper premiumConfigMapper;

    // 缓存
    private final Map<String, Long> companyCache = new HashMap<>();
    private final Map<String, Long> supplierCache = new HashMap<>();
    private final Map<String, PremiumConfig> premiumCache = new HashMap<>();

    // 预加载的静态数据
    private final List<InsuredCompany> allCompanies;
    private final List<Supplier> allSuppliers;
    private final List<PremiumConfig> allPremiumConfigs;

    /**
     * 构造函数，传入必要的依赖
     */
    public InsuranceEmployeeImportListener(
            InsuranceEmployeeMapper insuranceEmployeeMapper,
            InsuredCompanyMapper insuredCompanyMapper,
            SupplierMapper supplierMapper,
            PremiumConfigMapper premiumConfigMapper) {
        this.insuranceEmployeeMapper = insuranceEmployeeMapper;
        this.insuredCompanyMapper = insuredCompanyMapper;
        this.supplierMapper = supplierMapper;
        this.premiumConfigMapper = premiumConfigMapper;

        // 预加载所有公司、供应商和保费配置数据
        this.allCompanies = loadAllCompanies();
        this.allSuppliers = loadAllSuppliers();
        this.allPremiumConfigs = loadAllPremiumConfigs();

        // 初始化缓存
        allCompanies.forEach(c -> companyCache.put(c.getName(), c.getId()));
        allSuppliers.forEach(s -> supplierCache.put(s.getName(), s.getId()));
        allPremiumConfigs.forEach(p -> premiumCache.put(p.getJobType(), p));

        // 初始化数据列表
        this.cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

        log.info("导入监听器初始化完成，预加载{}家公司，{}家供应商，{}种工种配置",
                allCompanies.size(), allSuppliers.size(), allPremiumConfigs.size());
    }

    private List<InsuredCompany> loadAllCompanies() {
        QueryWrapper<InsuredCompany> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return insuredCompanyMapper.selectList(wrapper);
    }

    private List<Supplier> loadAllSuppliers() {
        QueryWrapper<Supplier> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return supplierMapper.selectList(wrapper);
    }

    private List<PremiumConfig> loadAllPremiumConfigs() {
        QueryWrapper<PremiumConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return premiumConfigMapper.selectList(wrapper);
    }

    @Override
    public void invoke(InsuranceEmployeeImportDTO data, AnalysisContext context) {
        int rowNum = context.readRowHolder().getRowIndex() + 1;

        // 限制导入行数不超过10000行
        if (successCount + errorCount >= 10000) {
            if (errorMessages.size() < MAX_ERROR_MESSAGES) {
                errorMessages.add("导入数据超过最大限制（10000行），请分批导入");
            }
            return;
        }

        // 姓名校验
        if (!StringUtils.hasText(data.getName())) {
            addErrorMessage("第" + rowNum + "行：姓名不能为空");
            errorCount++;
            return;
        }

        // 身份证号校验
        if (!StringUtils.hasText(data.getIdCard())) {
            addErrorMessage("第" + rowNum + "行：身份证号不能为空");
            errorCount++;
            return;
        }

        // 身份证号格式校验（18位）
        if (!isValidIdCard(data.getIdCard().trim())) {
            addErrorMessage("第" + rowNum + "行：身份证号格式不正确");
            errorCount++;
            return;
        }

        InsuranceEmployee employee = new InsuranceEmployee();
        employee.setName(data.getName().trim());
        employee.setIdCard(data.getIdCard().trim());
        employee.setPhone(StringUtils.hasText(data.getPhone()) ? data.getPhone().trim() : null);
        employee.setEmail(StringUtils.hasText(data.getEmail()) ? data.getEmail().trim() : null);
        employee.setHireDate(data.getHireDate());
        employee.setRemark(data.getRemark());
        employee.setStatus(1);

        // 投保公司校验
        if (StringUtils.hasText(data.getCompanyName())) {
            Long companyId = companyCache.get(data.getCompanyName().trim());
            if (companyId == null) {
                addErrorMessage("第" + rowNum + "行：投保公司'" + data.getCompanyName() + "'不存在");
                errorCount++;
                return;
            }
            employee.setCompanyId(companyId);
        }

        // 供应商校验
        if (StringUtils.hasText(data.getSupplierName())) {
            Long supplierId = supplierCache.get(data.getSupplierName().trim());
            if (supplierId == null) {
                addErrorMessage("第" + rowNum + "行：供应商'" + data.getSupplierName() + "'不存在");
                errorCount++;
                return;
            }
            employee.setSupplierId(supplierId);
        }

        // 工种和保费配置
        if (StringUtils.hasText(data.getJobType())) {
            String jobType = data.getJobType().trim();
            employee.setJobType(jobType);

            PremiumConfig config = premiumCache.get(jobType);
            if (config != null && config.getAnnualPremium() != null) {
                employee.setAnnualPremium(config.getAnnualPremium());
            } else {
                log.warn("第{}行：工种'{}'未找到对应的保费配置", rowNum, jobType);
            }
        }

        // 设置创建人
        Long currentUserId = BaseContext.getCurrentUserId();
        if (currentUserId != null) {
            employee.setCreateBy(currentUserId);
        }

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        cachedDataList.add(employee);
        successCount++;

        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("Excel导入解析完成，成功{}条，失败{}条", successCount, errorCount);
        if (!errorMessages.isEmpty()) {
            log.warn("导入错误信息（前{}条）：\n{}", errorMessages.size(),
                    String.join("\n", errorMessages.subList(0, Math.min(errorMessages.size(), 20))));
        }
    }

    private void saveData() {
        if (cachedDataList.isEmpty()) {
            return;
        }

        // 使用MyBatis Plus的批量插入方法
        // 注意：实际插入在Service层通过事务控制
        // 这里只是将数据保留在列表中，等所有数据解析完成后统一插入
        log.debug("暂存{}条数据，等待批量插入", cachedDataList.size());
    }

    /**
     * 身份证号格式校验
     * 简单校验：18位，前17位为数字，最后一位为数字或X
     */
    private boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return false;
        }
        String regex = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$";
        return idCard.matches(regex);
    }

    private void addErrorMessage(String message) {
        if (errorMessages.size() < MAX_ERROR_MESSAGES) {
            errorMessages.add(message);
        } else if (errorMessages.size() == MAX_ERROR_MESSAGES) {
            errorMessages.add("...错误信息过多，仅显示前" + MAX_ERROR_MESSAGES + "条...");
        }
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    /**
     * 获取所有解析成功的数据
     */
    public List<InsuranceEmployee> getAllData() {
        return Collections.unmodifiableList(cachedDataList);
    }
}
