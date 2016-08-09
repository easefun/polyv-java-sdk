package net.polyv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.Args;

@NotThreadSafe
public class IStreamEntity extends AbstractHttpEntity {

	private final InputStream content;
	private final long filesize;
	private final long length;
	private Progress progress = null;
	private long offset = 0;

	public IStreamEntity(final InputStream instream, final long filesize,
			long offset, Progress progress) throws IOException {
		super();
		if (offset > 0) {
			instream.skip(offset);
		}
		this.content = Args.notNull(instream, "Source input stream");
		this.filesize = filesize;
		this.length = filesize - offset;
		this.offset = offset;
		this.progress = progress;
		setContentType("application/offset+octet-stream");
	}

	public boolean isRepeatable() {
		return true;
	}

	public long getContentLength() {
		return this.length;
	}

	public Header getContentType() {
		return new BasicHeader("Content-Type",
				"application/offset+octet-stream");
	}

	public InputStream getContent() throws IOException {
		return this.content;
	}

	public void writeTo(final OutputStream outstream) throws IOException {
		Args.notNull(outstream, "Output stream");
		final InputStream instream = this.content;
		try {
			final byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];
			int l;
			if (this.length < 0) {
				// consume until EOF
				while ((l = instream.read(buffer)) != -1) {
					this.offset += OUTPUT_BUFFER_SIZE;
					this.offset = Math.min(this.offset, this.filesize);
					if (this.progress != null) {
						this.progress.run(this.offset, this.filesize);
					}
					outstream.write(buffer, 0, l);
				}
			} else {
				// consume no more than length
				long remaining = this.length;
				while (remaining > 0) {
					l = instream.read(buffer, 0, (int) Math.min(
							OUTPUT_BUFFER_SIZE, remaining));
					if (l == -1) {
						break;
					}
					this.offset += OUTPUT_BUFFER_SIZE;
					this.offset = Math.min(this.offset, this.filesize);
					if (this.progress != null) {
						this.progress.run(this.offset, this.filesize);
					}
					outstream.write(buffer, 0, l);
					remaining -= l;
				}
			}
		} finally {
			instream.close();
		}
	}

	public boolean isStreaming() {
		return true;
	}

}
