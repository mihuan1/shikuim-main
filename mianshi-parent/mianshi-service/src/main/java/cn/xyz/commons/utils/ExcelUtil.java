package cn.xyz.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


/** 
* @author: hsg 
* @date: 2018年7月26日 下午8:57:05 
* @version: 1.0  
* @Description:    Excel 表格工具类
*/
public class ExcelUtil{
		
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtil.class.getName());
	
	
	/**
     *写Excel文件
     * @param response  : 响应对象
     * @param fileName  : 下载文件名称
     * @param title     : 标题
     * @param dataList  : 内容
     * @return
     * @throws Exception
     */
    public static boolean writeExcel(HttpServletResponse response,String fileName,String[] title,List<String[]> dataList) throws Exception {
        boolean flag = false;
       
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFCellStyle hssfTitleCellStyle = getHSSFTitleStyle(wb);
            
            int pageSize = 65535;
            int listSize = dataList.size();
            int sheetSize = 0;
            if(listSize % pageSize == 0){
                sheetSize = listSize/pageSize;
            }else{
                sheetSize = (listSize/pageSize)+1;
            }

            if(sheetSize == 0){
                HSSFSheet sheet = wb.createSheet();
                HSSFRow row = sheet.createRow(0);
                row.setHeight((short) 600);//目的是想把行高设置成25px
                row.setRowStyle(hssfTitleCellStyle);
                sheet.setDefaultColumnWidth(20);  
                sheet.setDefaultRowHeightInPoints(20);  
                //title
                for (int i = 0;i < title.length;i++) {
                    HSSFCell cell = row.createCell(i);
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    cell.setCellStyle(hssfTitleCellStyle);
                    cell.setCellValue(title[i]);
                }
            }else{
                int start = 0;
                int end = 0;
                for(int s=0;s<sheetSize;s++){
                    start = s * pageSize;
                    if(s == (sheetSize-1))
                        end = listSize;
                    else
                        end = (s+1) * pageSize;
                    HSSFSheet sheet = wb.createSheet();
                    HSSFRow row = sheet.createRow(0);
                    row.setHeight((short) 600);//目的是想把行高设置成25px
                    row.setRowStyle(hssfTitleCellStyle);
                    sheet.setDefaultColumnWidth(20);  
                    sheet.setDefaultRowHeightInPoints(20);  
                    //title
                    for (int i = 0;i < title.length;i++) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                        cell.setCellStyle(hssfTitleCellStyle);
                        cell.setCellValue(title[i]);
                    }
                    //context
                    HSSFCellStyle hssfCellStyle = getHSSFCellStyle(wb);
                    int row_index = 1;
                    for(int j = start;j < end;j++){
                        HSSFRow data_row = sheet.createRow(row_index);
                        row_index ++;
                        String[] cellList = dataList.get(j);
                        int len = cellList.length;
                        for (int k=0;k<len;k++) {
                            HSSFCell cell = data_row.createCell(k);
                            String value = cellList[k];
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            cell.setCellStyle(hssfCellStyle);
                            cell.setCellValue(value);
                        }
                    }
                }
            }
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename="+ new String(fileName.getBytes(), "iso8859-1"));
            ServletOutputStream out = response.getOutputStream();
            wb.write(out);
            // 弹出下载对话框
            out.close();
            flag = true;
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }
    
    
	private static HSSFCellStyle getHSSFCellStyle(HSSFWorkbook wb) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 */
	private static HSSFCellStyle getHSSFTitleStyle(HSSFWorkbook wb) {
		// 标题样式
		HSSFCellStyle titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(CellStyle.ALIGN_CENTER); // 水平对齐
        titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //垂直对齐
        titleStyle.setLocked(true); // 样式锁定
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 16);
       
        titleFont.setFontName("微软雅黑");
        titleStyle.setFont(titleFont);
        
        return titleStyle;
	}
	
	
	
	
	
	/**
	 * 用于测试
	 * @param args
	 */
	
	/*public static void main(String[] args) {
	        String path = "E://demo.xlsx";
	        String name = "test";
	        List<String> titles =Lists.newArrayList();
	        titles.add("id");
	        titles.add("name");
	        titles.add("age");
	        titles.add("birthday");
	        titles.add("gender");
	        titles.add("date");
	        List<Map<String, Object>> values = Lists.newArrayList();
	        for (int i = 0; i < 10; i++) {
	            Map<String, Object> map = Maps.newHashMap();
	            map.put("id", i + 1D);
	            map.put("name", "test_" + i);
	            map.put("age", i * 1.5);
	            map.put("gender", "man");
	            map.put("birthday", new Date());
	            map.put("date",  Calendar.getInstance());
	            values.add(map);
	        }
	        System.out.println(writerExcel(path, name, titles, values));
	    }*/

	    
	   /**
	     * 数据写入Excel文件
	     *
	     * @param path 文件路径，包含文件全名，例如：D://file//demo.xls
	     * @param name sheet名称
	     * @param titles 行标题列
	     * @param values 数据集合，key为标题，value为数据
	     * @return True\False
	     */
	
	    /*public static boolean writerExcel(String path, String name, List<String> titles, List<Map<String, Object>> values) {
	        LOGGER.info("path : {}", path);
	        String style = path.substring(path.lastIndexOf("."), path.length()).toUpperCase(); // 从文件路径中获取文件的类型
	        return generateWorkbook(path, name, style, titles, values);
	    }*/

	    /**
	     * 将数据写入指定path下的Excel文件中
	     *
	     * @param name   sheet名
	     * @param style  Excel类型   xls、 xlsx
	     * @param titles 标题串
	     * @param values 内容集
	     * @return  Workbook
	     */
	    public static Workbook generateWorkbook(String name, String style, List<String> titles, List<Map<String, Object>> values) {
	        
	    	LOGGER.info("file style : {}", style);
	    	
	        Workbook workbook;
	        if ("XLS".equals(style.toUpperCase())) {
	            workbook = new HSSFWorkbook();
	        } else { //xlsx
	            workbook = new XSSFWorkbook();
	        }
	        // 生成一个表格
	        Sheet sheet;
	        
	        if (null == name || "".equals(name)) {
	            sheet = workbook.createSheet(); // name 为空则使用默认值
	        } else {
	            sheet = workbook.createSheet(name);
	        }
	        //设置表格默认列宽度为15个字节
	        sheet.setDefaultColumnWidth((short) 15);
	        // 生成样式
	        Map<String, CellStyle> styles = createStyles(workbook);
	        /*
	         * 创建标题行
	         */
	        Row row = sheet.createRow(0);
	        // 存储标题在Excel文件中的序号
	        Map<String, Integer> titleOrder = Maps.newHashMap();
	        
	        for (int i = 0; i < titles.size(); i++) {
	            Cell cell = row.createCell(i);
	            cell.setCellStyle(styles.get("header"));
	            String title = titles.get(i);
	            cell.setCellValue(title);
	            titleOrder.put(title, i);
	        }
	        
	        /*
	         * 写入正文
	         */
	        Iterator<Map<String, Object>> iterator = values.iterator();
	        int index = 0; // 行号
	        while (iterator.hasNext()) {
	            index++; // 出去标题行，从第一行开始写
	            row = sheet.createRow(index);
	            Map<String, Object> value = iterator.next();
	            for (Map.Entry<String, Object> map : value.entrySet()) {
	                //获取列名
	                String title = map.getKey();
	                //根据列名获取序号
	                int i = titleOrder.get(title);
	                //在指定序号处创建cell
	                Cell cell = row.createCell(i);
	                //设置cell的样式
	                if (index % 2 == 1) {
	                    cell.setCellStyle(styles.get("cellA"));
	                } else {
	                    cell.setCellStyle(styles.get("cellA"));
	                }
	                //获取列的值
	                Object object = map.getValue();
	                // 判断object的类型
	                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                if (object instanceof Double) {
	                    cell.setCellValue((Double) object);
	                } else if (object instanceof Date) {
	                    String time = simpleDateFormat.format((Date) object);
	                    cell.setCellValue(time);
	                } else if (object instanceof Calendar) {
	                    Calendar calendar = (Calendar) object;
	                    String time = simpleDateFormat.format(calendar.getTime());
	                    cell.setCellValue(time);
	                } else if (object instanceof Boolean) {
	                    cell.setCellValue((Boolean) object);
	                } else {
	                	if(null != object)
	                		cell.setCellValue(object.toString());
	                }
	            }
	        }
	        
	        /*
	         * 写入到文件中
	         */
	        /*boolean isCorrect = false;
	        try {
	            File file = new File(path);
	            OutputStream outputStream = new FileOutputStream(file);
	            workbook.write(outputStream);
	            outputStream.close();
	            isCorrect = true;
	        } catch (IOException e) {
	            isCorrect = false;
	            LOGGER.error("write Excel file error : {}", e.getMessage());
	        }*/
	        
	        return workbook;
	    }

	   
	    /**
	     * 创建表格样式
	     * @param wb
	     * @return
	     */
	    private static Map<String, CellStyle> createStyles(Workbook wb) {
	        Map<String, CellStyle> styles = Maps.newHashMap();

	        // 标题样式
	        CellStyle titleStyle = wb.createCellStyle();
	        titleStyle.setAlignment(CellStyle.ALIGN_CENTER); // 水平对齐
	        titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); //垂直对齐
	        titleStyle.setLocked(true); // 样式锁定
	        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
	        Font titleFont = wb.createFont();
	        titleFont.setFontHeightInPoints((short) 16);
	       
	        titleFont.setFontName("微软雅黑");
	        titleStyle.setFont(titleFont);
	        styles.put("title", titleStyle);

	        // 文件头样式
	        CellStyle headerStyle = wb.createCellStyle();
	        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
	        headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex()); // 前景色
	        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND); // 颜色填充方式
	        headerStyle.setWrapText(true);
	        headerStyle.setBorderRight(CellStyle.BORDER_THIN); // 设置边界
	        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
	        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        headerStyle.setBorderTop(CellStyle.BORDER_THIN);
	        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
	        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        Font headerFont = wb.createFont();
	        headerFont.setFontHeightInPoints((short) 12);
	        headerFont.setColor(IndexedColors.WHITE.getIndex());
	        titleFont.setFontName("微软雅黑");
	        headerStyle.setFont(headerFont);
	        styles.put("header", headerStyle);

	        Font cellStyleFont = wb.createFont();
	        cellStyleFont.setFontHeightInPoints((short) 12);
	        cellStyleFont.setColor(IndexedColors.BLUE_GREY.getIndex());
	        cellStyleFont.setFontName("微软雅黑");
	        
	        // 正文样式A
	        CellStyle cellStyleA = wb.createCellStyle();
	        cellStyleA.setAlignment(CellStyle.ALIGN_CENTER); // 居中设置
	        cellStyleA.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        cellStyleA.setWrapText(true);
	        cellStyleA.setBorderRight(CellStyle.BORDER_THIN);
	        cellStyleA.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleA.setBorderLeft(CellStyle.BORDER_THIN);
	        cellStyleA.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleA.setBorderTop(CellStyle.BORDER_THIN);
	        cellStyleA.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleA.setBorderBottom(CellStyle.BORDER_THIN);
	        cellStyleA.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleA.setFont(cellStyleFont);
	        styles.put("cellA", cellStyleA);

	        // 正文样式B:添加前景色为浅黄色
	        CellStyle cellStyleB = wb.createCellStyle();
	        cellStyleB.setAlignment(CellStyle.ALIGN_CENTER);
	        cellStyleB.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	        cellStyleB.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
	        cellStyleB.setFillPattern(CellStyle.SOLID_FOREGROUND);
	        cellStyleB.setWrapText(true);
	        cellStyleB.setBorderRight(CellStyle.BORDER_THIN);
	        cellStyleB.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleB.setBorderLeft(CellStyle.BORDER_THIN);
	        cellStyleB.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleB.setBorderTop(CellStyle.BORDER_THIN);
	        cellStyleB.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleB.setBorderBottom(CellStyle.BORDER_THIN);
	        cellStyleB.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        cellStyleB.setFont(cellStyleFont);
	        styles.put("cellB", cellStyleB);

	        return styles;
	    }
	
	
   
}