package com.polyv.sdk.test;

import java.util.List;

import net.polyv.Progress;

import com.polyv.sdk.PolyvSDKClient;
import com.polyv.sdk.Video;

public class Tester {
	public static void main(String[] args) {
        PolyvSDKClient client = PolyvSDKClient.getInstance();
        client.setReadtoken("nsJ7ZgQMN0-QsVkscukWt-qLfodxoDFm");
        client.setWritetoken("Y07Q4yopIVXN83n-MPoIlirBKmrMPJu0");
        client.setSecretkey("DFZhoOnkQf");
        client.setUserid("sl8da4jjbx");
		
        //testResumableUpload();
		// TODO Auto-generated method stub
		//testUpload();
		//testResumableUpload();
        testDeleteVideo();
	}
	/**
	 * 断点续传上传实例
	 */
	public static void testResumableUpload(){
		
	    PolyvSDKClient client = PolyvSDKClient.getInstance();

		String vid = "";
		try {
			vid = client.resumableUpload("/Users/hhl/Desktop/videos/名字abc.mp4", "标题11", "", "", 1,new Progress(){
				public void run(long offset, long max) {
					// TODO Auto-generated method stub
					int percent = (int)(offset*100/max);
					System.out.println(percent);

				}
				
				
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		System.out.println(vid);
	}
	public static void testGet() {
		try {
			Video v = PolyvSDKClient.getInstance().getVideo("sl8da4jjbx33489b03e87dd99641901d_s");
			System.out.println(v.getDuration());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testUpload() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				Video v;
				try {
					v = PolyvSDKClient.getInstance().upload("/Users/hhl/Downloads/test.avi",
							"我的标题", "tag", "desc", 0);
					System.out.println(v.getFirstImage());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		t.start();
		
		while(true){
			int percent = PolyvSDKClient.getInstance().getPercent();
			if(percent==100){
				break;
			}
			System.out.println("upload percent: " + percent + "%");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

	public static void testDeleteVideo() {
		try {

			boolean result = PolyvSDKClient.getInstance().deleteVideo(
					"sl8da4jjbxa1077082a56e35adef93c4_s");
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testListVideo() {
		try {
			List<Video> list = PolyvSDKClient.getInstance().getVideoList(1, 20);
			for (int i = 0; i < list.size(); i++) {
				Video v = list.get(i);
				System.out.println(v.getVid() + "/" + v.getTitle());
			}
			System.out.println("----查看结束----");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
