package com.loserico.common.lang.utils;

import com.loserico.common.lang.exception.IORuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.Thread.currentThread;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * IO 读写工具类
 * <p>
 * Copyright: Copyright (c) 2019/10/15 10:59
 * <p>
 * Company: Sexy Uncle Inc.
 * <p>
 *
 * @author Rico Yu  ricoyu520@gmail.com
 * @version 1.0
 */
@Slf4j
public class IOUtils {
	
	/**
	 * The Unix directory separator character.
	 */
	public static final String DIR_SEPARATOR_UNIX = "/";
	
	/**
	 * The Windows directory separator character.
	 */
	public static final char DIR_SEPARATOR_WINDOWS = '\\';
	/**
	 * The Unix line separator string.
	 */
	public static final String LINE_SEPARATOR_UNIX = "\n";
	
	public static final String CLASSPATH_PREFIX = "classpath*:";
	
	/**
	 * 从InputStream读取字符串
	 *
	 * @param in
	 * @return String
	 */
	public static String readFileAsString(InputStream in) {
		StringBuilder result = new StringBuilder();
		try (Scanner scanner = new Scanner(in)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append(LINE_SEPARATOR_UNIX);
			}
			scanner.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result.toString();
	}
	
	
	/**
	 * 读取文件系统中的文件
	 *
	 * @param filePath
	 * @return String
	 */
	public static String readFileAsString(String filePath) {
		StringBuilder result = new StringBuilder();
		File file = new File(filePath);
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append(LINE_SEPARATOR_UNIX);
			}
			scanner.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return result.toString();
	}
	
	/**
	 * 读取classpath下文件内容,文件不存在则返回null PathMatchingResourcePatternResolver
	 *
	 * @param fileName
	 * @return String
	 */
	public static String readClassPathFileAsString(String fileName) {
		InputStream in = readClasspathFileAsInputStream(fileName);
		if (in == null) {
			log.debug("Cannot file {} under classpath", fileName);
			return null;
		}
		return readFileAsString(in);
	}
	
	
	public static String readFile(Path path) {
		Objects.requireNonNull(path, "path cannot be null!");
		StringBuilder result = new StringBuilder();
		try (Scanner scanner = new Scanner(path.toFile())) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append(LINE_SEPARATOR_UNIX);
			}
			scanner.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return result.toString();
	}
	
	/**
	 * 将文件读到byte[]中
	 *
	 * @param filePath
	 * @return
	 */
	public static byte[] readFileAsBytes(String filePath) {
		Path path = Paths.get(filePath);
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("Read file as bytes failed!", e);
		}
		return new byte[0];
	}
	
	public static byte[] readFileAsBytes(Path path) {
		Objects.requireNonNull(path, "path cannot be null!");
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("Read file as bytes failed!", e);
		}
		return new byte[0];
	}
	
	public static byte[] readFileAsBytes(File file) {
		requireNonNull(file, "file 不能为null");
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			log.error("Read file as bytes failed!", e);
		}
		return new byte[0];
	}
	
	/**
	 * 将classpath的文件读到byte[]中
	 *
	 * @param fileName
	 * @return
	 */
	public static byte[] readClassPathFileAsBytes(String fileName) {
		File file = readClasspathFileAsFile(fileName);
		if (file == null) {
			return new byte[0];
		}
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			log.error("Read file as bytes failed!", e);
		}
		return new byte[0];
	}
	
	
	public static File readInputStreamAsFile(InputStream in) throws IOException {
		final File tempFile = File.createTempFile(RandomStringUtils.randomAlphanumeric(16), "tmp");
		tempFile.deleteOnExit();
		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(in, out);
		}
		return tempFile;
	}
	
	/**
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static InputStream readFileAsStream(String filePath) throws IOException {
		return Files.newInputStream(Paths.get(filePath), READ);
	}
	
	/**
	 * 读取classpath下某个文件，返回InputStream
	 *
	 * @param fileName
	 * @return
	 */
	public static InputStream readClasspathFileAsInputStream(String fileName) {
		ClassLoader classLoader = firstNonNull(currentThread().getContextClassLoader(), IOUtils.class.getClassLoader());
		URL url = classLoader.getResource(fileName);
		if (url == null && !fileName.startsWith(DIR_SEPARATOR_UNIX)) {
			log.debug("Cannot find file {} under classpath", fileName);
			url = classLoader.getResource("/" + fileName);
		}
		if (url != null) {
			try {
				return url.openStream();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return null;
			}
		}
		
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource resource = resolver.getResource(fileName);
		if (resource.exists()) {
			try {
				return resource.getInputStream();
			} catch (IOException e) {
				log.error("", e);
				return null;
			}
		}
		
		try {
			if (!fileName.startsWith(DIR_SEPARATOR_UNIX)) {
				fileName = CLASSPATH_PREFIX + DIR_SEPARATOR_UNIX + "**" + DIR_SEPARATOR_UNIX + fileName;
			}
			Resource[] resources = resolver.getResources(fileName);
			if (resources.length > 0) {
				return resources[0].getInputStream();
			}
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
		
		return null;
	}
	
	public static List<String> readLines(String filePath) {
		List<String> lines = new ArrayList<String>();
		File file = new File(filePath);
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				lines.add(line);
			}
			scanner.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return lines;
	}
	
	public static List<String> readLines(InputStream in) {
		List<String> lines = new ArrayList<String>();
		try (BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
		     Scanner scanner = new Scanner(bufferedInputStream)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				lines.add(line);
			}
			scanner.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return lines;
	}
	
	/**
	 * 持续从命令行读取数据并交给consumer, 收到exit或者quit退出
	 * @param consumer
	 */
	public static void readCommandLine(Consumer<String> consumer) {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));) {
			
			while (true) {
				String command = bufferedReader.readLine();
				if ("quit".equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
					break;
				}
				consumer.accept(command);
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		
	}
	
	/**
	 * 从InputStream读取字符串
	 *
	 * @param in
	 * @return String
	 */
	public static String readAsString(InputStream in) {
		return readAsString(in, true);
	}
	
	/**
	 * 从InputStream读取字符串
	 *
	 * @param in
	 * @param autoClose
	 * @return String
	 */
	public static String readAsString(InputStream in, boolean autoClose) {
		List<String> lines = new ArrayList<String>();
		try (BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
		     Scanner scanner = new Scanner(bufferedInputStream)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				lines.add(line);
			}
			scanner.close();
			if (autoClose) {
				in.close();
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return join(lines, "\n");
	}
	
	
	/**
	 * 读取classpath下某个文件，返回File
	 *
	 * @param fileName
	 * @return File
	 */
	public static File readClasspathFileAsFile(String fileName) {
		ClassLoader classLoader = firstNonNull(currentThread().getContextClassLoader(), IOUtils.class.getClassLoader());
		URL url = classLoader.getResource(fileName);
		if (url == null && !fileName.startsWith(DIR_SEPARATOR_UNIX)) {
			log.warn("Cannot find file {} under classpath", fileName);
			url = classLoader.getResource("/" + fileName);
		}
		if (url != null) {
			return new File(url.getFile());
		}
		
		/*
		 * Java Application中不带目录的时候可以查到
		 */
		List<File> files = Resources.getResources(fileName);
		if (!files.isEmpty()) {
			return files.get(0);
		}
		
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource resource = resolver.getResource(fileName);
		if (resource.exists()) {
			try {
				return resource.getFile();
			} catch (IOException e) {
				log.error("", e);
				return null;
			}
		}
		
		try {
			if (!fileName.startsWith(DIR_SEPARATOR_UNIX)) {
				fileName = CLASSPATH_PREFIX + DIR_SEPARATOR_UNIX + "**" + DIR_SEPARATOR_UNIX + fileName;
			}
			Resource[] resources = resolver.getResources(fileName);
			if (resources.length > 0) {
				return resources[0].getFile();
			}
			return null;
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
		
	}
	
	
	/**
	 * Write string data to file
	 *
	 * @param filePath
	 * @param data
	 * @return
	 */
	public static boolean write(String filePath, String data) {
		Objects.requireNonNull(filePath, "filePath cannot be null!");
		Path path = Paths.get(filePath);
		return write(path, data);
	}
	
	public static boolean write(String filePath, String data, Charset charset) {
		Objects.requireNonNull(filePath, "filePath cannot be null!");
		Path path = Paths.get(filePath);
		return write(path, data, charset);
	}
	
	/**
	 * Write string data to file
	 *
	 * @param path
	 * @param data
	 * @return
	 */
	public static boolean write(Path path, String data) {
		Objects.requireNonNull(path, "path cannot be null!");
		return write(path, Optional.of(data).orElse("").getBytes(UTF_8), CREATE, APPEND);
	}
	
	/**
	 * 用指定的编码格式写文件
	 *
	 * @param path
	 * @param data
	 * @param charset
	 * @return
	 */
	public static boolean write(Path path, String data, Charset charset) {
		Objects.requireNonNull(path, "path cannot be null!");
		return write(path, Optional.of(data).orElse("").getBytes(charset), CREATE, APPEND);
	}
	
	public static boolean write(String filePath, byte[] data) {
		Objects.requireNonNull(filePath, "filePath cannot be null!");
		Path path = Paths.get(filePath);
		return write(path, data);
	}
	
	public static boolean write(Path path, byte[] data) {
		Objects.requireNonNull(path, "path cannot be null!");
		return write(path, data, CREATE, APPEND);
	}
	
	/**
	 * Write byte[] data to file
	 *
	 * @param path
	 * @param data
	 * @param options
	 * @return
	 */
	public static boolean write(Path path, byte[] data, OpenOption... options) {
		Objects.requireNonNull(path, "path cannot be null!");
		createParentDir(path);
		try {
			Files.write(path, data, options);
			return true;
		} catch (IOException e) {
			log.error(format("Write data [{0}] to path [{1}] failed!", data, path), e);
		}
		return false;
	}
	
	/**
	 * 将content写入临时文件
	 *
	 * @param fileName
	 * @param suffix
	 * @param content
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static Path writeTempFile(String fileName, String suffix, String content,
	                                 Charset charset) throws IOException {
		Path path = tempFile(fileName, suffix).toPath();
		write(path, content, charset);
		return path;
	}
	
	/**
	 * @param path
	 * @return boolean
	 * @of Create parent directory if this path has a parent path and if not exist yet
	 * 不管是否执行了父目录的创建，返回父目录存在与否的最终状态
	 * true 存在
	 * false 不存在
	 */
	public static boolean createParentDir(Path path) {
		Optional.of(path.getParent())
				.ifPresent(parent -> {
					if (!Files.exists(parent, NOFOLLOW_LINKS)) {
						try {
							Files.createDirectories(parent);
						} catch (IOException e) {
							log.error(format("create parent directory [{0}] failed", parent), e);
						}
					}
				});
		return Files.exists(path.getParent(), NOFOLLOW_LINKS);
	}
	
	public static boolean createDir(Path path) {
		Optional.of(path)
				.ifPresent(dir -> {
					if (!Files.exists(dir, NOFOLLOW_LINKS)) {
						try {
							Files.createDirectories(dir);
						} catch (IOException e) {
							log.error(format("create directory [{0}] failed", dir), e);
						}
					}
				});
		return Files.exists(path, NOFOLLOW_LINKS);
	}
	
	/**
	 * Delete a file if exists
	 *
	 * @param path
	 * @return
	 */
	public static boolean deleteFile(Path path) {
		Objects.requireNonNull(path, "path cannot be null!");
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			log.error(format("Delete file {0} failed", path), e);
		}
		return true;
	}
	
	/**
	 * Delete specified directory with its sub-dir and all of tis files
	 *
	 * @param path
	 * @return
	 */
	public static boolean deleteDirectory(String path) {
		Path directory = Paths.get(path);
		return deleteDirectory(directory);
	}
	
	public static boolean deleteDirectory(File path) {
		Path directory = path.toPath();
		return deleteDirectory(directory);
	}
	
	/**
	 * Delete specified directory with its sub-dir
	 *
	 * @param path
	 * @return boolean 删除成功与否
	 */
	public static boolean deleteDirectory(Path path) {
		if (!Files.isDirectory(path, NOFOLLOW_LINKS)) {
			log.error("{} is not a directory!", path);
			return false;
		}
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			return true;
		} catch (IOException e) {
			log.error("Delete path " + path + " failed", e);
			return false;
		}
	}
	
	/**
	 * 将source文件移动到targetFolder
	 *
	 * @param source
	 * @param targetFolder
	 * @return
	 */
	public static void move(Path source, Path targetFolder) throws IOException {
		move(source, targetFolder, null);
	}
	
	/**
	 * 将source文件移动到targetFolder,并重命名为renameTo
	 *
	 * @param source
	 * @param targetFolder
	 * @param renameTo
	 * @return
	 */
	public static void move(Path source, Path targetFolder, String renameTo) throws IOException {
		if (Files.notExists(source, NOFOLLOW_LINKS)) {
			return;
		}
		
		if (Files.notExists(targetFolder, NOFOLLOW_LINKS)) {
			try {
				Files.createDirectories(targetFolder);
			} catch (Throwable e) {
				String msg = format("Create directory[{0}] failed", targetFolder.toString());
				log.error(msg, e);
				throw new IOException(msg, e);
			}
		}
		try {
			Path targetFile = null;
			if (isNotBlank(renameTo)) {
				targetFile = targetFolder.resolve(renameTo);
			} else {
				targetFile = targetFolder.resolve(source.getFileName());
			}
			Files.move(source, targetFile, REPLACE_EXISTING);
		} catch (Throwable e) {
			String msg = format("Move file[{0}] to [{1}] failed.", source, targetFolder);
			log.error(msg, e);
			throw new IOException(msg, e);
		}
	}
	
	
	/**
	 * 获取根目录
	 *
	 * @param path
	 * @return
	 */
	public static Path getRootDirectory(Path path) {
		return Optional.ofNullable(path.getRoot())
				.map(root -> {
					StringBuilder pathStr = new StringBuilder();
					pathStr.append(root.toString());
					Optional.of(path.subpath(0, 1)).ifPresent(sub -> pathStr.append(sub));
					return Paths.get(pathStr.toString());
				}).orElseGet(() -> path.subpath(0, 1));
	}
	
	/**
	 * 将path代表的文件写入OutputStream
	 *
	 * @param path
	 * @param out
	 * @throws IOException
	 */
	public static void copy(Path path, final OutputStream out) throws IOException {
		InputStream inputStream = Files.newInputStream(path, READ);
		final byte[] buf = new byte[2048];
		int len;
		while ((len = inputStream.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
		inputStream.close();
	}
	
	public static void copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buf = new byte[2048];
		int len;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
	}
	
	/*
	 * Copy one file to another place
	 */
	public static boolean copy(Path copyFrom, Path copyTo, CopyOption... options) {
		boolean parentCreateResult = createParentDir(copyTo);
		if (parentCreateResult) {
			try (InputStream is = new FileInputStream(copyFrom.toFile())) {
				Files.copy(is, copyTo, options);
				return true;
			} catch (IOException e) {
				log.error(format("copy from [{0}] to [{1}] failed!", copyFrom, copyTo), e);
			}
		}
		return false;
	}
	
	/**
	 * 通过NIO方式拷贝数据，每读取一部分数据就立刻写入输出流
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyPositive(InputStream in, OutputStream out) throws IOException {
		ReadableByteChannel inChannel = Channels.newChannel(in);
		WritableByteChannel outChannel = Channels.newChannel(out);
		
		ByteBuffer buffer = ByteBuffer.allocate(8192);
		int read;
		
		while ((read = inChannel.read(buffer)) > 0) {
			buffer.rewind();
			buffer.limit(read);
			
			while (read > 0) {
				read -= outChannel.write(buffer);
			}
			
			buffer.clear();
		}
	}
	
	public static void copyNegative(InputStream in, OutputStream out) throws IOException {
		ReadableByteChannel inChannel = Channels.newChannel(in);
		WritableByteChannel outChannel = Channels.newChannel(out);
		
		ByteBuffer buffer = ByteBuffer.allocate(8192);
		while (inChannel.read(buffer) != -1) {
			buffer.flip();
			outChannel.write(buffer);
			buffer.compact();
		}
		
		buffer.flip();
		while (buffer.hasRemaining()) {
			outChannel.write(buffer);
		}
		inChannel.close();
		outChannel.close();
	}
	
	/**
	 * 将数据从 InputStream 拷贝到 OutputStream，最后两个都关闭
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyAndClose(final InputStream in, final OutputStream out) throws IOException {
		try {
			copy(in, out);
			in.close();
			out.close();
		} catch (IOException ex) {
			closeSilently(in);
			closeSilently(out);
			throw ex;
		}
	}
	
	/**
	 * 保留文件的后缀，将文件名前缀替换为随机字符串+日期
	 *
	 * @param fileName
	 * @return String 随机生成的文件名
	 */
	public static String randomFileName(String fileName) {
		if (isBlank(fileName)) {
			return "";
		}
		int dotIndex = fileName.lastIndexOf(".");
		String suffix = dotIndex == -1 ? "" : fileName.substring(dotIndex);
		String baseName = RandomStringUtils.randomAlphanumeric(16);
		String timeSuffix = LocalDateTime.now().format(ofPattern("yyyyMMddHHmmss"));
		return String.join("", baseName, timeSuffix, suffix);
	}
	
	public static void closeSilently(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static BufferedReader toBufferedReader(InputStream in) {
		return new BufferedReader(new InputStreamReader(in, UTF_8));
	}
	
	public static BufferedReader toBufferedReader(InputStream in, Charset charset) {
		return new BufferedReader(new InputStreamReader(in, charset));
	}
	
	public static ByteArrayInputStream toByteArrayInputStream(File file) throws IOException {
		return new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
	}
	
	/**
	 * 在临时目录创建指定后缀的文件
	 *
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
	public static File tempFile(String suffix) throws IOException {
		if (suffix != null && suffix.indexOf(".") != 0) {
			suffix = "." + suffix;
		}
		return File.createTempFile(RandomStringUtils.randomAlphanumeric(16), suffix);
	}
	
	/**
	 * 在临时目录创建指定文件名和后缀的文件 java.io.tmpdir
	 *
	 * @param fileName
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
	public static File tempFile(String fileName, String suffix) throws IOException {
		Objects.requireNonNull(fileName, "fileName 不可以为null哦");
		if (suffix != null && suffix.indexOf(".") != 0) {
			suffix = "." + suffix;
		}
		String tempDir = System.getProperty("java.io.tmpdir");
		return Paths.get(tempDir, fileName + suffix).toFile();
	}
	
	/**
	 * 获取文件大小 如果path代表一个目录，获取目录中所有文件大小之和
	 *
	 * @param path
	 * @return
	 */
	public static long length(Path path) {
		return FileUtils.sizeOf(path.toFile());
	}
}
