package com.was.mapper;

import com.was.dto.PaySendVo;
import com.was.dto.PersonalAgreeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PersonalMapper {

    PersonalAgreeDto selectOganizationByCd(PersonalAgreeDto dto);

    int insPaySend(PaySendVo param);

    List<PaySendVo> selPayList(PaySendVo param);

    List<PaySendVo> selPayListAll();

    PaySendVo selPayInfo(PaySendVo param);
}
