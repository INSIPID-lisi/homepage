package com.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homepage.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(String email);
}
