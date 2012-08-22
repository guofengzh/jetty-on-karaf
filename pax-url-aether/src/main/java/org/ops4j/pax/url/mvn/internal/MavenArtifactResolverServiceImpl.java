package org.ops4j.pax.url.mvn.internal ;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.ops4j.pax.url.maven.commons.MavenConfiguration;
import org.ops4j.pax.url.mvn.MavenArtifactResolverService;
import org.ops4j.pax.url.mvn.Parser;

public class MavenArtifactResolverServiceImpl implements MavenArtifactResolverService {

    private AetherBasedResolver2File m_aetherBasedResolver;
    
	public File resolve( final URL url ) throws IOException
	{
		Parser parser = new Parser( url.getPath() );
        return m_aetherBasedResolver.resolve( parser.getGroup(), parser.getArtifact(), parser.getClassifier(), parser.getType(), parser.getVersion() );
	}

	public void setupResolver( MavenConfiguration configuration) throws MalformedURLException {
		m_aetherBasedResolver = new AetherBasedResolver2File( configuration );
	}
}
