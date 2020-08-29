package com.meritoki.library.cortex.model;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.meritoki.library.cortex.controller.NodeController;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HexagonGroupSaveLoadTest {

	static Document document = new Document();
	
	@BeforeAll
	public static void initialize() {
		document.cortex = new Group(Group.HEXAGONAL);
		document.cortex.load();
	}
	
	@Test
	@Order(1)
	public void save() {
		File file = new File("test/hexagon-group.json");
		NodeController.saveJson(file, document);
	}
	
	@Test
	@Order(2)
	public void load() {
		File file = new File("test/hexagon-group.json");
		document = (Document)NodeController.openJson(file, Document.class);
		document.cortex.load();
	}
	
}
