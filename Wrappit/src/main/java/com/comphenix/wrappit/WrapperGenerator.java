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
package com.comphenix.wrappit;

import com.comphenix.protocol.PacketType;
import com.comphenix.wrappit.minecraft.CodePacketInfo;
import com.comphenix.wrappit.minecraft.CodePacketReader;
import com.comphenix.wrappit.utils.CaseFormating;
import com.comphenix.wrappit.utils.IndentBuilder;
import com.comphenix.wrappit.wiki.WikiPacketField;
import com.comphenix.wrappit.wiki.WikiPacketInfo;
import com.comphenix.wrappit.wiki.WikiPacketReader;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.RootCommandNode;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.WorldType;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.*;


public class WrapperGenerator {
	public enum Modifiers {
		ATTRIBUTE_COLLECTION_MODIFIER( List.class, "List<WrappedAttribute>", "getAttributeCollectionModifier()" ),
		BASE_COMPONENT_ARRAY( BaseComponent[].class, "BaseComponent[]", "getSpecificModifier(BaseComponent[].class)" ),
		BLOCK( Block.class, "Material", "getBlocks()" ),
		BLOCK_DATA( IBlockData.class, "WrappedBlockData", "getBlockData()" ),
		BLOCK_DATA_ARRAY( IBlockData[].class, "WrappedBlockData[]", "getBlockDataArrays()" ),
		BLOCK_POSITION( BlockPosition.class, "BlockPosition", "getBlockPositionModifier()" ),
		BLOCK_POSITION_COLLECTION_MODIFIER( List.class, "List<BlockPosition>", "getBlockPositionCollectionModifier()" ),
		BOOLEANS( boolean.class, "boolean", "getBooleans()" ),
		BYTE_ARRAYS( byte[].class, "byte[]", "getByteArrays()" ),
		BYTES( byte.class, "byte", "getBytes()" ),
		CHUNK_COORD_INT_PAIR( ChunkCoordIntPair.class, "ChunkCoordIntPair", "getChunkCoordIntPairs()" ),
		COLLECTION( Collection.class, "Collection", "getSpecificModifier(Collection.class)" ),
		COMPONENT( IChatBaseComponent.class, "WrappedChatComponent", "getChatComponents()" ),
		COMPONENT_ARRAY( IChatBaseComponent[].class, "WrappedChatComponent[]", "getChatComponentArrays()" ),
		DATA_WATCHER_MODIFIER( DataWatcher.class, "WrappedDataWatcher", "getDataWatcherModifier()" ),
		DIFFICULTIES( EnumDifficulty.class, "Difficulty", "getDifficulties()" ),
		DIMENSION( IRegistryCustom.Dimension.class, "int", "getDimensions()" ),
		DOUBLES( double.class, "double", "getDoubles()" ),
		ENTITY_TYPE_MODIFIER( EntityTypes.class, "EntityType", "getEntityTypeModifier()" ),
		ENUMS( Enum.class, "Enum<?>", "getSpecificModifier(Enum.class)" ),
		ENUM_HAND( EnumHand.class, "Hand", "getHands()" ),
		FLOATS( float.class, "float", "getFloat()" ),
		GAME_PROFILE( GameProfile.class, "WrappedGameProfile", "getGameProfiles()" ),
		GAME_STATE_CHANGE( PacketPlayOutGameStateChange.a.class, "int", "getGameStateIDs()" ),
		INTEGER_ARRAYS( int[].class, "int[]", "getIntegerArrays()" ),
		INTEGERS( int.class, "int", "getIntegers()" ),
		ITEM_LIST_MODIFIER( List.class, "List<ItemStack>", "getItemListModifier()" ),
		ITEM_ARRAY_MODIFIER( ItemStack[].class, "ItemStack[]", "getItemArrayModifier()" ),
		ITEM_MODIFIER( ItemStack.class, "ItemStack", "getItemModifier()" ),
		LONGS( long.class, "long", "getLongs()" ),
		MAP( Map.class, "Map<?,?>", "getSpecificModifier(Map.class)" ),
		MERCHANT_RECIPE_LIST( MerchantRecipeList.class, "List<MerchantRecipe>", "getMerchantRecipeLists()" ),
		MINECRAFT_KEY( MinecraftKey.class, "MinecraftKey", "getMinecraftKeys()" ),
		MOB_EFFECT_TYPES( MobEffectList.class, "PotionEffectType", "getEffectTypes()" ),
		MOVING_OBJECT_POSITION_BLOCK( MovingObjectPositionBlock.class, "MovingObjectPositionBlock", "getMovingBlockPositions()" ),
		MULTI_BLOCK_CHANGE_INFO_ARRAY( SectionPosition[].class, "MultiBlockChangeInfo[]", "getMultiBlockChangeInfoArrays()" ),
		NBT_LIST_MODIFIER( List.class, "List<NbtBase<?>>", "getListNbtModifier()" ),
		NBT_MODIFIER( NBTTagCompound.class, "NbtBase<?>", "getNbtModifier()" ),
		PARTICLE_PARAM( ParticleParam.class, "WrappedParticle", "getNewParticles()" ),
		PLAYER_INFO_DATA_LIST( List.class, "List<PlayerInfoData>", "getPlayerInfoDataLists()" ),
		POSITION_COLLECTION_MODIFIER( List.class, "List<ChunkPosition>", "getPositionCollectionModifier()" ),
		POSITION_LIST( List.class, "List<BlockPosition>", "getBlockPositionCollectionModifier()" ),
		SET( Set.class, "Set<?>", "getSpecificModifier(Set.class)" ),
		PUBLIC_KEY_MODIFIER( PublicKey.class, "PublicKey", "getSpecificModifier(PublicKey.class)" ),
		RESOURCE_KEY( ResourceKey.class, "World", "getWorldKeys()" ),
		ROOT_COMMAND_NODE( RootCommandNode.class, "RootCommandNode<?>", "getSpecificModifier(RootCommandNode.class)" ), // should be safe to use
		SERVER_PING( ServerPing.class, "WrappedServerPing", "getServerPings()" ),
		SHORT_ARRAYS( short[].class, "short[]", "getShortArrays()" ),
		SHORTS( short.class, "short", "getShorts()" ),
		SOUND_EFFECT( SoundEffect.class, "Sound", "getSoundEffects()" ),
		SOUND_CATEGORY( SoundCategory.class, "SoundCategory", "getSoundCategories()" ),
		SUGGESTIONS( Suggestions.class, "Suggestions", "getSpecificModifier(Suggestions.class)" ), // should be safe to use
		SECTION_POSITION( SectionPosition.class, "BlockPosition", "getSectionPositions()" ),
		STATISTIC_MAP( Map.class, "Map<WrappedStatistic, Integer>", "getStatisticMaps()" ),
		STRING_ARRAYS( String[].class, "String[]", "getStringArrays()" ),
		STRINGS( String.class, "String", "getStrings()" ),
		TITLE_ACTION( PacketPlayOutTitle.EnumTitleAction.class, "EnumWrappers.TitleAction", "getTitleActions()" ),
		UUID( UUID.class, "UUID", "getUUIDs" ),
		VEC3D( Vec3D.class, "Vector", "getVectors()" ),
		WATCHABLE_COLLECTION_MODIFIER( List.class, "List<WrappedWatchableObject>", "getWatchableCollectionModifier()" ),
		WORLD_BORDER_ACTION( PacketPlayOutWorldBorder.EnumWorldBorderAction.class, "EnumWrappers.WorldBorderAction", "getWorldBorderActions()" ),
		WORLD_TYPE_MODIFIER( WorldType.class, "WorldType", "getWorldTypeModifier()" );

