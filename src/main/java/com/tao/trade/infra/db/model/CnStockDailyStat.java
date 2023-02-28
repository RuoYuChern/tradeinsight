package com.tao.trade.infra.db.model;

import java.math.BigDecimal;
import java.util.Date;

public class CnStockDailyStat {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.id
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.symbol
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private String symbol;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private BigDecimal price;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.sma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private BigDecimal smaPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.ma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private BigDecimal maPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.ema_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private BigDecimal emaPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.wma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private BigDecimal wmaPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.cn_stock_daily_stat.trade_date
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    private Date tradeDate;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.id
     *
     * @return the value of public.cn_stock_daily_stat.id
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.id
     *
     * @param id the value for public.cn_stock_daily_stat.id
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.symbol
     *
     * @return the value of public.cn_stock_daily_stat.symbol
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.symbol
     *
     * @param symbol the value for public.cn_stock_daily_stat.symbol
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.price
     *
     * @return the value of public.cn_stock_daily_stat.price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.price
     *
     * @param price the value for public.cn_stock_daily_stat.price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.sma_price
     *
     * @return the value of public.cn_stock_daily_stat.sma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public BigDecimal getSmaPrice() {
        return smaPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.sma_price
     *
     * @param smaPrice the value for public.cn_stock_daily_stat.sma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setSmaPrice(BigDecimal smaPrice) {
        this.smaPrice = smaPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.ma_price
     *
     * @return the value of public.cn_stock_daily_stat.ma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public BigDecimal getMaPrice() {
        return maPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.ma_price
     *
     * @param maPrice the value for public.cn_stock_daily_stat.ma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setMaPrice(BigDecimal maPrice) {
        this.maPrice = maPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.ema_price
     *
     * @return the value of public.cn_stock_daily_stat.ema_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public BigDecimal getEmaPrice() {
        return emaPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.ema_price
     *
     * @param emaPrice the value for public.cn_stock_daily_stat.ema_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setEmaPrice(BigDecimal emaPrice) {
        this.emaPrice = emaPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.wma_price
     *
     * @return the value of public.cn_stock_daily_stat.wma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public BigDecimal getWmaPrice() {
        return wmaPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.wma_price
     *
     * @param wmaPrice the value for public.cn_stock_daily_stat.wma_price
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setWmaPrice(BigDecimal wmaPrice) {
        this.wmaPrice = wmaPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.cn_stock_daily_stat.trade_date
     *
     * @return the value of public.cn_stock_daily_stat.trade_date
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public Date getTradeDate() {
        return tradeDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.cn_stock_daily_stat.trade_date
     *
     * @param tradeDate the value for public.cn_stock_daily_stat.trade_date
     *
     * @mbg.generated Thu Feb 23 21:22:50 CST 2023
     */
    public void setTradeDate(Date tradeDate) {
        this.tradeDate = tradeDate;
    }
}