package com.leyuwei.pmtestbt;

/**
 * 
 * Code below is specially designed for author's graduation design. 
 * Anyone who quote this must be authorized by the author.
 * Any actions without authorization is strictly prohibited.
 * 
 * @author jumperle(����쿣�������Ϣ����2013-1�࣬ѧ��2220132334) from Dalian Maritime University
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
	 * @category ����Э��������9�ֽڣ�������ֽ���У��λ
	 * @param AQI ��������ָ������Χ0~1000
	 * @param H ��ǰʱ
	 * @param M ��ǰ��
	 * @param S ��ǰ��
	 * @param Mon ��ǰ��
	 * @param Day ��ǰ��
	 * @return ����Э���
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
