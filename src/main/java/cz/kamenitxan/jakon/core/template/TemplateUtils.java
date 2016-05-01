package cz.kamenitxan.jakon.core.template;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
 */
public class TemplateUtils {
	private static TemplateEngine engine;

	static {
		engine = new Pebble();
	}

	public static TemplateEngine getEngine() {
		return engine;
	}

	public static void saveRenderedPage(String content, String path) {
		try {
			File file = new File("out/" + path + ".html");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				boolean created = file.createNewFile();
				if (!created) {
					throw new IOException("Could not create file.");
				}
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
