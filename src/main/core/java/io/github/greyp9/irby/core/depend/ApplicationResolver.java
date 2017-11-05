package io.github.greyp9.irby.core.depend;

import io.github.greyp9.arwo.core.codec.hex.HexCodec;
import io.github.greyp9.arwo.core.depend.Dependency;
import io.github.greyp9.arwo.core.depend.DependencyResolver;
import io.github.greyp9.arwo.core.hash.secure.HashU;
import io.github.greyp9.arwo.core.http.Http;
import io.github.greyp9.arwo.core.http.HttpRequest;
import io.github.greyp9.arwo.core.http.HttpResponse;
import io.github.greyp9.arwo.core.httpclient.HttpClient;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.value.NTV;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.irby.core.app.config.ApplicationConfig;
import io.github.greyp9.irby.core.http11.config.Http11Config;
import io.github.greyp9.irby.core.http11.config.Http11ConfigContext;
import io.github.greyp9.irby.core.http11.config.Http11ConfigServlet;
import io.github.greyp9.irby.core.https11.config.Https11Config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;

public class ApplicationResolver {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final ApplicationConfig appConfig;


    public ApplicationResolver(final ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void resolveDependencies() throws IOException {
        final Collection<Dependency> dependencies = new TreeSet<Dependency>();
        final DependencyResolver resolver = new DependencyResolver(
                "io/github/greyp9/irby/core/depend/dependencies.xml");
        for (final Http11Config config : appConfig.getHttp11Configs()) {
            for (Http11ConfigContext configContext : config.getContexts()) {
                for (Http11ConfigServlet configServlet : configContext.getServlets()) {
                    final String className = configServlet.getClassName();
                    dependencies.addAll(resolver.resolveDependencies(className));
                }
            }
        }
        for (final Https11Config config : appConfig.getHttps11Configs()) {
            for (Http11ConfigContext configContext : config.getContexts()) {
                for (Http11ConfigServlet configServlet : configContext.getServlets()) {
                    final String className = configServlet.getClassName();
                    dependencies.addAll(resolver.resolveDependencies(className));
                }
            }
        }
        logger.finer(String.format("DEPENDENCY COUNT=[%d]", dependencies.size()));
        for (final Dependency dependency : dependencies) {
            resolveDependency(dependency);
        }
    }

    public void resolveDependency(final Dependency dependency) throws IOException {
        // only proceed if local copy of resource not present
        final String src = dependency.getSrc();
        final String dest = dependency.getDest();
        final File file = new File(".", dest);
        logger.finer(String.format("LOOK FOR [%s]", file.getAbsolutePath()));
        if (!file.exists()) {
            logger.info(String.format("DOWNLOAD SOURCE=[%s]", src));
            final URL url = new URL(src);
            final NameTypeValues headersRequest = NTV.create();
            final HttpRequest httpRequest = new HttpRequest(
                    Http.Method.GET, url.getFile(), url.getQuery(), headersRequest, null);
            final HttpClient httpClient = new HttpClient();
            final HttpResponse httpResponse = httpClient.doRequest(url, httpRequest);
            final byte[] responseEntity = StreamU.read(httpResponse.getEntity());
            final String md5 = HexCodec.encode(HashU.md5(responseEntity));
            final String sha1 = HexCodec.encode(HashU.sha1(responseEntity));
            logger.info(String.format("DOWNLOAD MD5=[%s], SHA1=[%s]", md5, sha1));
            if ((dependency.getMd5().equals(md5)) && (dependency.getSha1().equals(sha1))) {
                StreamU.write(file, responseEntity);
            } else {
                logger.warning(String.format("EXPECT MD5=[%s], SHA1=[%s]", dependency.getMd5(), dependency.getSha1()));
            }
        }
    }
}
