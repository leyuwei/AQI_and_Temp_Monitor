package com.leyuwei.pmtestbt;

/**
 * 
 * Code below is specially designed for author's graduation design. 
 * Anyone who quote this must be authorized by the author.
 * Any actions without authorization is strictly prohibited.
 * 
 * @author jumperle(乐煜炜，电子信息工程2013-1班，学号2220132334) from Dalian Maritime University
 * @category Application for graduation design 2017
 * @version 1.0.0
 * @since 2017/04/08 13:26
 * @description ToolBox for other classes to use
 *
 */

public class ToolBox {
	/*
	 * Functions' definitions
	 */
	
	/**
	 * @author jumperle
	 * @category 数据协议打包，共9字节，最后两字节是校验位
	 * @param AQI 空气质量指数，范围0~1000
	 * @param H 当前时
	 * @param M 当前分
	 * @param S 当前秒
	 * @param Mon 当前月
	 * @param Day 当前日
	 * @return 数据协议包
	 */
	public static int[] framePack(int AQI, int H, int M, int S, int Mon, int Day){
		int[] a = new int[9];
		a[0]=AQI/256;
		a[1]=AQI%256;
		a[2]=H;
		a[3]=M;
		a[4]=S;
		a[5]=Mon;
		a[6]=Day;
		int sum = AQI+H+M+S+Mon+Day;
		a[7]=sum/256;
		a[8]=sum%256;
		return a;
	}
}
