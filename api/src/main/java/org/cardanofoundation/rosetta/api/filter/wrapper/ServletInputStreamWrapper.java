package org.cardanofoundation.rosetta.api.filter.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServletInputStreamWrapper extends ServletInputStream {

  private InputStream inputStream;


  public  ServletInputStreamWrapper(byte[] body) {


    this.inputStream = new ByteArrayInputStream(body);
  }

  @Override
  public boolean isFinished() {

    try {

      return inputStream.available() == 0;

    }catch(Exception e) {

      return false;
    }
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener listener) {


  }

  @Override
  public int read() throws IOException {
    return this.inputStream.read();
  }

}
