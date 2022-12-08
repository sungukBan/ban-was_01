package com.oauth.mapper;

import com.oauth.model.HanaUsers;
import org.apache.ibatis.annotations.Mapper;

import javax.transaction.Transactional;

@Mapper
@Transactional
public interface HanaUsersMapper {

    int insertHanaUsers(HanaUsers hanaUsers);

    HanaUsers selectHanaUsers(HanaUsers hanaUsers);
}
