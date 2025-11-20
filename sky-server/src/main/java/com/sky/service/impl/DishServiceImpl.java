package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入1条数据
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值
        Long dishId = dish.getId();

        // 向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

     /**
      * 菜品批量删除
      * @param ids
      */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否能够删除 --- 是否存在起售中的菜品？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                // 当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断当前菜品是否能够删除 --- 是否被套餐关联了？
        List<Long> setmealIds = setmealDishMapper.getSetmealDishIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品表中的菜品数据
        /*for (Long id : ids) {
            dishMapper.deleteById(id);
            // 删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }*/

        // 根据菜品id集合批量删除菜品数据
        // sql: delete from dish where id in (id1, id2, id3, ...)
        dishMapper.deleteByIds(ids);

        // 根据菜品id集合批量删除菜品关联的口味数据
        // sql: delete from dish_flavor where dish_id in (id1, id2, id3, ...)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品数据
        Dish dish = dishMapper.getById(id);

        // 根据菜品id查询口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        // 将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 修改菜品表基本信息
        dishMapper.update(dish);

        // 删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        // 重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 菜品起售/停售
     * @param status  1-起售  0-停售
     * @param id      菜品id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {

        // 0. 参数校验
        if (id == null || status == null) {
            throw new IllegalArgumentException("菜品状态修改失败：id 或 status 不能为空");
        }

        // 1. 如果是【停售】，先判断是否被启售中的套餐关联
        if (Objects.equals(status, StatusConstant.DISABLE)) {

            // 1.1 查出这个菜品关联的所有套餐ID
            List<Long> setmealIds =
                    setmealDishMapper.getSetmealIdsByDishIds(Collections.singletonList(id));
            if (setmealIds != null && !setmealIds.isEmpty()) {

                // 1.2 只统计“启售中”的套餐数量
                Integer enableCount = setmealMapper.countEnableByIds(setmealIds, StatusConstant.ENABLE);
                if (enableCount != null && enableCount > 0) {
                    // 有启售中的套餐在用这道菜，不能停售
                    throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
                }
            }
        }

        // 2. 真正更新菜品状态
        int n = dishMapper.updateStatus(status, id);
        if (n == 0) {
            // 数据库里根本没这条记录，或者参数没绑定上
            throw new RuntimeException("更新失败：请检查参数或记录是否存在");
        }

        // 3. 精准删除 Redis 缓存（按分类删）
        Dish dish = dishMapper.getById(id);
        if (dish != null && dish.getCategoryId() != null) {
            Long categoryId = dish.getCategoryId();
            // 这里的 key 规则要和用户端查询使用的一致，比如 user 端用的是 "dish_" + categoryId + "_1"
            String pattern = "dish_" + categoryId + "_*";

            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }
}
