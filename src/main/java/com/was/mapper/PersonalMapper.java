package com.was.mapper;

import com.was.dto.PersonalAgreeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PersonalMapper {

    PersonalAgreeDto selectMNGAgree(PersonalAgreeDto dto);
    PersonalAgreeDto selectOganizationByCd(PersonalAgreeDto dto);
    int selectAgreeOne(PersonalAgreeDto dto);
    void insertAgree(PersonalAgreeDto dto);
}
