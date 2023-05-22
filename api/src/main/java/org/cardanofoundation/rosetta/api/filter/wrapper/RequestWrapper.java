package org.cardanofoundation.rosetta.api.filter.wrapper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import org.springframework.util.StreamUtils;

public class RequestWrapper extends HttpServletRequestWrapper {

  private byte[] body;

  public RequestWrapper(HttpServletRequest request) throws IOException {
    super(request);

    this.body = StreamUtils.copyToByteArray(request.getInputStream());
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new ServletInputStreamWrapper(this.body);

  }

}
