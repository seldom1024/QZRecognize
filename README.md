# QZRecognize
强智教务系统验证码识别
 [原文](https://blog.csdn.net/u013555315/article/details/104336886)
之前没有想过要通过爬取页面的方式来获取数据，想想毕竟api得到的数据有限，国内使用强智系统的不少，也算是做了点贡献吧。
识别的过程倒是蛮顺利。肉眼验证似乎没有不正确的
话不多说，我们看到的登录界面的验证码是这样的：
![登录](https://img-blog.csdnimg.cn/20200215234307657.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTM1NTUzMTU=,size_16,color_FFFFFF,t_70)
如果你的目标和这个一毛一样，那么你便可以拿来用咯
思路很清晰，首先我们知道图像实际上是个二维数组，我们将图片二值化，即处理成只有黑白两色的图像，与训练好的的二维数组逐一相比较，最接近哪个便是哪个字符。
如字母a的二维数组：

```java
//a
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0},
			{0,0,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,1,1,1,1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0},
			{0,0,0,1,1,1,1,0,0,1,1,1,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}}
```

## 图片分析

验证码形如![验证码示例](https://img-blog.csdnimg.cn/20200215235854911.jpg)，经过爬取大量验证码可以发现，验证码图片为80*40px，周围有1px黑边，实际上黑边我忽略了，因为再后面的切割中被剪掉了。接着说图片，图片只含有小写字母和数字，没有字母o和数字0，即一共34个字符，后期要至少准备34个二维数组用于比较

## 进一步分析
打开ps
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200216001048904.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTM1NTUzMTU=,size_16,color_FFFFFF,t_70)
找一个图片有重合的例子，发现每个字符占水平20px，垂直24px,可以初步处理下，裁剪掉上下的空白：

```java
BufferedImage image = ImageIO.read(f);
image = image.getSubimage(0, 9, 80, 24);
```
## 二值化

```java
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

```
按照前面的分析将图片分割成4部分，分别处理之：

```java
		int width = image.getWidth();
		int height = image.getHeight();

		//System.out.println("width\t" + width + "\theight\t" + height);
		//int subWidth = width / 4;
		newim[0] = binaryImage(image.getSubimage(4, 0, 20, height));
		newim[1] = binaryImage(image.getSubimage(22, 0, 20, height));
		newim[2] = binaryImage(image.getSubimage(40, 0, 20, height));
		newim[3] = binaryImage(image.getSubimage(58, 0, 20, height));
```
实际上现在每个分割的图片便可以用0,1表示，如果用██填充的话。。。：

```java
    ████████████                        
  ██████████████████                    
  ██          ████████                  
                ██████                  
                ██████                  
                ██████                  
              ██████                    
            ██████                      
    ████████████                        
    ████████████████                    
              ████████                  
                ████████                
                  ██████                
                  ██████                
                  ██████                
  ████          ██████                  
  ██████████████████                    
    ██████████████                      
                                        
                                      
  
					{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
					{0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0},
					{0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0},
					{0,1,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0},
					{0,1,1,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
					{0,1,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0},
					{0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
					{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}},                                      
```
之后我们用训练好的数组比较选出结果即可

```java
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
		
```
效果不错
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200216003136438.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTM1NTUzMTU=,size_16,color_FFFFFF,t_70)
完整代码：github正在上传

参考: [java图像处理](https://blog.csdn.net/wokuailewozihao/article/details/79742651)

