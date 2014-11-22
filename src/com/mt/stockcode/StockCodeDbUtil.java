package com.mt.stockcode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mt.sql.MysqlUtil;
import com.mt.sql.batchUpdate;

public class StockCodeDbUtil {

	public static void refreshStockCodeInDB(List stockCode) throws SQLException{
		   //对股票代码拆分为 代码+市场
		   List params = new ArrayList();
		   for(Iterator it=stockCode.iterator();it.hasNext();){
			   String tmp = (String) it.next();
			   List param = new ArrayList();
			   param.add(0,tmp.substring(2));
			   param.add(1,tmp.substring(0,2));
			   params.add(param);
		   }
		   MysqlUtil mysql= new MysqlUtil();
		   mysql.getConnection();
		   mysql.updateByPreparedStatement("delete from gao_stock.stock_info_tmp ", null);
		   batchUpdate.batchUpdate("insert into gao_stock.stock_info_tmp(stock_id,market) values(?,?)", params, 2);
		   mysql.releaseConn();
		   
	   }

	public static List getNewStock(){
		   MysqlUtil mysql= new MysqlUtil();
		   mysql.getConnection();
		   List result=null;
		   try {
			   result = mysql.findModeResult("select stock_id,market from gao_stock.stock_info_tmp a where (stock_id) not in (select stock_id from gao_stock.stock_info)", null);
		   } catch (SQLException e) {
			   e.printStackTrace();
		   }
		   return result;
		   
	   }

}
