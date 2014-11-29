package com.mt.main;

import java.util.List;

import com.mt.sql.batchUpdate;
import com.mt.stockprice.StockPriceURLUtil;

public class ManulEntry {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//单跑一个股票的存量
		String sql = "insert into stock_day(stock_id,market,volume,open,high,close,low,chg,percent,turnrate,ma5,ma10,ma20,ma30,ema12,ema26,dif,dea,macd,d_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		List<List> allHisStockInfoByCode = StockPriceURLUtil.getAllHisStockInfoByCode("sh600429");
		batchUpdate.batchUpdate(sql, allHisStockInfoByCode, 20);
	}

}
