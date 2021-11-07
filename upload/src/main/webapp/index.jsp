<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>文件上传测试</title>
</head>
<body>
<form action="/upload/UploadifyAvatarServlet" enctype="multipart/form-data"
		method="post">
		<input
			type="hidden" name="userId" value="10047" />
			<input
			type="hidden" name="version" value="1" />
		<table>
			<tr>
				<td>头像：<input type="file" name="image"></td>
				
			</tr>
			
			<tr>
				<td><input type="submit" value="头像上传" /></td>
			</tr>
		</table>
	</form>
	<form action="/upload/UploadServlet" enctype="multipart/form-data"
		method="post">
		<input type="hidden" name="uploadFlag" value="1" /> 
		<input type="hidden" name="userId" value="100123" />
		<input type="hidden" name="validTime" value="10" />
		<table>
			<tr>
				<td>文件1：<input type="file" name="file1"></td>
			</tr>
			<tr>
				<td>文件2： <input type="file" name="file2"></td>
			</tr>
			<tr>
				<td>文件3：<input type="file" name="file3"></td>
			</tr>
			<tr>
				<td>文件4： <input type="file" name="file4"></td>
			</tr>
			<tr>
				<td><input type="submit" value="照片上传" /></td>
			</tr>
		</table>
	</form>

	<form action="/upload/UploadServlet" enctype="multipart/form-data"
		method="post">
		<input type="hidden" name="uploadFlag" value="2" /><input
			type="hidden" name="userId" value="100123" />
			<input type="hidden" name="validTime" value="10" />
		<table>
			<tr>
				<td>文件1：<input type="file" name="file1"></td>
			</tr>
			<tr>
				<td>文件2： <input type="file" name="file2"></td>
			</tr>
			<tr>
				<td><input type="submit" value="商务圈图片上传" /></td>
			</tr>
		</table>
	</form>

	<form action="/upload/UploadServlet" enctype="multipart/form-data"
		method="post">
		<input type="hidden" name="uploadFlag" value="3" /><input
			type="hidden" name="userId" value="100123" />
			<input type="hidden" name="validTime" value="10" />
		<table>
			<tr>
				<td>文件1：<input type="file" name="file1"></td>
			</tr>
			<tr>
				<td>文件2： <input type="file" name="file2"></td>
			</tr>
			<tr>
				<td><input type="submit" value="图片上传" /></td>
			</tr>
		</table>
	</form>

	<form action="/upload/UploadServlet" enctype="multipart/form-data"
		method="post">
		<input type="hidden" name="uploadFlag" value="4" /><input
			type="hidden" name="userId" value="100123" />
			<input type="hidden" name="validTime" value="10" />
		<table>
			<tr>
				<td>文件1：<input type="file" name="file1"></td>
			</tr>
			<tr>
				<td>文件2： <input type="file" name="file2"></td>
			</tr>
			<tr>
				<td><input type="submit" value="视频上传" /></td>
			</tr>
		</table>
	</form>

	<form action="/upload/UploadServlet" enctype="multipart/form-data"
		method="post">
		<input type="hidden" name="uploadFlag" value="4" /><input
			type="hidden" name="userId" value="100123" />
			<input type="hidden" name="validTime" value="10" />
		<table>
			<tr>
				<td>文件1：<input type="file" name="file1"></td>
			</tr>
			<tr>
				<td>文件2： <input type="file" name="file2"></td>
			</tr>
			<tr>
				<td><input type="submit" value="其它上传" /></td>
			</tr>
		</table>
	</form>
	
	<form action="/upload/UploadMusicServlet" enctype="multipart/form-data"
		method="post">
		<input type="hidden" name="uploadFlag" value="4" /><input
			type="hidden" name="userId" value="100123" />
			<input type="hidden" name="validTime" value="10" />
		<table>
			<tr>
				<td>文件1：<input type="file" name="file1"></td>
			</tr>
			<tr>
				<td>文件2： <input type="file" name="file2"></td>
			</tr>
			<tr>
				<td><input type="submit" value="短视频音乐上传" /></td>
			</tr>
		</table>
	</form>

</body>
</html>