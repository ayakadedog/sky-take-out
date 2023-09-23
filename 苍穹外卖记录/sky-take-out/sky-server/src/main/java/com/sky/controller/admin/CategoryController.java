package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/admin/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<Page> page(CategoryPageQueryDTO categoryPageQueryDTO ){

        String name = categoryPageQueryDTO.getName();
        Integer page = categoryPageQueryDTO.getPage();
        Integer pageSize = categoryPageQueryDTO.getPageSize();
        Integer type = categoryPageQueryDTO.getType();
        Page<Category> pageInfo = new Page(page,pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Category::getName, name);
        queryWrapper.eq(Objects.nonNull(type),Category::getType, type );
        queryWrapper.orderByAsc(Category::getSort);

        categoryService.page(pageInfo, queryWrapper);

        return Result.success(pageInfo);
    }

    /**
     * 条件查询  目测没个卵用
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("条件查询")
    public Result<List<Category>> list(Integer type) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq( Category::getType, type );
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);

        return Result.success(list);
    }

    /**
     * 禁用员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result noUse(@PathVariable Integer status, long id){

        categoryService.noUse(status, id);

        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result deleteCategory(Long id){

        categoryService.remove(id);
        return Result.success();

    }

    /**
     * 编辑分类信息
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑分类信息")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO){

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);

        //TODO 记得全局替换,自动装配变量
        Long empId = BaseContext.getCurrentId();
        category.setUpdateUser(empId);
        category.setUpdateTime(LocalDateTime.now());

        categoryService.updateById(category);
        return Result.success();
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO){

        String name = categoryService.getByName(categoryDTO.getName());
        if(name != null){
            return Result.error( name + MessageConstant.ALREADY_EXISTS);
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);

        //TODO 记得全局替换,自动装配变量
        Long empId = BaseContext.getCurrentId();
        category.setUpdateUser(empId);
        category.setUpdateTime(LocalDateTime.now());
        category.setStatus(StatusConstant.DISABLE);
        category.setCreateTime(LocalDateTime.now());
        category.setCreateUser(empId);
        categoryService.save(category);
        return Result.success();
    }
}
