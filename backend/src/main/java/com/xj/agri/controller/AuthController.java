package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.dto.LoginRequest;
import com.xj.agri.dto.LoginResponse;
import com.xj.agri.entity.OperationLog;
import com.xj.agri.entity.SysUser;
import com.xj.agri.mapper.OperationLogMapper;
import com.xj.agri.mapper.SysUserMapper;
import com.xj.agri.util.JwtUtil;
import com.xj.agri.util.PasswordUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证")
public class AuthController {
    private final SysUserMapper sysUserMapper;
    private final OperationLogMapper operationLogMapper;
    private final JwtUtil jwtUtil;

    public AuthController(SysUserMapper sysUserMapper, OperationLogMapper operationLogMapper, JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.operationLogMapper = operationLogMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>()
                .eq("username", request.username())
                .last("limit 1"));
        if (user == null || Boolean.FALSE.equals(user.getEnabled())
                || !PasswordUtil.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        OperationLog log = new OperationLog();
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setModuleName("认证");
        log.setOperationType("登录");
        log.setRequestUri(servletRequest.getRequestURI());
        log.setRequestMethod(servletRequest.getMethod());
        log.setIpAddress(servletRequest.getRemoteAddr());
        log.setResultStatus("成功");
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return ApiResponse.ok(new LoginResponse(token, user.getUsername(), user.getRealName(), user.getRole()));
    }

    @GetMapping("/me")
    public ApiResponse<SysUser> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPasswordHash(null);
        return ApiResponse.ok(user);
    }
}
