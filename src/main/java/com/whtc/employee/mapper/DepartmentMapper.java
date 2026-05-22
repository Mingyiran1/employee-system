package com.whtc.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whtc.employee.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 查询指定部门的所有子部门ID（递归）
     *
     * @param parentId 父部门ID
     * @return 子部门ID列表
     */
    @Select("WITH RECURSIVE dept_tree AS (" +
            "    SELECT id FROM department WHERE parent_id = #{parentId} AND is_deleted = 0" +
            "    UNION ALL" +
            "    SELECT d.id FROM department d" +
            "    INNER JOIN dept_tree dt ON d.parent_id = dt.id" +
            "    WHERE d.is_deleted = 0" +
            ")" +
            "SELECT id FROM dept_tree")
    List<Long> selectChildDeptIds(@Param("parentId") Long parentId);
}