		private static Map<Class<?>, Modifiers> inputLookup;

		static {
			inputLookup = new HashMap<>();

			for (Modifiers modifier : values()) {
				inputLookup.put(modifier.inputType, modifier);
			}
		}

		public static Modifiers getByInputType(Class<?> inputType) {
			for (; inputType != null && !inputType.equals(Object.class); inputType = inputType.getSuperclass()) {
				Modifiers mod = inputLookup.get(inputType);

				if (mod != null)
					return mod;
			}

			// Unable to find modifier
			return null;
		}

		private final Class<?> inputType;
		private final String outputType;
		private final String name;

		Modifiers(Class<?> inputType, String outputType, String name) {
			this.inputType = inputType;
			this.outputType = outputType;
			this.name = name;
		}

		public Class<?> getInputType() {
			return inputType;
		}

		public String getMethodName() {
			return name;
		}

		public String getOutputType() {
			return outputType;
		}

		public boolean isWrapper() {
			return switch ( this ) {
				case BLOCK, BLOCK_POSITION, COMPONENT, CHUNK_COORD_INT_PAIR, COMPONENT_ARRAY, DATA_WATCHER_MODIFIER, GAME_PROFILE, POSITION_LIST, SERVER_PING -> true;
				default -> false;
			};
		}
	}

