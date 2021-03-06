/*
 * Copyright (c) 2015 phrack. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */

package com.shootoff.plugins;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shootoff.camera.Shot;
import com.shootoff.targets.TargetRegion;
import com.shootoff.targets.io.TargetIO;

public class TestRandomShoot {
	private PrintStream originalOut;
	private ByteArrayOutputStream stringOut = new ByteArrayOutputStream();
	private PrintStream stringOutStream = new PrintStream(stringOut);
	
	@Before
	public void setUp() {
		new JFXPanel(); // Initialize the JFX toolkit
		
		TextToSpeech.silence(true);
		originalOut = System.out;
		System.setOut(stringOutStream);
	}
	
	@After
	public void tearDown() {
		TextToSpeech.silence(false);
		System.setOut(originalOut);
	}

	@Test
	public void testNoTarget() throws IOException {
		List<Group> targets = new ArrayList<Group>();
		
		RandomShoot rs = new RandomShoot(targets);
		
		assertEquals("This training protocol requires a target with subtargets\n", stringOut.toString());
		stringOut.reset();
		
		rs.reset(targets);
		
		assertEquals("This training protocol requires a target with subtargets\n", stringOut.toString());
	}

	@Test
	public void testFiveSmallTarget() throws IOException {
		List<Group> targets = new ArrayList<Group>();
		targets.add(TargetIO.loadTarget(new File("targets" + File.separator + 
				"SimpleBullseye_five_small.target")).get());
		
		RandomShoot rs = new RandomShoot(targets);
		
		// Make sure initial state makes sense
		
		assertEquals(5, rs.getSubtargets().size());
		
		assertTrue(rs.getSubtargets().contains("1"));
		assertTrue(rs.getSubtargets().contains("2"));
		assertTrue(rs.getSubtargets().contains("3"));
		assertTrue(rs.getSubtargets().contains("4"));
		assertTrue(rs.getSubtargets().contains("5"));
		
		String firstSubtarget = rs.getSubtargets().get(rs.getCurrentSubtargets().peek());
		
		assertTrue(stringOut.toString().startsWith("shoot subtarget " + firstSubtarget));
		stringOut.reset();
		
		// Simulate missing a shot
		
		rs.shotListener(new Shot(Color.GREEN, 0, 0, 0, 2), Optional.empty());
		
		assertEquals("shoot " + firstSubtarget + "\n", stringOut.toString());
		stringOut.reset();
		
		// Simulate a hit
		
		TargetRegion expectedRegion = null;
		
		for (Node node : targets.get(0).getChildren()) {
			expectedRegion = (TargetRegion)node;
			
			if (expectedRegion.getTag("subtarget").equals(firstSubtarget)) break;
		}
		
		int oldSize = rs.getCurrentSubtargets().size();
		
		rs.shotListener(new Shot(Color.GREEN, 0, 0, 0, 2), Optional.of(expectedRegion));
		
		if (oldSize > 1) {
			assertEquals(oldSize - 1, rs.getCurrentSubtargets().size());
		} else {
			String nextSubtarget = rs.getSubtargets().get(rs.getCurrentSubtargets().peek());
			assertTrue(stringOut.toString().startsWith("shoot subtarget " + nextSubtarget));
			stringOut.reset();
		}
	}	
}
