 /********************
  * 
  * 
  * 
  * ʾ��
  * 
  * 
  * 
  * 
        String sql2 = "select * from test_stock_day "; 
        List<Map<String, Object>> list = jdbcUtils.findModeResult(sql2, null); 
        System.out.println(list);
        System.out.println("\n"+list.size());
        *******************/
        /*******************��*********************/  
        /*      String sql = "insert into userinfo (username, pswd) values (?, ?), (?, ?), (?, ?)"; 
        List<Object> params = new ArrayList<Object>(); 
        params.add("С��"); 
        params.add("123xiaoming"); 
        params.add("����"); 
        params.add("zhangsan"); 
        params.add("����"); 
        params.add("lisi000"); 
        try { 
            boolean flag = jdbcUtils.updateByPreparedStatement(sql, params); 
            System.out.println(flag); 
        } catch (SQLException e) { 
            // TODO Auto-generated catch block 
            e.printStackTrace(); 
        }*/  
  
  
        /*******************ɾ*********************/  
        //ɾ������Ϊ�����ļ�¼  
        /*      String sql = "delete from userinfo where username = ?"; 
        List<Object> params = new ArrayList<Object>(); 
        params.add("С��"); 
        boolean flag = jdbcUtils.updateByPreparedStatement(sql, params);*/  
  
        /*******************��*********************/  
        //������Ϊ���ĵ��������  
        /*      String sql = "update userinfo set pswd = ? where username = ? "; 
        List<Object> params = new ArrayList<Object>(); 
        params.add("lisi88888"); 
        params.add("����"); 
        boolean flag = jdbcUtils.updateByPreparedStatement(sql, params); 
        System.out.println(flag);*/  
  
        /*******************��*********************/  
        //�����÷����ѯ�����¼  
        /*      String sql2 = "select * from userinfo "; 
        List<Map<String, Object>> list = jdbcUtils.findModeResult(sql2, null); 
        System.out.println(list);*/  

package com.mt.sql;

import java.lang.reflect.Field;  
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.ResultSet;  
import java.sql.ResultSetMetaData;  
import java.sql.SQLException;  
import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map;  
  
  
  
public class MysqlUtil {  
    //���ݿ��û���  
    private static final String USERNAME = "root";  
    //���ݿ�����  
    private static final String PASSWORD = "";  
    //������Ϣ   
    private static final String DRIVER = "com.mysql.jdbc.Driver";  
    //���ݿ��ַ  
    private static final String URL = "jdbc:mysql://localhost:3306/gao_stock";  
    private Connection connection;  
    private PreparedStatement pstmt;  
    private ResultSet resultSet;  
    static{
    	try{  
            Class.forName(DRIVER);  
            //System.out.println("���ݿ����ӳɹ���");  
  
        }catch(Exception e){  
  
        } 
    }
 
      
    public static void main(String[] args) throws SQLException {
        MysqlUtil jdbcUtils = new MysqlUtil();  
        jdbcUtils.getConnection();  
       
	}
    /** 
     * ������ݿ������ 
     * @return 
     */  
    public Connection getConnection(){  
        try {  
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);  
        } catch (SQLException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return connection;  
    }  
  
      
    /** 
     * ���ӡ�ɾ������ 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public boolean updateByPreparedStatement(String sql, List<Object>params)throws SQLException{  
        boolean flag = false;  
        int result = -1;  
        pstmt = connection.prepareStatement(sql);  
        int index = 1;  
        if(params != null && !params.isEmpty()){  
            for(int i=0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        result = pstmt.executeUpdate();  
        flag = result > 0 ? true : false;  
        return flag;  
    }  
  
    /** 
     * ����proc
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public boolean callProc(String procedureName,List<Object> params)throws SQLException{  
        boolean flag = false;  
        int result = -1;  
        String callParam="";
        for(int i=0;i<params.size();i++){
        	if(i==0){
        		callParam+=params.get(i).toString();
        	}else{
        		callParam=callParam+","+params.get(i).toString();
        	}
        }
        String callStr="call "+procedureName+"("+callParam+")";
        pstmt = connection.prepareCall(callStr);  
        result = pstmt.executeUpdate();  
        flag = result == 0 ? true : false;  
        return flag;  
    }  
    
    /** 
     * ��ѯ������¼ 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public Map<String, Object> findSimpleResult(String sql, List<Object> params) throws SQLException{  
        Map<String, Object> map = new HashMap<String, Object>();  
        int index  = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i=0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();//���ز�ѯ���  
        ResultSetMetaData metaData = resultSet.getMetaData();  
        int col_len = metaData.getColumnCount();  
        while(resultSet.next()){  
            for(int i=0; i<col_len; i++ ){  
                String cols_name = metaData.getColumnName(i+1);  
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                map.put(cols_name, cols_value);  
            }  
        }  
        return map;  
    }  
  
    /**��ѯ������¼ 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public List<Map<String, Object>> findModeResult(String sql, List<Object> params) throws SQLException{  
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
        int index = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();  
        ResultSetMetaData metaData = resultSet.getMetaData();  
        int cols_len = metaData.getColumnCount();  
        while(resultSet.next()){  
            Map<String, Object> map = new HashMap<String, Object>();  
            for(int i=0; i<cols_len; i++){  
                String cols_name = metaData.getColumnName(i+1);  
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                map.put(cols_name, cols_value);  
            }  
            list.add(map);  
        }  
  
        return list;  
    }  
  
    /**ͨ��������Ʋ�ѯ������¼ 
     * @param sql 
     * @param params 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    public <T> T findSimpleRefResult(String sql, List<Object> params,  
            Class<T> cls )throws Exception{  
        T resultObject = null;  
        int index = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();  
        ResultSetMetaData metaData  = resultSet.getMetaData();  
        int cols_len = metaData.getColumnCount();  
        while(resultSet.next()){  
            //ͨ��������ƴ���һ��ʵ��  
            resultObject = cls.newInstance();  
            for(int i = 0; i<cols_len; i++){  
                String cols_name = metaData.getColumnName(i+1);  
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                Field field = cls.getDeclaredField(cols_name);  
                field.setAccessible(true); //��javabean�ķ���Ȩ��  
                field.set(resultObject, cols_value);  
            }  
        }  
        return resultObject;  
  
    }  
  
    /**ͨ��������Ʋ�ѯ������¼ 
     * @param sql  
     * @param params 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    public <T> List<T> findMoreRefResult(String sql, List<Object> params,  
            Class<T> cls )throws Exception {  
        List<T> list = new ArrayList<T>();  
        int index = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();  
        ResultSetMetaData metaData  = resultSet.getMetaData();  
        int cols_len = metaData.getColumnCount();  
        while(resultSet.next()){  
            //ͨ��������ƴ���һ��ʵ��  
            T resultObject = cls.newInstance();  
            for(int i = 0; i<cols_len; i++){  
                String cols_name = metaData.getColumnName(i+1);  
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                Field field = cls.getDeclaredField(cols_name);  
                field.setAccessible(true); //��javabean�ķ���Ȩ��  
                field.set(resultObject, cols_value);  
            }  
            list.add(resultObject);  
        }  
        return list;  
    }  
  
    /** 
     * �ͷ����ݿ����� 
     */  
    public void releaseConn(){  
        if(resultSet != null){  
            try{  
                resultSet.close();  
            }catch(SQLException e){  
                e.printStackTrace();  
            }  
        }  
    }
}
