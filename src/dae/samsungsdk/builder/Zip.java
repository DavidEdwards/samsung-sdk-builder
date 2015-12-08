package dae.samsungsdk.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Credits: http://stackoverflow.com/a/15970455/503508
 */
public class Zip {
	private List<String> fileList = new ArrayList<String>();

	private String sourcePath = null;

	public void zipIt(String zipFile) {
		if(sourcePath == null) {
			throw new RuntimeException("The source directory path was not provided.");
		}
		
		byte[] buffer = new byte[1024];
		String source = "";
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			try {
				source = this.sourcePath.substring(this.sourcePath.lastIndexOf("\\") + 1,
						this.sourcePath.length());
			}
			catch (Exception e) {
				source = this.sourcePath;
			}
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);

//			System.out.println("Output to Zip : " + zipFile);
			FileInputStream in = null;

			for (String file : this.fileList) {
//				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(this.sourcePath + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				}
				finally {
					in.close();
				}
			}

			zos.closeEntry();
//			System.out.println("Folder successfully compressed");

		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				zos.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setSourcePath(File path) {
		this.sourcePath = path.getAbsolutePath();
		generateFileList(path);
	}

	private void generateFileList(File node) {
		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.getAbsolutePath()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateZipEntry(String file) {
		return file.substring(this.sourcePath.length() + 1);
	}
}
