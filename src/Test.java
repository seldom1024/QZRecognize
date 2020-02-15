import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
/**
 * 
 * @author shuo747
 *
 */
public class Test {

	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();    //获取开始时间
		File testDir = new File("jpg");
		for (File file : testDir.listFiles()) {
			BufferedImage image = ImageIO.read(file);
			String result = QZRC.RC(image);
			file.renameTo(new File(file.getParentFile(), result + ".jpg"));
			System.out.println("rc " + result);
		}

		long endTime = System.currentTimeMillis();    //获取结束时间

		System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
	}

}
