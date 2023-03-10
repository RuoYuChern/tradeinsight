package com.tao.trade.infra.db.mapper;

import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.infra.db.model.CnStockDailyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CnStockDailyMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    long countByExample(CnStockDailyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int deleteByExample(CnStockDailyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int insert(CnStockDaily row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int insertSelective(CnStockDaily row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    List<CnStockDaily> selectByExample(CnStockDailyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    CnStockDaily selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int updateByExampleSelective(@Param("row") CnStockDaily row, @Param("example") CnStockDailyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int updateByExample(@Param("row") CnStockDaily row, @Param("example") CnStockDailyExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int updateByPrimaryKeySelective(CnStockDaily row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.cn_stock_daily
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    int updateByPrimaryKey(CnStockDaily row);
}