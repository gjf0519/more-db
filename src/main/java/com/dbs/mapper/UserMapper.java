package com.dbs.mapper;

import com.dbs.entity.User;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
@CacheNamespace
public interface UserMapper {

    @Select("SELECT * FROM simp_user WHERE id=#{id}")
    User getUserById(int id);

    @Insert("INSERT INTO simp_user (username,password,role,permission)" +
            " VALUES (#{username},#{password},#{role},#{permission})")
    void insertUser(User user);
}
