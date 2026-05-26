package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.entity.SysUser;
import com.xj.agri.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户")
public class SysUserController extends BaseCrudController<SysUser> {
    private final SysUserMapper sysUserMapper;

    public SysUserController(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    protected BaseMapper<SysUser> mapper() {
        return sysUserMapper;
    }

    @Override
    protected QueryWrapper<SysUser> buildQuery(String keyword) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("username", keyword).or().like("real_name", keyword));
        }
        return wrapper.orderByDesc("id");
    }
}
