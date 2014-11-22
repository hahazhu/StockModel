import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mt.common.Util;
import com.mt.datapre.DataPrepare;
import com.mt.sql.MysqlUtil;

public class test {
	public static void main(String[] args) throws Exception {
		//System.out.println(getAllHisStockInfoByCode("sh600000"));
		MysqlUtil jdbcUtils = new MysqlUtil();  
        jdbcUtils.getConnection();  
		 String call = "upDayStockInfo"; 
	        List list = new ArrayList();
	        list.add("300393");
	        list.add("20141119");
	   boolean res = jdbcUtils.callProc(call, list);
	   System.out.println(res);
	}

	public static List getAllHisStockInfoByCode(String stockCode) throws Exception {
		// 仅仅打印
		List stockInfo = new ArrayList();
		stockInfo.add(stockCode.substring(2));
		stockInfo.add(stockCode.substring(0, 2));
		URL url = new URL(
				"http://www.xueqiu.com/stock/forchartk/stocklist.json?period=1day&symbol="
						+ stockCode + "&type=before&_=1407681428608");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(16000);
		connection
				.setRequestProperty(
						"Cookie",
						"xq_a_token=E706dkOMDtEOPuxWwq4tpR; xqat=E706dkOMDtEOPuxWwq4tpR; xq_r_token=PSOnXD494w4Ow98tx0igbX; xq_is_login=1; xq_token_expire=Mon%20Dec%2015%202014%2009%3A19%3A40%20GMT%2B0800%20(CST); bid=1c4065c952da5c5df28169838ab8e607_i2pflpr5; __utmt=1; __utma=1.1268723063.1416446479.1416446479.1416446504.2; __utmb=1.8.9.1416464670716; __utmc=1; __utmz=1.1416446479.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lvt_1db88642e346389874251b5a1eded6e3=1416446479,1416465800; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1416465800");

		connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		JSONObject jb = new JSONObject(sb.toString());
		JSONArray object = jb.getJSONArray("chartlist");
		for (int i = 0; i < object.length(); i++) {
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
			String time = getDate8FromString((String) object2.get("time")).toString();
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
			stockInfo.add(dif);
			stockInfo.add(dea);
			stockInfo.add(macd);
			stockInfo.add(time);
			//System.out.println(volume+","+open+","+high+","+close+","+low+","+chg+","+percent+","+turnrate+","+ma5+","+ma10+","+ma20+","+ma30+","+dif+","+dea+","+macd+","+time);
		}
		//System.out.println(sb);
		return stockInfo;
	}

	public static String getDate8FromString(String in){
		Map monthTrans= new HashMap(){{
			put("Jan","01");
			put("Feb","02");
			put("Mar","03");
			put("Apr","04");
			put("May","05");
			put("Jun","06");
			put("Jul","07");
			put("Aug","08");
			put("Sep","09");
			put("Oct","10");
			put("Nov","11");
			put("Dec","12");
		}};
		String[] arr=in.split(" ");
		String year = arr[arr.length-1];
		String month = (String) monthTrans.get(arr[1]);
		String day = arr[2];
		return year+month+day;
	}

	// 压缩
	public static String compress(String str) throws IOException {
		if (str == null || str.length() == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		return out.toString("ISO-8859-1");
	}

	public static String uncompress(String str) throws IOException {
		if (str == null || str.length() == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("gzip"));
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		// toString()使用平台默认编码，也可以显式的指定如toString(&quot;GBK&quot;)
		return out.toString();
	}
}
