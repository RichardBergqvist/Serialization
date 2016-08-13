package net.rb.tacitumcloud.serialization;

import static net.rb.tacitumcloud.serialization.SerializationUtils.*;

import java.util.ArrayList;
import java.util.List;

public class TCObject extends TCBase {
	public static final byte CONTAINER_TYPE = ContainerType.OBJECT;
	private short fieldCount;
	public List<TCField> fields = new ArrayList<TCField>();
	private short stringCount;
	public List<TCString> strings = new ArrayList<TCString>();
	private short arrayCount;
	public List<TCArray> arrays = new ArrayList<TCArray>();	
	
	private TCObject() {
		
	}
	
	public TCObject(String name) {
		size += 1 + 2 + 2 + 2;
		setName(name);
	}

	public void addField(TCField field) {
		fields.add(field);
		size += field.getSize();
		fieldCount = (short) fields.size();
	}
	
	public void addString(TCString string) {
		strings.add(string);
		size += string.getSize();
		stringCount = (short) strings.size();
	}

	public void addArray(TCArray array) {
		arrays.add(array);
		size += array.getSize();
		arrayCount = (short) arrays.size();
	}
	
	public int getSize() {
		return size;
	}
	
	public TCField findField(String name) {
		for (TCField field : fields) {
			if (field.getName().equals(name))
				return field;
		}
		return null;
	}
	
	public TCString findString(String name) {
		for (TCString string : strings) {
			if (string.getName().equals(name))
				return string;
		}
		return null;
	}
	
	public TCArray findArray(String name) {
		for (TCArray array : arrays) {
			if (array.getName().equals(name))
				return array;
		}
		return null;
	}
	
	public int getBytes(byte[] dest, int pointer) {
		pointer = writeBytes(dest, pointer, CONTAINER_TYPE);
		pointer = writeBytes(dest, pointer, nameLength);
		pointer = writeBytes(dest, pointer, name);
		pointer = writeBytes(dest, pointer, size);
		
		pointer = writeBytes(dest, pointer, fieldCount);
		for (TCField field : fields)
			pointer = field.getBytes(dest, pointer);
		
		pointer = writeBytes(dest, pointer, stringCount);
		for (TCString string : strings)
			pointer = string.getBytes(dest, pointer);
		
		pointer = writeBytes(dest, pointer, arrayCount);
		for (TCArray array : arrays)
			pointer = array.getBytes(dest, pointer);
		return pointer;
	}
	
	public static TCObject deserialize(byte[] data, int pointer) {
		byte containerType = data[pointer++];
		assert(containerType == CONTAINER_TYPE);
		
		TCObject result = new TCObject();
		result.nameLength = readShort(data, pointer);
		pointer += 2;
		result.name = readString(data, pointer, result.nameLength).getBytes();
		pointer += result.nameLength;
		
		result.size = readInt(data, pointer);
		pointer += 4;
		
		// Early-Out: pointer += result.size - sizeOffset - result.nameLength;
		
		result.fieldCount = readShort(data, pointer);
		pointer += 2;
		
		for (int i = 0; i < result.fieldCount; i++) {
			TCField field = TCField.deserialize(data, pointer);
			result.fields.add(field);
			pointer += field.getSize();
		}
		
		result.stringCount = readShort(data, pointer);
		pointer += 2;
		
		for (int i = 0; i < result.stringCount; i++) {
			TCString string = TCString.deserialize(data, pointer);
			result.strings.add(string);
			pointer += string.getSize();
		}
		
		result.arrayCount = readShort(data, pointer);
		pointer += 2;
		
		for (int i = 0; i < result.arrayCount; i++) {
			TCArray array = TCArray.deserialize(data, pointer);
			result.arrays.add(array);
			pointer += array.getSize();
		}
		return result;
	}
}