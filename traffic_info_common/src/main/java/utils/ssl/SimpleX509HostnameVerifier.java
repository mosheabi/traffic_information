package utils.ssl;

import org.apache.http.conn.ssl.X509HostnameVerifier;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.X509Certificate;

public class SimpleX509HostnameVerifier implements X509HostnameVerifier {

	@Override
	public boolean verify(String arg0, SSLSession arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void verify(String host, SSLSocket ssl) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void verify(String host, X509Certificate cert) throws SSLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void verify(String host, String[] cns, String[] subjectAlts)
			throws SSLException {
		// TODO Auto-generated method stub

	}

}
