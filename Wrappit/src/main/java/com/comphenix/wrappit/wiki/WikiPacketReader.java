/*
  Copyright (C) dmulloy2 <http://dmulloy2.net>
  Copyright (C) Kristian S. Strangeland

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */
package com.comphenix.wrappit.wiki;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;

/**
 * Retrieve valuable information from the Minecraft Protocol Wiki.
 * 
 * @author Kristian
 */
public class WikiPacketReader {
	public static final String STANDARD_URL = "https://www.wiki.vg/Protocol";
	
	// Stored packet information
	private final Map<PacketType, WikiPacketInfo> packets;
	
	public WikiPacketReader() throws IOException {
		this(STANDARD_URL);
	}
	
	public WikiPacketReader(String url) throws IOException {
		packets = loadFromDocument(Jsoup.connect(url).get());
	}
	
	public WikiPacketReader(File file) throws IOException {
		packets = loadFromDocument(Jsoup.parse(file, null));
	}
	
	private Map<PacketType, WikiPacketInfo> loadFromDocument(Document doc) {
		Map<PacketType, WikiPacketInfo> result = new HashMap <>();
		Element bodyContent = doc.getElementById("mw-content-text");

		Element parserOutput = bodyContent.getElementsByClass("mw-parser-output").get(0);
		
		// Current protocol and sender
		Protocol protocol = null;
		Sender sender = null;
		
		for (Element element : parserOutput.children()) {
			String tag = element.tagName();
			System.out.println(element.nodeName());
			
			// Protocol candidate
			if (tag.equals("h2")) {
				try {
					String text = getEnumText(element.select(".mw-headline").first());
					System.out.println("h2 text=" + text);
					protocol = Protocol.valueOf(text);
					
				} catch (IllegalArgumentException e) {
					// We are in a section that is not a protocol
					protocol = null;
				}
			// Sender candidates
			} else if (tag.equals("h3")) {
				String text = getEnumText(element.select(".mw-headline").first());
				
				if ("SERVERBOUND".equals(text)) {
					sender = Sender.CLIENT;
				} else if ("CLIENTBOUND".equals(text)) {
					sender = Sender.SERVER;
				}
			// Table candidate
			} else if (protocol != null && sender != null && tag.equals("table")) {
				int columnPacketId = getPacketIDColumn(element);
				
				// We have a real packet table
				if (columnPacketId >= 0) {
					String string = element.select("td").get(columnPacketId).text().replace("0x", "").trim();
					if (string.contains(" ")) {
						string = string.split(" ")[1];
					}
					int packetId = Integer.parseInt(string, 16);

					try {
						// Hopefully this isn't an issue
						PacketType type = PacketType.findCurrent(protocol, sender, packetId);
						result.put(type, processTable(type, element));
					} catch (IllegalArgumentException ex) {
						System.err.println("Could not find PacketType[id=" + packetId + ", protocol=" + protocol + ", sender=" + sender + "]");
					}
				}
			}
		}
		return result;
	}
	
	private WikiPacketInfo processTable(PacketType type, Element table) {
		List<WikiPacketField> fields = new ArrayList <>();
		Elements rows = table.select("tr");
		
		// Skip the first row
		for (int i = 1; i < rows.size(); i++) {
			String[] data = getCells(rows.get(i), i == 1 ? 3 : 0, 3);
			fields.add(new WikiPacketField(data[0], data[1], data[2]));
		}
		// Save this
		return new WikiPacketInfo(type, fields);
	}

	private String[] getCells(Element row, int start, int count) {
		String[] result = new String[count];
		Elements columns = row.getElementsByTag("td");
		
		// Convert each cell to text
		for (int i = 0; i < count; i++) {
			// We'll ignore non-existant columns
			if (i + start < columns.size()) {
				result[i] = columns.get(i + start).text();
			}
		}
		return result;
	}

	/**
	 * Retrieve the column that contains the packet ID of a packet table.
	 * @param table - a table.
	 * @return The 0-based index of this column, or -1 if not found.
	 */
	private int getPacketIDColumn(Element table) {
		Elements headers = table.select("th");
		
		// Find the header with the packet ID
		for (int i = 0; i < headers.size(); i++) {
			final String text = getEnumText(headers.get(i));
			
			if ("PACKET_ID".equals(text))
				return i;
		}
		return -1;
	}
	
	/**
	 * Retrieve the upper-case enum version of the textual content of an element.
	 * @param element - the element.
	 * @return The textual content.
	 */
	private String getEnumText(Element element) {
		return element.text().trim().toUpperCase().replace(" ", "_");
	}
	
	public Collection<WikiPacketInfo> getCachedPackets() {
		return packets.values();
	}

	/**
	 * Attempt to retrieve information about a packet from its ID.
	 * @param type - the packet to retrieve.
	 * @return Information about this packet.
	 * @throws IOException If this packet cannot be found on the Wiki.
	 */
	public WikiPacketInfo readPacket(PacketType type) throws IOException {
		WikiPacketInfo result = packets.get(type);
		
		if (result != null)
			return result;
		else
			throw new IOException("Packet " + type + " cannot be found on the wiki.");
	}
}
