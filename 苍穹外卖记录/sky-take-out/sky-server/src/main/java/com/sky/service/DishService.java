package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 更改分类启禁用状态
     * @param status
     * @param id
     * @return
     */
    Void noUse(Integer status,Long id);
    /**
     * 通过名字查
     * @param name
     * @return
     */
    String getByName(String name);
    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 口味保存
     * @param dishDto
     * @param empId
     */
    void saveWithFlavor(DishDTO dishDto, Long empId);

    /**
     * 删除
     * @param ids
     * @return
     */
    List<DishDTO> deleteWithFlavor(List<Long> ids);

    /**
     * 通过id查
     * @param id
     * @return
     */
     DishDTO getByIdWithFlavor(Long id);

    /**
     * 更新
     * @param dishDto
     */
     void updateWithFlavor(DishDTO dishDto);

     //TODO
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

}
