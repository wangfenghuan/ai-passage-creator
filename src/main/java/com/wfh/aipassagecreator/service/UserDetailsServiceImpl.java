package com.wfh.aipassagecreator.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.wfh.aipassagecreator.mapper.UserMapper;
import com.wfh.aipassagecreator.model.entity.User;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @Title: UserDetailsService
 * @Author wangfenghuan
 * @Package com.wfh.aipassagecreator.service
 * @Date 2026/3/11 10:25
 * @description: Spring Security 用户详情服务实现
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", username);
        User user = userMapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return user;
    }
}
