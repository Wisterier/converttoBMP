package 图片学习;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

/*
 * @author Wisterier
 */
public class BMP {

	public BMP(String input, String output) {
		toBMP(input, output);
	}

	String xx(char[] k) {
		String r = "";
		for (int j = 0; j < 8 - k.length; j++) {
			r = r + "0";
		}
		return r + String.valueOf(k);
	}

	int hextoint(String temp) {
		char[] c = temp.toCharArray();
		int temp3, temp4, temp5;
		if (c[0] == 'a') {
			temp3 = 10;
		}
		if (c[0] == 'b') {
			temp3 = 11;
		}
		if (c[0] == 'c') {
			temp3 = 12;
		}
		if (c[0] == 'd') {
			temp3 = 13;
		}
		if (c[0] == 'e') {
			temp3 = 14;
		}
		if (c[0] == 'f') {
			temp3 = 15;
		} else {
			temp3 = c[0] - '0';
		}
		if (c[1] == 'a') {
			temp4 = 10;
		}
		if (c[1] == 'b') {
			temp4 = 11;
		}
		if (c[1] == 'c') {
			temp4 = 12;
		}
		if (c[1] == 'd') {
			temp4 = 13;
		}
		if (c[1] == 'e') {
			temp4 = 14;
		}
		if (c[1] == 'f') {
			temp4 = 15;
		} else {
			temp4 = c[1] - '0';
		}
		temp5 = temp3 * 16 + temp4;
		return temp5;
	}

	public int[] whtohex(int worh) {
		// w0xff ff ff ff h 0xff ff ff ff
		// 00 00 0e ff convert to ff 0e 00 00
		// ff 03 e0 f5 convert to f5 e0 03 ff
		// 0 1 - 6 7
		// 2 3 - 4 5
		// 4 5 - 2 3
		// 6 7 - 0 1
		int[] a = new int[4];
		String[] tx = new String[4];
		// 把8个十六进制数拆成4个字符串
		String temp = Integer.toHexString(worh);
		char[] c1 = temp.toCharArray();
		char[] c = xx(c1).toCharArray();
		// xx()补零 eff - 0x00 00 0e ff
		int x = 3;
		for (int d = 0; d < c.length; d += 2) {
			tx[x] = String.valueOf(c[d]) + String.valueOf(c[d + 1]);
			x--;
		}
		// 颠倒 0x00 00 0e ff - 0xff 0e 00 00
		for (int i = 0; i < tx.length; i++) {
			int temp1 = hextoint(tx[i]);
			//
			a[i] = temp1;
		}

		return a;
	}

	public void toBMP(String input, String output) {
		FileOutputStream fos;
		BufferedImage bf;
		File f = new File(output);
		Random r = new Random();
		try {
			bf = ImageIO.read(new File(input));

			byte[] BM = "BM".getBytes();
			int[] size = new int[] { 0, 0, 0, 0 };
			int[] w = whtohex(bf.getWidth());// 重要! 宽
			int[] h = whtohex(bf.getHeight());// 重要! 高
			/*
			 * 理论上 size=14+40 +w*h*3=54+w*h*3 实际上windows系统中读取4字节更快， 因此要补零，使得文件大小能被四整除。
			 * bmp是自下而上，自左向右制图，且RGB也是反着的，BGR。 bmp要使得每行的像素的RGB字节总数能被4整除,补零的机制如下(只考虑宽w):
			 * 补零字节个数 n=[0,3] (w*3+n)%4=0 即可求出n. 有了n之后，在每行的某位补n个字节的0。
			 */
			byte[] 保留字符 = new byte[] { 0, 0, 0, 0 };
			byte[] 信息头 = new byte[] { 54, 00, 00, 00 };
			// 信息头 除了wh其他都没什么重要的，都可以为0
			byte[] 信息头size = new byte[] { 40, 0, 0, 0 };
			byte[] o = new byte[] { 1, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			// 24位(即4字节,8个十六进制数)一个像素
			byte[] p = new byte[16];
			for (int a = 0; a < 16; a++) {
				p[a] = 0;
			}
			// 信息头写入 总54字节
			int n = 0, width = 0;
			// 判断补零个数
			for (int a = 0; a < 4; a++) {
				if ((width * 3 + a) % 4 == 0) {
					n = a;
				}
			}
			fos = new FileOutputStream(f);
			fos.write(BM);
			for (int a = 0; a < 4; a++) {
				fos.write(size[a]);
			}
			fos.write(保留字符);
			fos.write(信息头);
			fos.write(信息头size);
			for (int a = 0; a < 4; a++) {
				fos.write(w[a]);
			}
			for (int a = 0; a < 4; a++) {
				fos.write(h[a]);
			}

			fos.write(o);
			fos.write(p);
			// 写入信息头 共54字节
			int r2, g1, b1;
			int count = 0;// 宽像素计数器
			for (int i = bf.getHeight() - 1; i >= 0; i--) {

				for (int j = 0; j < bf.getWidth(); j++) {
					if (count == width) {
						for (int a = 0; a < n; a++) {
							fos.write(0);
						}
						count = 0;
					}
//					System.out.println("0x" + Integer.toHexString(bf.getRGB(j, i) & 0xffffff));
//					System.out.println((bf.getRGB(j, i) & 0xffffff));
//					r2 = (bf.getRGB(j, i) & 0xff0000) >> 16;// Red
//					g1 = (bf.getRGB(j, i) & 0xff00) >> 8;// Green
//					b1 = (bf.getRGB(j, i) & 0xff);// Blue
					Color c = new Color(bf.getRGB(j, i));
					r2 = c.getRed();
					g1 = c.getGreen();
					b1 = c.getBlue();
					fos.write(b1);
					fos.write(g1);
					fos.write(r2);
					// 寫入BGR
					count++;
					System.out.println("(" + j + "," + i + ")" + r2 + " " + g1 + " " + b1);

				}
			}
			System.out.println(bf.getHeight());
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
