package com.meritoki.library.cortex.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class NodeController {
	private static Logger logger = LogManager.getLogger(NodeController.class.getName());

	public static void main(String[] args) {
		System.out.println(getUserHome());
	}
	
	public static String getSeperator() {
		return FileSystems.getDefault().getSeparator();
	}
	
	public static String getUserHome() {
		return System.getProperty("user.home");
	}
	
	public static BufferedImage openBufferedImage(String filePath, String fileName) {
		logger.debug("openBufferedImage(" + filePath + ", " + fileName + ")");
		return openBufferedImage(new java.io.File(filePath + getSeperator() + fileName));
	}

	public static BufferedImage openBufferedImage(java.io.File file) {
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(file);
		} catch (IOException ex) {
			logger.error("IOException "+ex.getMessage());
		}
		return bufferedImage;
	}

	@JsonIgnore
	public static Object openJson(java.io.File file, Class className) {
		logger.info("openJson(" + file + ", " + className + ")");
		Object object = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			object = mapper.readValue(file, className);
		} catch (JsonGenerationException e) {
			logger.error(e);
		} catch (JsonMappingException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return object;
	}


	public static <T> Object openJson(File file, TypeReference<List<T>> typeReference) {
		logger.info("openJson(" + file + ", " + typeReference + ")");
		Object object = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			object = mapper.readValue(file, typeReference);
			
		} catch (JsonGenerationException e) {
			logger.error(e);
		} catch (JsonMappingException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return object;
	}


	@JsonIgnore
	public static void saveJson(String path, String name, Object object) {
		logger.info("saveJson("+path+","+name+", object)");
		saveJson(new java.io.File(path+getSeperator()+name), object);
	}

	@JsonIgnore
	public static void saveJson(File file, Object object) {
		logger.info("saveJson("+file.getAbsolutePath()+",object)");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(file, object);
		} catch (IOException ex) {
			logger.error(ex);
		}
	}



	
	@JsonIgnore
	public static Object toJson(String string, Class className) {
		logger.info("toJson(" + string + ", " + className + ")");
		Object object = null;
		ObjectMapper mapper = new ObjectMapper();
//		mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			object = mapper.readValue(string, className);
			
		} catch (JsonGenerationException e) {
			logger.error(e);
		} catch (JsonMappingException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return object;
	}
	
}
