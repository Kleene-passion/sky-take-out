package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam List<Long> ids){
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐状态起售停售
     * @param status
     * @param id
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐状态起售停售")
    public Result<Void> startOrStop(@PathVariable Integer status,
                                    @RequestParam(required = false) Long id,
                                    @RequestParam(required = false) List<Long> ids) {
        if (id != null) {
            setmealService.startOrStop(status, Collections.singletonList(id));
        } else if (ids != null && !ids.isEmpty()) {
            setmealService.startOrStop(status, ids);
        }

        // 将所有的菜品缓存数据清理掉，所有以dish_开头的key
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 根据ID查询套餐（含菜品明细）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐（含菜品明细）")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return Result.success(setmealService.getByIdWithDish(id));
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result<Void> update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }
}
