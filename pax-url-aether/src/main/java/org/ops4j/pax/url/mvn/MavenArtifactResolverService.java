package org.ops4j.pax.url.mvn;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public interface MavenArtifactResolverService {
	String URL_RESOLVER_PROTOCOL = "url.resolver.protocol" ;
	public File resolve( final URL url ) throws IOException ;
}
