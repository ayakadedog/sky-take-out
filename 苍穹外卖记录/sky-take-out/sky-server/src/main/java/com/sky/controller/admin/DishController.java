package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Resource
    private DishService dishService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<Page> page(DishPageQueryDTO dishPageQueryDTO ){

        String name = dishPageQueryDTO.getName();
        Integer page = dishPageQueryDTO.getPage();
        Integer pageSize = dishPageQueryDTO.getPageSize();
        Integer status = dishPageQueryDTO.getStatus();
        Integer categoryQueryById = dishPageQueryDTO.getCategoryId();

        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishVO> voDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.eq(Objects.nonNull(status),Dish::getStatus, status );
        queryWrapper.eq(Objects.nonNull(categoryQueryById),Dish::getCategoryId, categoryQueryById );
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, voDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();

        List<DishVO> list = records.stream().map((add) -> {

            DishVO dishVO = new DishVO();

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
     * 根据条件查询对应的菜品数据
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 停售菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用菜品")
    public Result noUse(@PathVariable Integer status, long id){

        dishService.noUse(status, id);

        //TODO
       //cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据id删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除")
    public Result deleteByIds(@RequestParam("ids") List<Long> ids){

        List<DishDTO> list = dishService.deleteWithFlavor(ids);

        Set keys = redisTemplate.keys("dish_");
        redisTemplate.delete(keys);

        return Result.success(MessageConstant.DISH_DEL_SUC);
    }

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){

        String name = dishService.getByName(dishDTO.getName());
        if(name != null){
            return Result.error(MessageConstant.ALREADY_EXISTS);
        }

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        Long empId = BaseContext.getCurrentId();
        dish.setCreateTime(LocalDateTime.now());
        dish.setCreateUser(empId);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(empId);

        dishService.save(dish);

        dishService.saveWithFlavor(dishDTO,dish.getId());
        //TODO
        //cleanCache("dish_" + dishDTO.getCategoryId());


        return Result.success(dishDTO.getName() + MessageConstant.ADD_SUC);
    }

    /**
     * 根据id查询信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询信息")
    public Result<DishDTO> get(@PathVariable Long id){

        DishDTO dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDto){

        if (dishDto.getStatus() == StatusConstant.ENABLE )
            return Result.error(MessageConstant.DISH_ON_SALE_CH);
        dishService.updateWithFlavor(dishDto);
        //TODO
        //cleanCache("dish_*");


        return Result.success(MessageConstant.EDIT_SUCCESS);
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){

        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
