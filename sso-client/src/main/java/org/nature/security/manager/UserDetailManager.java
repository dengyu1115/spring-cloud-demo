package org.nature.security.manager;

import org.apache.commons.lang3.StringUtils;
import org.nature.common.exception.BizWarn;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service("userDetailManager")
public class UserDetailManager implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            throw new BizWarn("用户不能为空");
        }
        // 调用方法查询用户
        User user = this.findUser(username);
        if (user == null) {
            throw new BizWarn("用户不存在");
        }
        return user;
    }

    private List<SimpleGrantedAuthority> getUserAuthorities(String username) {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_" + "ADMIN"));
    }

    private User findUser(String username) {
        if (!"nature".equals(username)) {
            return null;
        }
        List<SimpleGrantedAuthority> authorities = this.getUserAuthorities(username);
        /*
        前端传入明文密码，后端使用数据库存放的密文
         */
        String password = "$2a$10$CByRqAsNMxMVFr5fFrhHFu9iHWU.s56Lgqq56EY5.Wu6YNH0Xs/nu";
        return new User(username, password, authorities);
    }
}