	private static final String NEWLN = System.getProperty("line.separator");

	private static final String[] HEADER = {
		"/*",
		" * This file is part of PacketWrapper.",
		" * Copyright (C) 2012-2015 Kristian S. Strangeland",
		" * Copyright (C) 2015 dmulloy2",
		" *",
		" * PacketWrapper is free software: you can redistribute it and/or modify",
		" * it under the terms of the GNU Lesser General Public License as published by",
		" * the Free Software Foundation, either version 3 of the License, or",
		" * (at your option) any later version.",
		" *",
		" * PacketWrapper is distributed in the hope that it will be useful,",
		" * but WITHOUT ANY WARRANTY; without even the implied warranty of",
		" * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the",
		" * GNU General Public License for more details.",
		" *",
		" * You should have received a copy of the GNU Lesser General Public License",
		" * along with PacketWrapper.  If not, see <https://www.gnu.org/licenses/>.",
		" */"
	};

	private final CodePacketReader codeReader;

	private final Set<String> ignoreArray = new HashSet <>( Arrays.asList( "array", "of" ) );
	private final WikiPacketReader wikiReader;

	public WrapperGenerator(CodePacketReader codeReader, WikiPacketReader wikiReader) {
		this.codeReader = codeReader;
		this.wikiReader = wikiReader;
	}

	public String generateClass(PacketType type) throws IOException {
		StringBuilder builder = new StringBuilder();
		IndentBuilder indent = new IndentBuilder(builder, 1);

		CodePacketInfo codeInfo = codeReader.readPacket(type);
		WikiPacketInfo wikiInfo = wikiReader.readPacket(type);

		// Java style
		String className = "Wrapper" + Wrappit.getCamelCase(type.getProtocol()) + Wrappit.getCamelCase(type.getSender())
				+ Wrappit.getCamelCase(type.name());

		// Current field index
		int fieldIndex = 0;

		for (String header : HEADER) {
			builder.append( header ).append( NEWLN );
		}

		builder.append("package com.comphenix.packetwrapper;").append(NEWLN).append(NEWLN);
		builder.append("import com.comphenix.protocol.PacketType;").append(NEWLN);
		builder.append("import com.comphenix.protocol.events.PacketContainer;").append(NEWLN).append(NEWLN);
		builder.append("public class ").append(className).append(" extends AbstractPacket {").append(NEWLN)
				.append(NEWLN);

		indent.appendLine("public static final PacketType TYPE = " + getReference(type) + ";");
		indent.appendLine("");

		// Default constructors
		indent.appendLine("public " + className + "() {");
		indent.incrementIndent().appendLine("super(new PacketContainer(TYPE), TYPE);").appendLine("handle.getModifier().writeDefaults();");
		indent.appendLine("}" + NEWLN);

		// And the wrapped packet constructor
		indent.appendLine("public " + className + "(PacketContainer packet) {");
		indent.incrementIndent().appendLine("super(packet, TYPE);");
		indent.appendLine("}" + NEWLN);

		for (WikiPacketField field : wikiInfo.getPacketFields()) {
			if (fieldIndex < codeInfo.getNetworkOrder().size()) {
				Field codeField = codeInfo.getNetworkOrder().get(fieldIndex);
				Modifiers modifier = Modifiers.getByInputType(codeField.getType());

				if (modifier == null) {
					indent.appendLine("// Cannot find type for " + codeField.getName());
					System.err.println("Cannot find type " + codeField.getType() + " for field " + codeField.getName() + " in " + type.toString());
					continue;
				}

				try {
					writeGetMethod(indent, fieldIndex, modifier, codeInfo, field);
				} catch (Throwable ex) {
					indent.appendLine("// Cannot generate getter " + codeField.getName());
					System.err.println("Failed to generate getter " + codeField.getName() + " in " + type.toString());
					ex.printStackTrace();
				}

				try {
					writeSetMethod(indent, fieldIndex, modifier, codeInfo, field);
				} catch (Throwable ex) {
					indent.appendLine("// Cannot generate setter " + codeField.getName());
					System.err.println("Failed to generate setter " + codeField.getName() + " in " + type.toString());
					ex.printStackTrace();
				}
			} else {
				indent.appendLine("// Cannot generate field " + field.getFieldName());
			}

			fieldIndex++;
		}

		builder.append("}");
		return builder.toString();
	}

