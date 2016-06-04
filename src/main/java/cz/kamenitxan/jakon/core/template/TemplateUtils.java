package cz.kamenitxan.jakon.core.template;

import cz.kamenitxan.jakon.core.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
 */
public abstract class TemplateUtils {
	public static TemplateEngine getEngine() {
		return Settings.getTemplateEngine();
	}

	public static void saveRenderedPage(String content, String path) {
		try {
			File file = new File(Settings.getOutputDir() +  "/" + path + ".html");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				boolean created = file.createNewFile();
				if (!created) {
					throw new IOException("Could not create file.");
				}
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Copies a directory tree
	 *
	 * @param fromS
	 * @param toS
	 */
	public static void copy(String fromS, String toS) {
		Path from = Paths.get(fromS);
		Path to = Paths.get(toS);

		validate(from);
		try {
			Files.walkFileTree(from, EnumSet.of(FileVisitOption.FOLLOW_LINKS),Integer.MAX_VALUE,new CopyDirVisitor(from, to));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void validate(Path... paths) {
		for (Path path : paths) {
			Objects.requireNonNull(path);
			if (!Files.isDirectory(path)) {
				throw new IllegalArgumentException(String.format("%s is not a directory", path.toString()));
			}
		}
	}

	static class CopyDirVisitor extends SimpleFileVisitor<Path> {

		private Path fromPath;
		private Path toPath;
		private StandardCopyOption copyOption;


		public CopyDirVisitor(Path fromPath, Path toPath, StandardCopyOption copyOption) {
			this.fromPath = fromPath;
			this.toPath = toPath;
			this.copyOption = copyOption;
		}

		public CopyDirVisitor(Path fromPath, Path toPath) {
			this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

			Path targetPath = toPath.resolve(fromPath.relativize(dir));
			if(!Files.exists(targetPath)){
				Files.createDirectory(targetPath);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

			Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
			return FileVisitResult.CONTINUE;
		}
	}
}
