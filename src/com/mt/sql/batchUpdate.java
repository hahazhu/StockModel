package com.mt.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class batchUpdate {
	static {
		String db = "com.mysql.jdbc.Driver";
		try {
			Class.forName(db);
		} catch (Exception e) {
			System.out.println("��������ʧ��:" + db);
		}
	}
	public static void batchUpdate(String sql, List content ,int paraNum) {
		String host = "jdbc:mysql://127.0.0.1:3306/gao_stock";
		String user = "root";
		String passwd = "";
		Connection con = null;
		try {
			con = DriverManager.getConnection(host, user, passwd);
			con.setAutoCommit(false);// �ر������Զ��ύ
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			for (Iterator it=content.iterator();it.hasNext();) {
				List row = (List) it.next();
				if(row.size()!=paraNum){
					System.out.println("����������");
				}else{
					for(int j=1;j<=paraNum;j++){
						pstmt.setString(j, (String)row.get(j-1).toString());
					}
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
			con.commit();// ���ִ����ϣ��ύ������
			con.close();
		} catch (Exception e) {
			try {
				con.rollback();
				con.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println(e);
		}
		
	}
}
