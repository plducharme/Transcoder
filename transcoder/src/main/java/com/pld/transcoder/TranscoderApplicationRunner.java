package com.pld.transcoder;

import static org.mockito.Mockito.timeout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TranscoderApplicationRunner implements ApplicationRunner {

	private static String SOURCE_DIR = "./in";
	private static String TARGET_DIR = "./out";
	private static String ARCHIVED_DIR = "./archived";
	
	private static final byte[] UTF16_BIG_ENDIAN = { (byte)0xFE, (byte)0xFF};
	private static final byte[] UTF16_LITTLE_ENDIAN = { (byte)0xFF, (byte)0xFE};
	
	private static final Logger log = LoggerFactory.getLogger(TranscoderApplicationRunner.class);
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Transcoder started");
		processArguments(args);
		File[] files = getFilesToProcess();
		processFiles(files);
	}
	
	private void processArguments(ApplicationArguments args) {
		Set<String> options = args.getOptionNames();
		for(String option: options) {
			if(option.equalsIgnoreCase("sourceDir")) {
				SOURCE_DIR = args.getOptionValues(option).get(0);
				log.info("Setting sourceDir to" + SOURCE_DIR);
			} else if(option.equalsIgnoreCase("targetDir")) {
				TARGET_DIR = args.getOptionValues(option).get(0);
				log.info("Setting targetDir to" + TARGET_DIR);
			} else if (option.equalsIgnoreCase("archivedDir")) {
				ARCHIVED_DIR = args.getOptionValues(option).get(0);
				log.info("Setting archivedDir to" + ARCHIVED_DIR);
			} else {
				log.info("Unknown option found: [" + option + "], ignoring");
			}
		}
		
	}
	
	private File[] getFilesToProcess(){
		File sourceDir = new File(SOURCE_DIR);
		return sourceDir.listFiles();
	}
	
	private void processFiles(File[] files) {
		log.info("Processing " + files.length + " files");
		for(int i = 0; i < files.length; i++) {
			log.info("Processing " + files[i].getName());
			try {
				InputStream is = new FileInputStream(files[i]);
				byte[] header = new byte[2];
				is.read(header);
				is.close();
				if(Arrays.equals(header, UTF16_BIG_ENDIAN)) {
					log.info("Found UTF-16 Big Endian");
					transcodeFile(files[i], StandardCharsets.UTF_16BE, StandardCharsets.UTF_8);
				} else if (Arrays.equals(header, UTF16_LITTLE_ENDIAN)) {
					log.info("Found UTF-16 LITTLE Endian");
					transcodeFile(files[i], StandardCharsets.UTF_16LE, StandardCharsets.UTF_8);
				} else {
					log.info("File has nos UTF-16 BOM");
					log.debug("header " + String.format("0x%02X", header[0]) + " " + String.format("0x%02X", header[1]));
				}
					
			} catch (IOException e) {
				log.error("Error while processing " + files[i], e);
			}
		}
	}
	
	private void transcodeFile(File srcFile, Charset srcEncoding, Charset tgtEncoding) {
		File tgtFile = new File(TARGET_DIR + File.separator + srcFile.getName());
		BufferedReader br; 
		BufferedWriter bw;  
	          
		char[] buffer = new char[16384];
	    int read;
	    
	    try {
	    	br = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), srcEncoding));
	    	bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tgtFile), tgtEncoding));
			while ((read = br.read(buffer)) != -1)
				bw.write(buffer, 0, read);
			
			br.close();
			bw.close();
		} catch (IOException e) {
			log.error("Error while processing file " + srcFile.getName(), e);
			return;
		} 
	    
	    log.info("Transcoding done, moving files");
	    moveProcessedFile(srcFile);
	}
	
	private void moveProcessedFile(File srcFile) {
		String tgtFile = ARCHIVED_DIR + File.separator + srcFile.getName();
		try {
			Path temp = Files.move(srcFile.toPath(), Paths.get(tgtFile));
			if(temp != null) {
				log.info("Moved " + srcFile.getName() + " to " + tgtFile);
			} else {
				log.error("Error while moving file to archived directory");
			}
		} catch (IOException e) {
			log.error("Error while moving file to archieved directory", e);
		}  
		
	}
	
	
	

}
