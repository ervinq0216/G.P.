package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.Result;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Favorite;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 切换收藏状态 (已收藏则取消，未收藏则添加)
     */
    @PostMapping("/toggle")
    public Result<String> toggleFavorite(@RequestBody Favorite fav) {
        if (fav.getPatientId() == null || fav.getDoctorId() == null) {
            return Result.error("参数错误");
        }

        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPatientId, fav.getPatientId())
                .eq(Favorite::getDoctorId, fav.getDoctorId()));

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return Result.success("已取消收藏");
        } else {
            fav.setCreatedTime(LocalDateTime.now());
            favoriteMapper.insert(fav);
            return Result.success("收藏成功");
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long patientId, @RequestParam Long doctorId) {
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPatientId, patientId)
                .eq(Favorite::getDoctorId, doctorId));
        return Result.success(count > 0);
    }

    /**
     * 获取我的收藏列表 (包含医生详情)
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getMyFavorites(@RequestParam Long patientId) {
        List<Favorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPatientId, patientId)
                .orderByDesc(Favorite::getCreatedTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite f : favs) {
            Doctor d = doctorMapper.selectById(f.getDoctorId());
            if (d != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("doctorId", d.getId());
                map.put("realName", d.getRealName());
                map.put("avatar", d.getAvatar());
                map.put("jobNumber", d.getJobNumber());

                Department dept = departmentMapper.selectById(d.getDeptId());
                map.put("deptName", dept != null ? dept.getName() : "未知科室");

                result.add(map);
            }
        }
        return Result.success(result);
    }
}