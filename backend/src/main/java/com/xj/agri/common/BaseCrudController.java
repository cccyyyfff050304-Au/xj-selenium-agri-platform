package com.xj.agri.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Validated
public abstract class BaseCrudController<T> {
    protected abstract BaseMapper<T> mapper();

    protected QueryWrapper<T> buildQuery(String keyword) {
        return new QueryWrapper<>();
    }

    @GetMapping
    public ApiResponse<Page<T>> page(@RequestParam Map<String, String> params) {
        long current = parseLong(params.getOrDefault("current", params.getOrDefault("page", "1")), 1);
        long size = parseLong(params.getOrDefault("size", "10"), 10);
        String keyword = params.get("keyword");
        return ApiResponse.ok(mapper().selectPage(Page.of(current, size), buildQuery(keyword, params)));
    }

    @GetMapping("/{id}")
    public ApiResponse<T> detail(@PathVariable Long id) {
        T entity = mapper().selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        return ApiResponse.ok(entity);
    }

    @PostMapping
    public ApiResponse<T> create(@Valid @RequestBody T body) {
        mapper().insert(body);
        return ApiResponse.ok("创建成功", body);
    }

    @PutMapping("/{id}")
    public ApiResponse<T> update(@PathVariable Long id, @Valid @RequestBody T body) {
        var accessor = PropertyAccessorFactory.forBeanPropertyAccess(body);
        if (accessor.isWritableProperty("id")) {
            accessor.setPropertyValue("id", id);
        }
        mapper().updateById(body);
        return ApiResponse.ok("更新成功", body);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        mapper().deleteById(id);
        return ApiResponse.ok("删除成功", true);
    }

    protected QueryWrapper<T> buildQuery(String keyword, Map<String, String> params) {
        return buildQuery(keyword);
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return fallback;
        }
    }
}
