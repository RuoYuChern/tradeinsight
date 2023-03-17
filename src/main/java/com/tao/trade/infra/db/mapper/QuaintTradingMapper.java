package com.tao.trade.infra.db.mapper;

import com.tao.trade.infra.db.model.QuaintTrading;
import com.tao.trade.infra.db.model.QuaintTradingExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface QuaintTradingMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    long countByExample(QuaintTradingExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int deleteByExample(QuaintTradingExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int insert(QuaintTrading row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int insertSelective(QuaintTrading row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    List<QuaintTrading> selectByExample(QuaintTradingExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    QuaintTrading selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int updateByExampleSelective(@Param("row") QuaintTrading row, @Param("example") QuaintTradingExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int updateByExample(@Param("row") QuaintTrading row, @Param("example") QuaintTradingExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int updateByPrimaryKeySelective(QuaintTrading row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.quaint_trading
     *
     * @mbg.generated Thu Mar 16 21:07:55 CST 2023
     */
    int updateByPrimaryKey(QuaintTrading row);
}