	private String getFieldName(WikiPacketField field) {
		String converted = CaseFormating.toCamelCase(field.getFieldName());
		return converted.replace("Eid", "EntityID")
				.replace("EntityId", "EntityID")
				.replace("JsonData", "Message");
	}

	private String getFieldType(WikiPacketField field) {
		// Most are primitive
		String type = field.getFieldType().toLowerCase();

		// Better names
		type = type.replace("string", "String")
				.replace("slot", "ItemStack")
				.replace("metadata", "WrappedDataWatcher")
				.replace("unsigned", "")
				.replace("optional", "")
				.replace( "varint enum", "enum" )
				.replace("varint", "int")
				.replace("bool", "boolean")
				.replace("Boolean", "boolean")
				.replace("uuid", "UUID")
				.replace( "inputitem1", "ItemStack" )
				.replace( "inputitem2", "ItemStack" )
				.replace(" ", "");

		// Detect arrays
		if (type.contains("array")) {
			return (getLongestWord(type.split("\\s+"), ignoreArray).replace("array", "") + "[]").replace("of", "");
		} else {
			return type;
		}
	}

	private String getLongestWord(String[] input, Set<String> blacklist) {
		int selected = 0;

		// Find a longer word that is not on the black list
		for (int i = 1; i < input.length; i++) {
			if (!blacklist.contains(input[i]) && input[i].length() > input[selected].length()) {
				selected = i;
			}
		}
		return input[selected];
	}

	private String getModifierCall(int fieldIndex, String callFormat, CodePacketInfo codeInfo) {
		Field field = codeInfo.getNetworkOrder().get(fieldIndex);
		int memoryIndex = 0;

		// Find the correct index
		for (Field compare : codeInfo.getMemoryOrder()) {
			if (compare.getType().equals(field.getType())) {
				if (field.equals(compare))
					break;
				else
					memoryIndex++;
			}
		}

		// The modifier we will use
		Modifiers modifier = Modifiers.getByInputType(field.getType());
		String method = modifier != null ? modifier.getMethodName() : "UNKNOWN()";

		return method + String.format(callFormat, memoryIndex);
	}

	private String getReference(PacketType type) {
		return "PacketType." + Wrappit.getCamelCase(type.getProtocol()) + "." + Wrappit.getCamelCase(type.getSender()) + "." + type.name();
	}

