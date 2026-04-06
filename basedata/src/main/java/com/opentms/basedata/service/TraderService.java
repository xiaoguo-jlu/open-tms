package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.TraderDTO;
import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.vo.TraderVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TraderService extends IService<Trader> {

    IPage<TraderVO> queryPage(String keyword, String status, int pageNo, int pageSize);

    TraderVO getById(Long id);

    boolean save(TraderDTO dto);

    boolean update(TraderDTO dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<TraderVO> listAll();
}
