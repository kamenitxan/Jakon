package cz.kamenitxan.jakon.core.template;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
 */
public class TemplateUtils {
	public static void saveRenderedPage(String templateName, String content) {
		try {
			File file = new File("out/" + "test" + ".html");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
