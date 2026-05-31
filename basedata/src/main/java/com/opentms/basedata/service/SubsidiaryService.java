package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.SubsidiaryDTO;
import com.opentms.basedata.vo.SubsidiaryVO;

/**
 * 子公司Service接口
 */
public interface SubsidiaryService {

    /**
     * 分页查询
     */
    Page<SubsidiaryVO> queryPage(String keyword, String status, int pageNum, int pageSize);

    /**
     * 根据ID查询
     */
    SubsidiaryVO getSubsidiaryById(Long id);

    /**
     * 保存
     */
    SubsidiaryVO saveSubsidiary(SubsidiaryDTO dto);

    /**
     * 更新
     */
    SubsidiaryVO updateSubsidiary(SubsidiaryDTO dto);

    /**
     * 删除
     */
    void deleteSubsidiary(Long id);

    /**
     * 检查编码是否存在
     */
    boolean checkCodeExists(String code, Long excludeId);
}