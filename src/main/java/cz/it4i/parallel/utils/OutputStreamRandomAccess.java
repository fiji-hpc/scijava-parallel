package cz.it4i.parallel.utils;

import com.google.common.io.CountingOutputStream;

import io.scif.io.IRandomAccess;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class OutputStreamRandomAccess implements IRandomAccess {

	private final DataOutputStream dos;
	private final CountingOutputStream cos;


	public OutputStreamRandomAccess(OutputStream os) {
		cos = new CountingOutputStream(os);
		dos = new DataOutputStream(cos);
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int skipBytes(int n) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean readBoolean() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte readByte() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public short readShort() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public char readChar() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readInt() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long readLong() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public float readFloat() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double readDouble() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(int b) throws IOException {
		dos.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		dos.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		dos.write(b, off, len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		dos.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		dos.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		dos.writeShort(v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		dos.writeChar(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		dos.writeInt(v);

	}

	@Override
	public void writeLong(long v) throws IOException {
		dos.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		dos.writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		dos.writeDouble(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		dos.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		dos.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		dos.writeUTF(s);
	}

	@Override
	public void close() throws IOException {
		cos.flush();
	}

	@Override
	public long getFilePointer() throws IOException {
		return cos.getCount();
	}

	@Override
	public long length() throws IOException {
		return cos.getCount();
	}

	@Override
	public ByteOrder getOrder() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOrder(ByteOrder order) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(byte[] b) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(ByteBuffer buffer) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(ByteBuffer buffer, int offset, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void seek(long pos) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(ByteBuffer buf) throws IOException {
		dos.write(buf.array());

	}

	@Override
	public void write(ByteBuffer buf, int off, int len) throws IOException {
		dos.write(buf.array(), off, len);

	}

}
