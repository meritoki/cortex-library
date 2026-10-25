package com.meritoki.library.cortex.model;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.meritoki.library.controller.node.NodeController;
import com.meritoki.library.cortex.model.network.square.Squared;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SquareNetworkSaveLoadTest {

//	static Document document = new Document();
//	
//	@BeforeAll
//	public static void initialize() {
//		document.cortex = new Squared(Squared.BRIGHTNESS, 0, 0, 7, 1, 0);
//		document.cortex.load();
//	}
//	
//	@Test
//	@Order(1)
//	public void save() {
//		File file = new File("test/square-network.json");
//		NodeController.saveJson(file, document);
//	}
//	
//	@Test
//	@Order(2)
//	public void load() {
//		File file = new File("test/square-network.json");
//		document = (Document)NodeController.openJson(file, Document.class);
//		document.cortex.load();
//	}
	
}
