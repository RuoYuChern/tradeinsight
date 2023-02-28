package com.tao.trade.infra.db.mapper;

import com.tao.trade.infra.db.model.DataDeltaDate;
import com.tao.trade.infra.db.model.DataDeltaDateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataDeltaDateMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    long countByExample(DataDeltaDateExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int deleteByExample(DataDeltaDateExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int deleteByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int insert(DataDeltaDate row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int insertSelective(DataDeltaDate row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    List<DataDeltaDate> selectByExample(DataDeltaDateExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    DataDeltaDate selectByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int updateByExampleSelective(@Param("row") DataDeltaDate row, @Param("example") DataDeltaDateExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int updateByExample(@Param("row") DataDeltaDate row, @Param("example") DataDeltaDateExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int updateByPrimaryKeySelective(DataDeltaDate row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.data_delta_date
     *
     * @mbg.generated Mon Feb 27 17:52:39 CST 2023
     */
    int updateByPrimaryKey(DataDeltaDate row);
}