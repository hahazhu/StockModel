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
		
		//��ȡ���µ�ȫ����Ʊ����
		List stockCodeList = StockCodeURLUtil.getStockListFromSina();
		//���
		StockCodeDbUtil.refreshStockCodeInDB(stockCodeList);
		
		//����Ƿ����¹�,���¹������������
		List newStock = StockCodeDbUtil.getNewStock();
		if(newStock!=null){
			System.err.println(newStock);
//			StockPriceURLUtil.importHisFromListAfterIndex(0,newStock);
		}
		
		
		if(flag.equals("history")){
			//�������
//			StockPriceURLUtil.importHisFromListAfterIndex(0,stockCodeList );
			StockPriceURLUtil.importHisFromListAfterCode("sh600530",stockCodeList );
		}else if(flag.equals("daily")){
			//ÿ������
			List<List> content=StockPriceURLUtil.getTodayStockInfo(0, stockCodeList);
			//���뵱�ռ۸�
			StockPriceDbUtil.importDailyPrice(content);
			//����macd��ָ��
			StockPriceDbUtil.calDailyPrice(stockCodeList);
			
		}
		
				
	}

	

}
