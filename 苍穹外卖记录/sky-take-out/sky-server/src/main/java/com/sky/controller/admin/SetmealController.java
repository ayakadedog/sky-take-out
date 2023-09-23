package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;

import com.sky.entity.Setmeal;
import com.sky.mapper.SetmealMapper;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Resource
    private SetmealService setmealService;
    @Resource
    private CategoryService categoryService;

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<Page> page(SetmealPageQueryDTO  setmealPageQueryDTO ){

        String name = setmealPageQueryDTO.getName();
        Integer page = setmealPageQueryDTO.getPage();
        Integer pageSize = setmealPageQueryDTO.getPageSize();
        Integer status = setmealPageQueryDTO.getStatus();
        Integer categoryQueryById = setmealPageQueryDTO.getCategoryId();

        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealVO> voDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.eq(Objects.nonNull(status),Setmeal::getStatus, status );
        queryWrapper.eq(Objects.nonNull(categoryQueryById),Setmeal::getCategoryId, categoryQueryById );
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, voDtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealVO> list = records.stream().map((add) -> {

            SetmealVO dishVO = new SetmealVO();

            BeanUtils.copyProperties(add, dishVO);

            Long categoryId = add.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishVO.setCategoryName(categoryName);
            }
            return dishVO;
        }).collect(Collectors.toList());

        voDtoPage.setRecords(list);
        return Result.success(voDtoPage);
    }

    /**
     * 启禁用套餐
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启禁用套餐")
    //    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result noUse(@PathVariable Integer status, long id){

        setmealService.noUse(status, id);

        return Result.success();
    }

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
//    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDto.categoryId")
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDto){

        String name = setmealService.getByName(setmealDto.getName());

        if(name != null){
            return Result.error(name + MessageConstant.ALREADY_EXISTS);
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto,setmeal);

        Long empId = BaseContext.getCurrentId();
        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setCreateUser(empId);
        setmeal.setUpdateUser(empId);

        setmealService.save(setmeal);
        setmealDto.setId(setmeal.getId());
        setmealService.saveWithDish(setmealDto);

        return Result.success();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询")
    public Result<SetmealDTO> get(@PathVariable Long id) {

        SetmealDTO setmealDto = setmealService.getByIdWithDish(id);

        return Result.success(setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    //    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDto){

        setmealService.updateWithDish(setmealDto);

        return Result.success(MessageConstant.EDIT_SUCCESS);
    }

    /**
     * 删除
     * @param ids
     * @return
     */
//    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    @DeleteMapping
    @ApiOperation("删除")
    public Result deleteByIds(@RequestParam("ids") List<Long> ids){

        setmealService.deleteWithDish(ids);
        return Result.success(MessageConstant.DISH_DEL_SUC);
    }

}
