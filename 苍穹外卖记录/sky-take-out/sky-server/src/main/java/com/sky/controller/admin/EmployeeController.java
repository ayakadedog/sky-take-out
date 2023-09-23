package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;
    @Resource
    private EmployeeMapper employeeMapper;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){

        Employee employee = employeeMapper.getByUsername(employeeDTO.getUsername());
        if (employee != null ){
            return Result.error("用户" + employeeDTO.getUsername() + MessageConstant.ALREADY_EXISTS);
        }

        employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        employee.setPassword(PasswordConstant.DEFAULT_PASSWORD);
        employee.setStatus(StatusConstant.ENABLE);

        employee.setCreateTime(LocalDateTime.now());
        employee.setCreateUser(BaseContext.getCurrentId());

        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeService.save(employee);

        return Result.success();
    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<Page> page(EmployeePageQueryDTO employeePageQueryDTO ){

        String name = employeePageQueryDTO.getName();
        Integer page = employeePageQueryDTO.getPage();
        Integer pageSize = employeePageQueryDTO.getPageSize();

        Page<Employee> pageInfo = new Page(page,pageSize);

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByAsc(Employee::getCreateTime);

        employeeService.page(pageInfo, queryWrapper);

        return Result.success(pageInfo);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee>getById(@PathVariable Long id){

        Employee employee= employeeService.getById(id);
        employee.setPassword("****");

        return Result.success(employee);
    }

    /**
     * 启禁用员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启禁用员工账号")
    public Result noUse(@PathVariable Integer status,long id){

         employeeService.noUse(status,id);

        return Result.success();
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO){

        Employee emp = employeeService.getById(employeeDTO.getId());
        if(!emp.getUsername().equals(employeeDTO.getUsername())){
            Employee employee = employeeMapper.getByUsername(employeeDTO.getUsername());
            if (employee != null){
                Result.error("此用户名" + MessageConstant.ALREADY_EXISTS);
            }
        }

        employeeService.updateEmployee(employeeDTO);

        return Result.success();
    }

    /**
     * 用户修改密码
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改密码")
    public Result updatePassword(@RequestBody PasswordEditDTO passwordEditDTO){

        Long empId = BaseContext.getCurrentId();
        String oldPassword = passwordEditDTO.getOldPassword();
        String newPassword = passwordEditDTO.getNewPassword();

        Employee employee = employeeService.getById(empId);

        //TODO 没做md5加密

        String password = employee.getPassword();
        if (!password.equals(oldPassword)){
            return Result.error(MessageConstant.PASSWORD_ERROR);
        }

        employeeMapper.updatePasswordByIdInteger(newPassword,empId);

        return Result.success(MessageConstant.PASSWORD_EDIT_SUCCESS);
    }
}
