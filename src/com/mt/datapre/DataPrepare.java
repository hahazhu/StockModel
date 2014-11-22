package com.mt.datapre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mt.common.Util;
import com.mt.sql.MysqlUtil;
import com.mt.sql.batchUpdate;

public class DataPrepare {
	private static String batchSql="insert into stock_day (stock_id,market,d_date,open,close,high,low,volume) values(?,?,?,?,?,?,?,?)";
	private static String hisBatchSql="insert into stock_day(stock_id,market,volume,open,high,close,low,chg,percent,turnrate,ma5,ma10,ma20,ma30,ema12,ema26,dif,dea,macd,d_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static String db = "/Users/hahazhu/myProject/StockModel/sina-stock-codes2.txt";
	private static final int COLUMNS = 37;
	private static List<String> codes = new ArrayList<String>();
	private static int allHisFlag=1;
	
	/*********************************************
	 * 
	 * 主程序
	 * 
	 *********************************************/
	public static void main(String[] args) {
		java.util.Date now_start = new java.util.Date(); 
		long start_time=now_start.getTime(); 
		getStockCode();
		if(allHisFlag==0){
			try {
				//	getAllStockInfo();
				@SuppressWarnings("rawtypes")
				List<List> content=getStockInfo(0);
				MysqlUtil jdbcUtils = new MysqlUtil();  
				jdbcUtils.getConnection();  
				String sql = "delete FROM stock_day where d_date=?"; 
				List<Object> params = new ArrayList<Object>(); 
				params.add(Util.GetNowDate()); 
				boolean flag = jdbcUtils.updateByPreparedStatement(sql, params);
				batchUpdate.batchUpdate(batchSql, content, 8);
				for(int i=0;i<codes.size();i++){
					String call = "upDayStockInfo"; 
			        List list = new ArrayList();
			        list.add(codes.get(i).toString().substring(2));
			        list.add(Util.GetNowDate());
					boolean res = jdbcUtils.callProc(call, list);
					System.out.println(codes.get(i).toString()+","+res);
				}			
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			try {
				//	getAllStockInfo();
				MysqlUtil jdbcUtils = new MysqlUtil();  
				jdbcUtils.getConnection();  
//				String sql = "delete FROM stock_day"; 
//				List<Object> params = new ArrayList<Object>(); 
//				//params.add(Util.GetNowDate()); 
//				boolean flag = jdbcUtils.updateByPreparedStatement(sql, params);
				getAllHisStockInfo(getIndexByCode("sh600567"));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		java.util.Date now_end = new java.util.Date();
		long end_time = now_end.getTime();
		long use_time = end_time - start_time;
		System.out.println("<<---本页生成耗时[" + use_time + "]毫秒("
				+ ((double) use_time) / 1000 + "秒)--->>");
	}
	private static int getIndexByCode(String stock){
		for(int i=0;i<codes.size();i++){
			if(codes.get(i).contains(stock))
				return i+1;
		}
		return 0;
	}
	public static void getStockCode() {
		File in = new File(db);
		if (!in.exists()) {
			if (codes.size() < 1)
				try {
					codes = GetStockCode.getAllStackCodes(db);
				} catch (IOException e) {
					e.printStackTrace();
				}
		} else {
			// 从本地获取
			if (codes.size() < 1)
				try {
					codes = GetStockCode.getAllStockCodesFromLocal(db);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static String[] getHeaders() {
		String[] header = { "股票名字", "今日开盘价", "昨日收盘价", "当前价格", "今日最高价", "今日最低价",
				"竟买价", "竞卖价", "成交的股票数", "成交金额(元)", "买一", "买一", "买二", "买二",
				"买三", "买三", "买四", "买四", "买五", "买五", "卖一", "卖一", "卖二", "卖二",
				"卖三", "卖三", "卖四", "卖四", "卖五", "卖五", "日期\t", "时间\t", "unknown1",
				"unknown2", "unknown3", "unknown4", "unknown5" };
		return header;
	}

	public static void getAllStockInfo() throws IOException {
		String[] header = getHeaders();
		System.out.println("总共有：" + codes.size() + "个股票");
		System.out.println(header.length);
		for (int i = 0; i < header.length; i++) {
			System.out.print(header[i] + "\t|");
		}
		System.out.println("\n");
		for (String code : codes) {
			if (code.contains("sh") || code.contains("sz")) {
				getStockInfoByCode(code);
			}
		}
	}

	/**
	 * 
	 * @param first
	 *            从0开始
	 * @param last
	 *            不包括 last
	 * @return
	 */
	public static List<List> getStockInfo(int first)
			throws Exception {
		List<List> stockInfo = new ArrayList<List>();
		first = first < 0 ? 0 : first;
		for (int i = first; i < codes.size(); i++) {
			stockInfo.add(getStockInfoByCode(codes.get(i)));
		}
		return stockInfo;
	}

	public static List getStockInfoByCode(String stockCode)
			throws IOException {
		// 仅仅打印
		List stockInfo =new ArrayList();
		stockInfo.add(stockCode.substring(2));
		stockInfo.add(stockCode.substring(0, 2));
		stockInfo.add(Util.GetNowDate());
		URL url = new URL("http://hq.sinajs.cn/?list=" + stockCode);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(15000);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		if (sb.length() > 0) {
			String rs = sb.toString();
			rs = rs.substring(rs.indexOf("\"") + 1, rs.lastIndexOf("\""));
			String[] rss = rs.split(",");
			for (int i = 0; i < rss.length; i++) {
				System.out.print(rss[i] + "\t|");
				if(i==1||i==3||i==4||i==5||i==8){
					stockInfo.add(rss[i]);
				}
			}
			System.out.println("\n------------------------------------");
		}
		return stockInfo;
	}

	
	/**
	 * 
	 * @param first
	 *            从0开始
	 * @param last
	 *            不包括 last
	 * @return
	 */
	public static void getAllHisStockInfo(int first)
			throws Exception {
		first = first < 0 ? 0 : first;
		for (int i = first; i < codes.size(); i++) {
			List<List> allHisStockInfoByCode = getAllHisStockInfoByCode(codes.get(i));
			batchUpdate.batchUpdate(hisBatchSql, allHisStockInfoByCode, 20);
		}
	}
	/*
	 * 
	 *  获取股票的所有历史数据 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static List<List> getAllHisStockInfoByCode(String stockCode) throws Exception {
		List<List> stockAllHisInfo = new ArrayList<List>();
		URL url = new URL(
				"http://www.xueqiu.com/stock/forchartk/stocklist.json?period=1day&symbol="
						+ stockCode + "&type=before&_=1407681428608");

		int n=0;
		StringBuffer sb = new StringBuffer();
		while(n==0){
			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(10000);
				connection
				.setRequestProperty(
						"Cookie",
						"xq_a_token=E706dkOMDtEOPuxWwq4tpR; xqat=E706dkOMDtEOPuxWwq4tpR; xq_r_token=PSOnXD494w4Ow98tx0igbX; xq_is_login=1; xq_token_expire=Mon%20Dec%2015%202014%2009%3A19%3A40%20GMT%2B0800%20(CST); bid=1c4065c952da5c5df28169838ab8e607_i2pflpr5; __utmt=1; __utma=1.1268723063.1416446479.1416446479.1416446504.2; __utmb=1.8.9.1416464670716; __utmc=1; __utmz=1.1416446479.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lvt_1db88642e346389874251b5a1eded6e3=1416446479,1416465800; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1416465800");
				connection.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				connection.disconnect();
				n=1;
			} catch (Exception e) {
				System.out.println("connect lose.....sleeping 10s");
				Thread.sleep(10000);
			}
		}
		JSONObject jb = new JSONObject(sb.toString());
		JSONArray object = jb.getJSONArray("chartlist");
		double lastEma12=0;double lastEma26=0;double lastDea=0;
		for (int i = 0; i < object.length(); i++) {
			List stockInfo = new ArrayList();
			stockInfo.add(stockCode.substring(2));
			stockInfo.add(stockCode.substring(0, 2));
			JSONObject object2 = (JSONObject) object.get(i);
			BigDecimal db = new BigDecimal(object2.get("volume").toString());
			String volume =   db.toPlainString().toString();
			String open = object2.get("open").toString();
			String high =   object2.get("high").toString();
			String close =   object2.get("close").toString();
			String low =   object2.get("low").toString();
			String chg =   object2.get("chg").toString();
			String percent =   object2.get("percent").toString();
			String turnrate =   object2.get("turnrate").toString();
			String ma5 =   object2.get("ma5").toString();
			String ma10 =   object2.get("ma10").toString();
			String ma20 =   object2.get("ma20").toString();
			String ma30 =   object2.get("ma30").toString();
			String dif =   object2.get("dif").toString();
			String dea =   object2.get("dea").toString();
			String macd =   object2.get("macd").toString();
			String time = Util.getDate8FromString((String) object2.get("time")).toString();
			stockInfo.add(volume);
			stockInfo.add(open);
			stockInfo.add(high);
			stockInfo.add(close);
			stockInfo.add(low);
			stockInfo.add(chg);
			stockInfo.add(percent);
			stockInfo.add(turnrate);
			stockInfo.add(ma5);
			stockInfo.add(ma10);
			stockInfo.add(ma20);
			stockInfo.add(ma30);
			if(i==0){
				stockInfo.add(close);
				stockInfo.add(close);
				stockInfo.add("0");
				stockInfo.add("0");
				stockInfo.add("0");
				lastEma12 =Double.parseDouble(close);
				lastEma26 = Double.parseDouble(close);
				lastDea = 0;
			}else{
				List result=Util.getMacd(lastEma12, lastEma26, lastDea,Double.parseDouble(object2.get("close").toString()));
				stockInfo.add(result.get(0));
				stockInfo.add(result.get(1));
				stockInfo.add(result.get(2));
				stockInfo.add(result.get(3));
				stockInfo.add(result.get(4));
				lastEma12 =Double.parseDouble(result.get(0).toString());
				lastEma26 =Double.parseDouble(result.get(1).toString());
				lastDea = Double.parseDouble(result.get(3).toString());
			}
			stockInfo.add(time);
			stockAllHisInfo.add(stockInfo);
			//System.out.println(volume+","+open+","+high+","+close+","+low+","+chg+","+percent+","+turnrate+","+ma5+","+ma10+","+ma20+","+ma30+","+dif+","+dea+","+macd+","+time);
		}
		System.out.println(stockCode+"::::::::::");
		return stockAllHisInfo;
	}
}
