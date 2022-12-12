package com.was.mapper;

import com.was.dto.PaySendVo;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BanWasMapper {

    int insPaySend(PaySendVo param);

    List<PaySendVo> selPayList(PaySendVo param);

    List<PaySendVo> selPayListAll();

    PaySendVo selPayInfo(PaySendVo param);
}
