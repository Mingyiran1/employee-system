package com.whtc.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whtc.employee.common.PageResult;
import com.whtc.employee.context.BaseContext;
import com.whtc.employee.dto.InsuranceEmployeeDTO;
import com.whtc.employee.dto.InsuranceEmployeePageQueryDTO;
import com.whtc.employee.entity.InsuranceEmployee;
import com.whtc.employee.entity.InsuredCompany;
import com.whtc.employee.entity.PremiumConfig;
import com.whtc.employee.entity.Supplier;
import com.whtc.employee.entity.SysUser;
import com.whtc.employee.mapper.InsuranceEmployeeMapper;
import com.whtc.employee.mapper.InsuredCompanyMapper;
import com.whtc.employee.mapper.PremiumConfigMapper;
import com.whtc.employee.mapper.SupplierMapper;
import com.whtc.employee.service.InsuranceEmployeeService;
import com.whtc.employee.vo.InsuranceEmployeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InsuranceEmployeeServiceImpl extends ServiceImpl<InsuranceEmployeeMapper, InsuranceEmployee> implements InsuranceEmployeeService {

    @Autowired
    private InsuranceEmployeeMapper insuranceEmployeeMapper;

    @Autowired
    private PremiumConfigMapper premiumConfigMapper;

    @Autowired
    private InsuredCompanyMapper insuredCompanyMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public PageResult pageQuery(InsuranceEmployeePageQueryDTO queryDTO) {
        InsuranceEmployeePageQueryDTO condition = queryDTO == null ? new InsuranceEmployeePageQueryDTO() : queryDTO;
        Page<InsuranceEmployee> pageInfo = new Page<>(condition.getPage(), condition.getSize());
        QueryWrapper<InsuranceEmployee> wrapper = buildQueryWrapper(condition);
        wrapper.orderByDesc("create_time");
        Page<InsuranceEmployee> pageData = this.page(pageInfo, wrapper);
        return new PageResult(pageData.getTotal(), toVOList(pageData.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
    public void saveEmployee(InsuranceEmployeeDTO employeeDTO) {
        InsuranceEmployee employee = new InsuranceEmployee();
        BeanUtils.copyProperties(employeeDTO, employee);

        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        if (employee.getStatus() == 1) {
            employee.setLeaveDate(null);
        }

        calculateAnnualPremium(employee);

        SysUser currentUser = BaseContext.getCurrentUser();
        if (currentUser != null) {
            employee.setCreateBy(currentUser.getId());
        }

        this.save(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
    public void updateEmployee(InsuranceEmployeeDTO employeeDTO) {
        if (employeeDTO.getId() == null) {
            throw new IllegalArgumentException("员工ID不能为空");
        }

        InsuranceEmployee existing = insuranceEmployeeMapper.selectById(employeeDTO.getId());
        if (existing == null) {
            throw new IllegalArgumentException("员工不存在");
        }

        InsuranceEmployee employee = new InsuranceEmployee();
        BeanUtils.copyProperties(employeeDTO, employee);
        if (employee.getStatus() != null && employee.getStatus() == 1) {
            employee.setLeaveDate(null);
        }
        if (StringUtils.hasText(employeeDTO.getJobType())) {
            calculateAnnualPremium(employee);
        }

        this.updateById(employee);
    }

    @Override
    public InsuranceEmployeeVO getEmployeeById(Long id) {
        InsuranceEmployee employee = insuranceEmployeeMapper.selectEmployeeWithCompany(id);
        if (employee == null) {
            return null;
        }

        InsuranceEmployeeVO vo = new InsuranceEmployeeVO();
        BeanUtils.copyProperties(employee, vo);
        vo.setCompanyName(employee.getCompanyName());
        vo.setSupplierName(employee.getSupplierName());
        calculatePremium(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
    public void deleteById(Long id) {
        InsuranceEmployee existing = insuranceEmployeeMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("删除的员工ID列表不能为空");
        }
        long existingCount = this.count(new QueryWrapper<InsuranceEmployee>().in("id", ids));
        if (existingCount != ids.size()) {
            throw new IllegalArgumentException("存在不存在的员工记录");
        }
        this.removeByIds(ids);
    }

    @Override
    public List<InsuranceEmployeeVO> listAll(InsuranceEmployeePageQueryDTO queryDTO) {
        QueryWrapper<InsuranceEmployee> wrapper = buildQueryWrapper(queryDTO);
        wrapper.orderByDesc("create_time");
        return toVOList(this.list(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"dashboard:all", "dashboard:overview", "dashboard:dept", "dashboard:trend", "dashboard:gender"}, allEntries = true)
    public Map<String, Object> importBatch(List<InsuranceEmployee> employees) {
        Map<String, Object> result = new HashMap<>();

        if (employees == null || employees.isEmpty()) {
            result.put("successCount", 0);
            result.put("errorCount", 0);
            result.put("errorMessages", List.of());
            return result;
        }

        this.saveBatch(employees, 100);

        result.put("successCount", employees.size());
        result.put("errorCount", 0);
        result.put("errorMessages", List.of());

        log.info("批量导入保险员工完成，成功{}条", employees.size());
        return result;
    }

    private QueryWrapper<InsuranceEmployee> buildQueryWrapper(InsuranceEmployeePageQueryDTO queryDTO) {
        QueryWrapper<InsuranceEmployee> wrapper = new QueryWrapper<>();
        if (queryDTO == null) {
            return wrapper;
        }

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like("name", queryDTO.getName().trim());
        }
        if (StringUtils.hasText(queryDTO.getIdCard())) {
            wrapper.like("id_card", queryDTO.getIdCard().trim());
        }
        if (queryDTO.getCompanyId() != null) {
            wrapper.eq("company_id", queryDTO.getCompanyId());
        }
        if (queryDTO.getSupplierId() != null) {
            wrapper.eq("supplier_id", queryDTO.getSupplierId());
        }
        if (StringUtils.hasText(queryDTO.getJobType())) {
            wrapper.eq("job_type", queryDTO.getJobType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus());
        }
        if (queryDTO.getHireDateStart() != null) {
            wrapper.ge("hire_date", queryDTO.getHireDateStart());
        }
        if (queryDTO.getHireDateEnd() != null) {
            wrapper.le("hire_date", queryDTO.getHireDateEnd());
        }
        if (queryDTO.getLeaveDateStart() != null) {
            wrapper.ge("leave_date", queryDTO.getLeaveDateStart());
        }
        if (queryDTO.getLeaveDateEnd() != null) {
            wrapper.le("leave_date", queryDTO.getLeaveDateEnd());
        }
        return wrapper;
    }

    private List<InsuranceEmployeeVO> toVOList(List<InsuranceEmployee> employees) {
        if (employees == null || employees.isEmpty()) {
            return List.of();
        }

        Map<Long, String> companyNameMap = loadCompanyNameMap(employees);
        Map<Long, String> supplierNameMap = loadSupplierNameMap(employees);

        return employees.stream().map(employee -> {
            InsuranceEmployeeVO vo = new InsuranceEmployeeVO();
            BeanUtils.copyProperties(employee, vo);
            vo.setCompanyName(companyNameMap.get(employee.getCompanyId()));
            vo.setSupplierName(supplierNameMap.get(employee.getSupplierId()));
            calculatePremium(vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, String> loadCompanyNameMap(List<InsuranceEmployee> employees) {
        List<Long> companyIds = employees.stream()
                .map(InsuranceEmployee::getCompanyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (companyIds.isEmpty()) {
            return Map.of();
        }

        return insuredCompanyMapper.selectBatchIds(companyIds).stream()
                .collect(Collectors.toMap(InsuredCompany::getId, InsuredCompany::getName, (left, right) -> left));
    }

    private Map<Long, String> loadSupplierNameMap(List<InsuranceEmployee> employees) {
        List<Long> supplierIds = employees.stream()
                .map(InsuranceEmployee::getSupplierId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (supplierIds.isEmpty()) {
            return Map.of();
        }

        return supplierMapper.selectBatchIds(supplierIds).stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getName, (left, right) -> left));
    }

    private void calculateAnnualPremium(InsuranceEmployee employee) {
        if (!StringUtils.hasText(employee.getJobType())) {
            return;
        }

        PremiumConfig config = premiumConfigMapper.selectByJobType(employee.getJobType());
        if (config != null && config.getAnnualPremium() != null) {
            employee.setAnnualPremium(config.getAnnualPremium());
            log.info("工种 {} 自动计算年保费: {}", employee.getJobType(), config.getAnnualPremium());
        }
    }

    private void calculatePremium(InsuranceEmployeeVO vo) {
        if (vo.getAnnualPremium() == null || vo.getHireDate() == null) {
            return;
        }

        BigDecimal dailyPremium = vo.getAnnualPremium()
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);
        vo.setDailyPremium(dailyPremium);

        LocalDate startDate = vo.getHireDate();
        LocalDate endDate;
        if (vo.getStatus() != null && vo.getStatus() == 2 && vo.getLeaveDate() != null) {
            endDate = vo.getLeaveDate();
        } else {
            endDate = LocalDate.now();
        }

        long workingDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (workingDays < 0) {
            workingDays = 0;
        }

        BigDecimal realTimePremium = dailyPremium
                .multiply(BigDecimal.valueOf(workingDays))
                .setScale(2, RoundingMode.HALF_UP);
        vo.setRealTimePremium(realTimePremium);
    }
}
