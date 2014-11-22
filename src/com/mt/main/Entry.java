package com.mt.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mt.common.Util;
import com.mt.sql.MysqlUtil;
import com.mt.sql.batchUpdate;
import com.mt.stockcode.StockCodeDbUtil;
import com.mt.stockcode.StockCodeURLUtil;
import com.mt.stockprice.StockPriceDbUtil;
import com.mt.stockprice.StockPriceURLUtil;

public class Entry {
	
	public static void main(String[] args) throws Exception {
		
		String flag="test";
		
		//获取最新的全量股票代码
		List stockCodeList = StockCodeURLUtil.getStockListFromSina();
		//入库
		StockCodeDbUtil.refreshStockCodeInDB(stockCodeList);
		
		//检查是否有新股,有新股则导入存量代码
		List newStock = StockCodeDbUtil.getNewStock();
		if(newStock!=null){
			System.err.println(newStock);
//			StockPriceURLUtil.importHisFromListAfterIndex(0,newStock);
		}
		
		
		if(flag.equals("history")){
			//导入存量
//			StockPriceURLUtil.importHisFromListAfterIndex(0,stockCodeList );
			StockPriceURLUtil.importHisFromListAfterCode("sh600530",stockCodeList );
		}else if(flag.equals("daily")){
			//每日增量
			List<List> content=StockPriceURLUtil.getTodayStockInfo(0, stockCodeList);
			//导入当日价格
			StockPriceDbUtil.importDailyPrice(content);
			//计算macd等指标
			StockPriceDbUtil.calDailyPrice(stockCodeList);
			
		}
		
				
	}

	

}
