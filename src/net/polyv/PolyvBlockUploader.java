package net.polyv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.chinanetcenter.api.sliceUpload.JSONObjectRet;
import com.chinanetcenter.api.sliceUpload.PutExtra;
import com.chinanetcenter.api.util.Config;
import com.chinanetcenter.api.util.EncodeUtils;
import com.chinanetcenter.api.wsbox.SliceUploadResumable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PolyvBlockUploader {
	private static final String GETTOKEN_URL = "http://my.polyv.net/wsuploadtoken/client?param=";
	private String luping;
	private String filename;
	private String filepath;
	private String filesize;
	private String cataid;
	private String title;
	private String tag;
	private String vpid;
	private long ts;
	private String userid;
	private String writetoken;

	private String uploadToken;
	private SliceUploadResumable sliceUploadResumable;
	// 定义进度回调
	private JSONObjectRet jsonObjectRet;
	// 由于网络原因获取uploadToken失败时，自动再次请求的次数
	private int reGetUploadToken;
	// 获取uploadToken的状态
	private int status;
	// 获取uploadToken时，服务端返回的并非200
	private static final int RESPONSE_FAIL = 1;
	// 除了上面的情况，由于其他的情况获取uplaodToken失败
	private static final int GET_UPLOADTOKEN_FAIL = 2;
	// 获取uploadToken成功
	private static final int GET_UPLOADTOKEN_SUCCESS = 3;
	// 获取uploadToken的重连状态
	private static final int GET_UPLOADTOKEN_RECONN = 4;
	// 不再获取uploadToken的状态
	private static final int NO_GET_UPLOADTOKEN = 5;
	// 正在请求链接以获取uploadToken的状态
	private static final int GETTING_UPLOADTOKEN = 6;
	private HttpURLConnection conn;
	// 当在重新请求获取uploadToken时，按下暂停，不再请求的常量
	// private static final int OUTGET = -10000;
	// 获取uploadToken请求超时的时间
	private static final int TIMEOUT = 30000;
	// 获取uploadToken失败时再次请求的间隔
	private static final int SPACE = 5000;
	// 最大请求uploadToken数
	private static final int MAX_REUPLOAD_COUNT = 5;

	private String bucketName;
	private String fileKey;
	private PutExtra putExtra;
	// 信息文件的第二层父目录
	private String infofileParent = "polyv";
	// 信息文件的路径
	private String infofilepath;

	/**
	 * 
	 * @param luping
	 *            视频课件优化处理：0/1
	 * @param filepath
	 *            文件的路径
	 * @param cataid
	 *            分类id
	 * @param title
	 *            标题
	 * @param tag
	 *            标签，多个标签用逗号隔开
	 * @param vid
	 *            视频的id
	 * @param userid
	 *            用户的id
	 * @param writetoken
	 *            用户的writetoken
	 * @param jsonObjectRet
	 *            上传监听器
	 */
	public PolyvBlockUploader(String luping, String filepath, String cataid, String title, String tag, String vid,
			String userid, String writetoken, JSONObjectRet jsonObjectRet) {
		this.filepath = filepath;
		if (filepath != null) {
			File uploadfile = new File(filepath);
			this.filename = uploadfile.getName();
			this.filesize = uploadfile.length() + "";
		}
		this.luping = luping;
		this.cataid = cataid;
		this.title = title;
		this.tag = tag;
		if (vid != null)
			this.vpid = vid.substring(0, vid.indexOf("_"));
		this.userid = userid;
		this.jsonObjectRet = jsonObjectRet;
		this.sliceUploadResumable = new SliceUploadResumable();
		this.writetoken = writetoken;
	}

	/**
	 * 删除信息文件
	 */
	public boolean deleteInfoFile() {
		return new File(infofilepath).delete();
	}

	public String getFileKey() {
		return fileKey;
	}

	public String getBucketName() {
		return bucketName;
	}

	public String getInfofileParent() {
		return infofileParent;
	}

	public void setInfofileParent(String infofileParent) {
		this.infofileParent = infofileParent;
	}

	/**
	 * 获取之前记录的下载信息
	 * 
	 * @param bucketName
	 * @param FileName
	 * @return
	 */
	public PutExtra getPutExtra(String bucketName, String fileKey) {
		String key = fileKey.substring(0, fileKey.lastIndexOf("."));
		File configFile = new File(System.getProperty("user.dir") + File.separator + infofileParent + File.separator
				+ bucketName + File.separator + key + "_sliceConfig.properties");
		infofilepath = configFile.getAbsolutePath();
		if (!configFile.exists())
			return null;
		FileReader reader = null;
		int fileLen = (int) configFile.length();
		char[] chars = new char[fileLen];
		try {
			reader = new FileReader(configFile);
			reader.read(chars);
			String txt = String.valueOf(chars);
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode obj = objectMapper.readTree(txt);
			PutExtra putExtra = new PutExtra(obj);
			return putExtra;
		} catch (Exception e) {
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
		}
	}

	// 获取uploadToken,读取信息。@return 是否应该继续执行
	private boolean readData(HttpURLConnection conn, String url) {
		if (this.conn == null)
			return true;
		StringBuilder sb = new StringBuilder();
		int len = 0;
		byte[] buf = new byte[1024];
		try {
			while ((len = conn.getInputStream().read(buf)) != -1) {
				sb.append(new String(buf, 0, len));
			}
		} catch (IOException e) {
			if (!getUploadToken(url))
				return false;
			readData(this.conn, url);
		}
		if (sb.toString().equals(""))
			return false;
		try {
			JSONObject datajson = new JSONObject(sb.toString()).getJSONObject("data");
			uploadToken = (String) datajson.get("uploadToken");
			bucketName = (String) datajson.get("bucketName");
			fileKey = (String) datajson.get("fileKey");

			Config.PUT_URL = ((String) datajson.get("putUrl")).replace("https", "http");
			Config.MGR_URL = (String) datajson.get("mgrUrl");
			Config.VERSION_NO = "polyv-java-sdk-1.6.3";

			putExtra = getPutExtra(bucketName, fileKey);
			if (putExtra == null) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("bucketName", bucketName);
				params.put("fileKey", fileKey);
				params.put("filePath", filepath);
				putExtra = new PutExtra();
				putExtra.params = params;
			}

			status = GET_UPLOADTOKEN_SUCCESS;
		} catch (JSONException e) {
			System.out.println(filename + "获取uploadtoken时解析json发生异常：" + e + " json数据为：" + sb.toString());
			status = GET_UPLOADTOKEN_FAIL;
		}
		return true;
	}

	// 获取uploadToken,获取响应。@return 是否应该继续执行
	private boolean getUploadToken(String url) {
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(TIMEOUT);
			conn.setConnectTimeout(TIMEOUT);
			if (conn.getResponseCode() != 200) {
				conn = null;
				status = RESPONSE_FAIL;
				return true;
			}
			status = GETTING_UPLOADTOKEN;
		} catch (IOException e) {
			status = GET_UPLOADTOKEN_RECONN;
			try {
				Thread.sleep(SPACE);
			} catch (InterruptedException e1) {
			}
			reGetUploadToken++;
			if (reGetUploadToken > 0)
				System.out.println(filename + "第" + reGetUploadToken + "次请求获取uploadtoken时发生了异常：" + e);
			if (reGetUploadToken < 0) {
				status = NO_GET_UPLOADTOKEN;
				reGetUploadToken = 0;
				if (jsonObjectRet != null) {
					jsonObjectRet.onFailure(new Exception("取消获取uplaodToken"));
				}
				return false;
			}
			if (reGetUploadToken > MAX_REUPLOAD_COUNT) {
				reGetUploadToken = 0;
				conn = null;
				status = GET_UPLOADTOKEN_FAIL;
				return true;
			}
			getUploadToken(url);
		}
		if (status == NO_GET_UPLOADTOKEN)
			return false;
		else
			return true;
	}

	/**
	 * 分块上传
	 * 
	 * @return 是否可以用分块上传，根据获取uploadtoken时服务端的响应状态码决定
	 */
	public boolean upload() {
		ts = System.currentTimeMillis();
		try {
			title = URLEncoder.encode(title, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore
		}

		try {
			tag = URLEncoder.encode(tag, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore
		}

		try {
			filename = URLEncoder.encode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore
		}

		// 签名原始串
		StringBuilder sb = new StringBuilder();
		sb.append("userid=").append(userid);

		if (cataid != null) {
			sb.append("&cataid=").append(cataid);
		}
		if (null != title) {
			sb.append("&title=").append(title);
		}
		if (null != tag) {
			sb.append("&tag=").append(tag);
		}
		if (null != luping) {
			sb.append("&luping=").append(luping);
		}
		if (null != filename) {
			sb.append("&filename=").append(filename);
		}
		if (null != filesize) {
			sb.append("&filesize=").append(filesize);
		}
		sb.append("&ts=").append(ts);
		sb.append("&writetoken=").append(writetoken);
		sb.append("&vpid=").append(vpid);
		String sign = new String(Hex.encodeHex(DigestUtils.md5(sb.toString()))); // 做md5
		// 参数拼接，注意：参数禁止携带secretKey
		StringBuilder paramSb = new StringBuilder();
		paramSb.append("userid=").append(userid);
		if (cataid != null) {
			paramSb.append("&cataid=").append(cataid);
		}
		if (null != title) {
			paramSb.append("&title=").append(title);
		}
		if (null != tag) {
			paramSb.append("&tag=").append(tag);
		}
		if (null != luping) {
			paramSb.append("&luping=").append(luping);
		}
		if (null != filename) {
			paramSb.append("&filename=").append(filename);
		}
		if (null != filesize) {
			paramSb.append("&filesize=").append(filesize);
		}
		paramSb.append("&ts=").append(ts);
		paramSb.append("&sign=").append(sign);
		paramSb.append("&vpid=").append(vpid);

		// 对拼接出来的参数做url安全的base64编码
		String paramVal = EncodeUtils.urlsafeEncode(paramSb.toString());
		// 请求接口，支持http get和post
		String url = GETTOKEN_URL + paramVal;

		if (!getUploadToken(url)) {
			return true;
		}
		if (!readData(conn, url)) {
			return true;
		}
		switch (status) {
		case RESPONSE_FAIL:
			return false;
		case GET_UPLOADTOKEN_FAIL:
			if (jsonObjectRet != null) {
				jsonObjectRet.onFailure(new Exception("获取uploadToken失败"));
			}
			return true;
		case GET_UPLOADTOKEN_SUCCESS:
			sliceUploadResumable.execUpload(bucketName, fileKey, filepath, putExtra, jsonObjectRet, uploadToken);
			return true;
		}
		return true;
	}
}
