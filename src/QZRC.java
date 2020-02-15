import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
/**
 * 
 * @author shuo747
 *
 */
public class QZRC {

	public static BufferedImage grayImage(BufferedImage bufferedImage) throws Exception {

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		BufferedImage grayBufferedImage = new BufferedImage(width, height, bufferedImage.getType());
		for (int i = 0; i < bufferedImage.getWidth(); i++) {
			for (int j = 0; j < bufferedImage.getHeight(); j++) {
				final int color = bufferedImage.getRGB(i, j);
				final int r = (color >> 16) & 0xff;
				final int g = (color >> 8) & 0xff;
				final int b = color & 0xff;
				int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);
				int newPixel = colorToRGB(255, gray, gray, gray);
				grayBufferedImage.setRGB(i, j, newPixel);
			}
		}

		return grayBufferedImage;

	}

	/**
	 * 颜色分量转换为RGB值
	 * 
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	private static int colorToRGB(int alpha, int red, int green, int blue) {

		int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red;
		newPixel = newPixel << 8;
		newPixel += green;
		newPixel = newPixel << 8;
		newPixel += blue;

		return newPixel;

	}

	public static BufferedImage binaryImage(BufferedImage image) throws Exception {
		int w = image.getWidth();
		int h = image.getHeight();
		float[] rgb = new float[3];
		double[][] coordinates = new double[w][h];
		int black = new Color(0, 0, 0).getRGB();
		int white = new Color(255, 255, 255).getRGB();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
		;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int pixel = image.getRGB(x, y);
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				float avg = (rgb[0] + rgb[1] + rgb[2]) / 3;
				coordinates[x][y] = avg;

			}
		}

		// 这里是阈值，白底黑字还是黑底白字，大多数情况下建议白底黑字，后面都以白底黑字为例
		double SW = 192;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (coordinates[x][y] < SW) {
					bi.setRGB(x, y, black);
				} else {
					bi.setRGB(x, y, white);
				}
			}
		}

		return bi;
	}

	// 自己加周围8个灰度值再除以9，算出其相对灰度值
	public static double getGray(double[][] coordinates, int x, int y, int w, int h) {
		double rs = coordinates[x][y] + (x == 0 ? 255 : coordinates[x - 1][y])
				+ (x == 0 || y == 0 ? 255 : coordinates[x - 1][y - 1])
				+ (x == 0 || y == h - 1 ? 255 : coordinates[x - 1][y + 1]) + (y == 0 ? 255 : coordinates[x][y - 1])
				+ (y == h - 1 ? 255 : coordinates[x][y + 1]) + (x == w - 1 ? 255 : coordinates[x + 1][y])
				+ (x == w - 1 || y == 0 ? 255 : coordinates[x + 1][y - 1])
				+ (x == w - 1 || y == h - 1 ? 255 : coordinates[x + 1][y + 1]);
		return rs / 9;
	}

	
	public static String RC(BufferedImage image) throws Exception {
		image = image.getSubimage(0, 9, 80, 24);
		// RC2.removeEdge(image);
		// RC2.C2(image);

		BufferedImage newim[] = new BufferedImage[4];
		// 将图像分成四块，因为要处理的文件有四个数字。

		int width = image.getWidth();
		int height = image.getHeight();

		//System.out.println("width\t" + width + "\theight\t" + height);
		int subWidth = width / 4;
		newim[0] = binaryImage(image.getSubimage(4, 0, 20, height));
		newim[1] = binaryImage(image.getSubimage(22, 0, 20, height));
		newim[2] = binaryImage(image.getSubimage(40, 0, 20, height));
		newim[3] = binaryImage(image.getSubimage(58, 0, 20, height));

		for (int i = 0; i < newim.length; ++i) {
			// File outputfile = new File("e:/1/qz/" + i + ".jpg");
			// ImageIO.write(newim[i], "jpg", outputfile);
			//print2(newim[i]);
		}

		int g[][][] = new int[4][][];
		// 将图像分成四块，因为要处理的文件有四个数字。

		for (int i = 0; i < g.length; i++) {
			g[i] = img2Array(newim[i]);
		}

		StringBuilder sb = new StringBuilder();
		int count = -1;

		double total = 1;
		int hit = 0;
		while (++count < 4) {
			
			int chCount = 0;
			int target = 0;//最接近的指针
			double temp = -1.0;
			while (chCount < MyCharacter.g.length) {
			
				total = 1;
				hit = 0;
				
				for (int i = 0; i < g[count].length; i++) {
					for (int j = 0; j < g[count][i].length; j++) {
						if (MyCharacter.g[chCount][i][j] == 1) {
							//System.out.print("-");
							++total;
							if(g[count][i][j] == MyCharacter.g[chCount][i][j]) {
								//System.out.print("+");
								++hit;
							}
								

						}
					}
				}
				//System.out.println(hit);
				//System.out.println(total);
				//System.out.println(MyCharacter.ch[chCount]+" "+hit/total);
				if(hit/total > temp) {
					target = chCount;
					temp = hit/total;
				}
				else {
					
				}
				++chCount;
				
			}
			//比例最大的给他
			sb.append(MyCharacter.ch[target]);
		}
		
		return sb.toString();
		
		
	}
	public static void main(String[] args) throws Exception {
		File testDir = new File("jpg");
		File f = null;
		for (File file : testDir.listFiles()) {
			f = file;
			break;
		}

		BufferedImage image = ImageIO.read(f);
		image = image.getSubimage(0, 9, 80, 24);
		// RC2.removeEdge(image);
		// RC2.C2(image);

		BufferedImage newim[] = new BufferedImage[4];
		// 将图像分成四块，因为要处理的文件有四个数字。

		int width = image.getWidth();
		int height = image.getHeight();

		//System.out.println("width\t" + width + "\theight\t" + height);
		//int subWidth = width / 4;
		newim[0] = binaryImage(image.getSubimage(4, 0, 20, height));
		newim[1] = binaryImage(image.getSubimage(22, 0, 20, height));
		newim[2] = binaryImage(image.getSubimage(40, 0, 20, height));
		newim[3] = binaryImage(image.getSubimage(58, 0, 20, height));

		for (int i = 0; i < newim.length; ++i) {
			// File outputfile = new File("e:/1/qz/" + i + ".jpg");
			// ImageIO.write(newim[i], "jpg", outputfile);
			print2(newim[i]);
		}

		int g[][][] = new int[4][][];
		// 将图像分成四块，因为要处理的文件有四个数字。

		for (int i = 0; i < g.length; i++) {
			g[i] = img2Array(newim[i]);
		}

		StringBuilder sb = new StringBuilder();
		int count = -1;

		double total = 1;
		int hit = 0;
		while (++count < 4) {
			
			int chCount = 0;
			int target = 0;//最接近的指针
			double temp = -1.0;
			while (chCount < MyCharacter.g.length) {
			
				total = 1;
				hit = 0;
				
				for (int i = 0; i < g[count].length; i++) {
					for (int j = 0; j < g[count][i].length; j++) {
						if (g[count][i][j] == 1) {
							//System.out.print("-");
							++total;
							if(g[count][i][j] == MyCharacter.g[chCount][i][j]) {
								//System.out.print("+");
								++hit;
							}
				
						}
					}
				}
				//System.out.println(hit);
				//System.out.println(total);
				System.out.println(MyCharacter.ch[chCount]+" "+hit/total);
				if(hit/total > temp) {
					target = chCount;
					temp = hit/total;
				}
				else {
					
				}
				++chCount;
				
			}
			System.out.println();
			//比例最大的给他
			sb.append(MyCharacter.ch[target]);
		}
		
		System.out.println(sb.toString());

		int count2 = -1;
		StringBuffer sb2 = new StringBuffer();
		while (++count2 < 4) {
			
			sb2.append("{");
			System.out.println("-");
			for (int i = 0; i < g[count2].length; i++) {
				sb2.append("{");
				for (int j = 0; j < g[count2][i].length; j++) {
					System.out.print(g[count2][i][j]);
					sb2.append(g[count2][i][j]);
					if (j == g[count2][i].length - 1)
						continue;
					sb2.append(",");
				}
				System.out.println();
				sb2.append("}");
				if (i == g[count2].length - 1)
					continue;
				sb2.append(",\n");

			}

			sb2.append("}\n\n");
			System.err.println();
			System.out.println(sb2.toString());
			System.out.println();

		}
		
		
		

	}

	private static int[][] img2Array(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int[][] res = new int[height][width];
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[i].length; j++) {
				int p = img.getRGB(j, i);
				if (p == -1)
					p = 0;
				else
					p = 1;
				res[i][j] = p;
				// System.out.print(p);

			}
			// System.out.println();

		}

		return res;
	}
	
	public static boolean isWhite(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 300) {
			return true;
		}
		return false;
	}

	public static void print2(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();

		System.out.println(width + " " + height);
		// if(true) return;

		for (int i = 0; i < height; ++i) {
			for (int j = 0; j < width; j++) {
				int p = img.getRGB(j, i);
				if (isWhite(p))
					System.out.print("  ");
				else
					System.out.print("██");

			}
			System.out.println("");
		}

		System.out.println("--------------------------------------");

	}

}