	private void writeGetMethod(IndentBuilder indent, int fieldIndex, Modifiers modifier, CodePacketInfo codeInfo, WikiPacketField field)
			throws IOException {

		if ( field.getFieldName() == null ) {
			System.err.println( "Undocumented field in wiki -> packet " + codeInfo.getType() + " - info required for " + modifier.getInputType() + " (field: " + fieldIndex + ")" );
			return;
		}

		String name = getFieldName(field);
		String outputType = getFieldType(field);
		String casting = "";

		// Simple attempt at casting
		if (modifier.isWrapper()) {
			outputType = modifier.getOutputType();
		} else if (!modifier.getOutputType().equals(outputType)) {
			casting = " (" + outputType + ")";
		}

		// Pattern I noticed fixing wrappers
		if ((modifier.getOutputType().equalsIgnoreCase("int") || modifier.getOutputType().equalsIgnoreCase("float"))
				&& (outputType.equalsIgnoreCase("byte") || outputType.equalsIgnoreCase("short"))) {
			outputType = modifier.getOutputType();
			casting = "";
		}

		String note = CaseFormating.toLowerCaseRange(field.getNotes(), 0, 1).trim();

		// Comment
		indent.appendLine("/**");
		indent.appendLine(" * Retrieve " + field.getFieldName() + ".");
		if (!note.isEmpty()) {
			indent.appendLine(" * <p>");
			indent.appendLine(" * Notes: " + note);
		}
		indent.appendLine(" * @return The current " + field.getFieldName());
		indent.appendLine(" */");

		indent.appendLine("public " + outputType + " get" + name + "() {");
		indent.incrementIndent().appendLine("return" + casting + " handle." + getModifierCall(fieldIndex, ".read(%s);", codeInfo));
		indent.appendLine("}" + NEWLN);

		// Generate getEntity methods
		if (name.toLowerCase().contains("entityid")) {
			writeGetEntityMethods(indent, fieldIndex);
		}
	}

	private static final List<String> GET_ENTITY_LINES = Arrays.asList(
			"/**",
			" * Retrieve the entity involved in this event.",
			" * @param world - the current world of the entity.",
			" * @return The involved entity.",
			" */",
			"public Entity getEntity(World world) {",
			"    return handle.getEntityModifier(world).read(%s);",
			"}" + NEWLN,

			"/**",
			" * Retrieve the entity involved in this event.",
			" * @param event - the packet event.",
			" * @return The involved entity.",
			" */",
			"public Entity getEntity(PacketEvent event) {",
			"    return getEntity(event.getPlayer().getWorld());",
			"}" + NEWLN
	);

	private void writeGetEntityMethods(IndentBuilder indent, int fieldIndex) throws IOException {
		for (String line : GET_ENTITY_LINES) {
			indent.appendLine(String.format(line, fieldIndex));
		}
	}

	private void writeSetMethod(IndentBuilder indent, int fieldIndex, Modifiers modifier, CodePacketInfo codeInfo, WikiPacketField field)
			throws IOException {
		if ( field.getFieldName() == null ) {
			System.err.println( "Undocumented field in wiki -> packet " + codeInfo.getType() + " - info required for " + modifier.getInputType() + " (field: " + fieldIndex + ")" );
			return;
		}

		String name = getFieldName(field);
		String inputType = getFieldType(field);
		String casting = "";

		if (modifier.isWrapper()) {
			inputType = modifier.getOutputType();
		} else if (!modifier.getOutputType().equals(inputType)) {
			casting = " (" + modifier.getOutputType() + ")";
		}

		// Pattern I noticed fixing wrappers
		if ((modifier.getOutputType().equalsIgnoreCase("int") || modifier.getOutputType().equalsIgnoreCase("float"))
				&& (inputType.equalsIgnoreCase("byte") || inputType.equalsIgnoreCase("short"))) {
			inputType = modifier.getOutputType();
			casting = "";
		}

		// String note = CaseFormating.toLowerCaseRange(field.getNotes(), 0, 1).trim();

		// Comment
		indent.appendLine("/**");
		indent.appendLine(" * Set " + field.getFieldName() + ".");
		indent.appendLine(" * @param value - new value.");
		indent.appendLine(" */");

		indent.appendLine("public void set" + name + "(" + inputType + " value) {");
		indent.incrementIndent().appendLine("handle." + getModifierCall(fieldIndex, ".write(%s," + casting + " value);", codeInfo));
		indent.appendLine("}\n");
	}
}
