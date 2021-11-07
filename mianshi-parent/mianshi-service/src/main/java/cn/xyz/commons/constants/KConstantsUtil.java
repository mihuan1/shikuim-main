package cn.xyz.commons.constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 属性表
 * 
 * @author luorc
 *
 */
public final class KConstantsUtil {

	private static final class SQL {
		public static final String all = "SELECT id, name FROM tb_constants";
		public static final String city = "SELECT id, name FROM tb_areas WHERE type = 3";
		public static final String degree = "SELECT id, name FROM tb_constants WHERE parent_id = 1";
		public static final String dutyTime = "SELECT id, name FROM tb_constants WHERE parent_id = 52";
		public static final String fn = "SELECT id, name FROM tb_constants WHERE parent_id IN(SELECT id FROM tb_constants WHERE parent_id IN (SELECT id FROM tb_constants WHERE parent_id = 64))";
		public static final String industry = "SELECT id, name FROM tb_constants WHERE parent_id IN (SELECT id FROM tb_constants WHERE parent_id = 63)";
		public static final String major = "SELECT id, name FROM tb_constants WHERE parent_id IN (SELECT id FROM tb_constants WHERE parent_id IN (SELECT id FROM tb_constants WHERE parent_id = 1005))";
		public static final String salary = "SELECT id, name FROM tb_constants WHERE parent_id = 3";
		public static final String scale = "SELECT id, name FROM tb_constants WHERE parent_id = 44";
		public static final String workType = "SELECT id, name FROM tb_constants WHERE parent_id = 59";

	}

	public static final Map<Integer, String> MAP_ALL;
	public static final Map<String, Integer> MAP_CITY;
	public static final Map<String, Integer> MAP_DEGREE;
	public static final Map<String, Integer> MAP_DUTY_TIME;
	public static final Map<String, Integer> MAP_FN;
	public static final Map<String, Integer> MAP_INDUSTRY;
	public static final Map<String, Integer> MAP_MAJOR;
	public static final Map<String, Integer> MAP_SALARY;
	public static final Map<String, Integer> MAP_SCALE;
	public static final Map<String, Integer> MAP_WORK_EXP;
	public static final Map<String, Integer> MAP_WORK_TYPE;

	private static final String password = "123456";
	private static final String url = "jdbc:mysql://localhost:3808/imapi?autoReconnect=true&useUnicode=true&characterEncoding=utf8";
	private static final String user = "root";

	static {
		MAP_CITY = Maps.newHashMap();
		MAP_DEGREE = Maps.newHashMap();
		MAP_FN = Maps.newHashMap();
		MAP_INDUSTRY = Maps.newHashMap();
		MAP_MAJOR = Maps.newHashMap();
		MAP_SCALE = Maps.newHashMap();
		MAP_WORK_EXP = Maps.newHashMap();
		MAP_DUTY_TIME = Maps.newHashMap();
		MAP_WORK_TYPE = Maps.newHashMap();
		MAP_SALARY = Maps.newHashMap();
		MAP_ALL = Maps.newHashMap();
		init();
	}

	public static int getCityIdByName(String cityText) {
		Integer cityId = KConstantsUtil.MAP_CITY.get(cityText);
		if (null == cityId)
			cityId = KConstantsUtil.MAP_CITY.get(cityText + "市");
		if (null == cityId)
			cityId = 0;
		return cityId;
	}

	private static void init() {
		System.out.println("KConstantsUtil =====> init ...");
		if (MAP_CITY.isEmpty())
			initWithSql(MAP_CITY, SQL.city);
		if (MAP_DEGREE.isEmpty())
			initWithSql(MAP_DEGREE, SQL.degree);
		if (MAP_FN.isEmpty())
			initWithSql(MAP_FN, SQL.fn);
		if (MAP_INDUSTRY.isEmpty())
			initWithSql(MAP_INDUSTRY, SQL.industry);
		if (MAP_MAJOR.isEmpty())
			initWithSql(MAP_MAJOR, SQL.major);
		if (MAP_SCALE.isEmpty())
			initWithSql(MAP_SCALE, SQL.scale);
		if (MAP_DUTY_TIME.isEmpty())
			initWithSql(MAP_DUTY_TIME, SQL.dutyTime);
		if (MAP_WORK_TYPE.isEmpty())
			initWithSql(MAP_WORK_TYPE, SQL.workType);
		if (MAP_SALARY.isEmpty())
			initWithSql(MAP_SALARY, SQL.salary);
		if (MAP_WORK_EXP.isEmpty()) {
			MAP_WORK_EXP.put("在读学生", 11);
			MAP_WORK_EXP.put("应届毕业生", 12);
			MAP_WORK_EXP.put("1年工作经验", 13);
			MAP_WORK_EXP.put("2年工作经验", 14);
			MAP_WORK_EXP.put("3-4年工作经验", 15);
			MAP_WORK_EXP.put("5-7年工作经验", 16);
			MAP_WORK_EXP.put("8-9年工作经验", 17);
			MAP_WORK_EXP.put("10年以上工作经验", 18);
		}
		if (MAP_ALL.isEmpty())
			initWithSql2(MAP_ALL, SQL.all);
	}

	private static void initWithSql(Map<String, Integer> maps, String sql) {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, password);
			pstmt = con.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next()) {
				maps.put(result.getString(2), result.getInt(1));
			}
		} catch (Exception e) {

		} finally {
			try {
				if (null != result)
					result.close();
				if (null != pstmt)
					pstmt.close();
				if (null != con)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	private static void initWithSql2(Map<Integer, String> maps, String sql) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, password);
			pstmt = con.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next()) {
				maps.put(result.getInt(1), result.getString(2));
			}
		} catch (Exception e) {

		} finally {
			try {
				if (null != result)
					result.close();
				if (null != pstmt)
					pstmt.close();
				if (null != con)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String... args) {

	}
}
