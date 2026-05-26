package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.AtTransferDTO;
import com.opentms.basedata.vo.AtTransferVO;

public interface AtTransferService {

    IPage<AtTransferVO> queryPage(AtTransferDTO dto, int pageNum, int pageSize);

    AtTransferVO getById(Long id);

    AtTransferVO save(AtTransferDTO dto);

    AtTransferVO update(AtTransferDTO dto);

    void delete(Long id);

    void submit(Long id);

    void execute(Long id);

    void cancel(Long id);
}
