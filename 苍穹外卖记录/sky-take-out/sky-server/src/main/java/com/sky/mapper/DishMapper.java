package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Update("update dish set status = #{status} where id = #{id}")
    Integer updateStatusById(Integer status,Long id);

    @Select("select * from dish where name = #{name}")
    Dish getByUsername(String name);

    List<Dish> list(Dish dish);

    Integer countByMap(Map map);
}
