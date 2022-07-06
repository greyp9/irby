package io.github.greyp9.irby.app.transform;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.app.AppFolder;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.value.NameTypeValues;
import io.github.greyp9.arwo.core.value.NameTypeValuesU;
import io.github.greyp9.arwo.core.xed.cursor.XedCursor;
import io.github.greyp9.arwo.core.xed.model.Xed;
import io.github.greyp9.arwo.core.xed.nav.XedNav;
import io.github.greyp9.arwo.core.xml.DocumentU;
import io.github.greyp9.arwo.core.xml.QNameU;
import io.github.greyp9.arwo.core.xsd.document.DocumentFactory;
import io.github.greyp9.arwo.core.xsd.instance.TypeInstance;
import io.github.greyp9.arwo.core.xsd.model.XsdTypes;
import io.github.greyp9.arwo.core.xsd.value.ValueInstance;
import io.github.greyp9.arwo.core.xslt.XsltX;
import io.github.greyp9.irby.core.app.Application;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

/**
 * Update application configuration file "conf/app.xml" with externally supplied parameters (port number).  Built in
 * XSLT is used to update configuration XML.
 */
public final class ConfigRealm {

    private ConfigRealm() {
    }

    public static void main(final String[] args) throws Exception {
        // query for configuration parameters to be updated
        final Console console = System.console();
        int i = 0;
        final String realmType = (args.length > ++i) ? args[i] : console.readLine(PROMPT_REALM);
        final String userName = (args.length > ++i) ? args[i] : console.readLine(PROMPT_NAME);
        final String userCredential = (args.length > ++i) ? args[i]
                : new String(console.readPassword(PROMPT_CREDENTIAL));
        // user feedback
        console.printf("Realm Type = %s.%n", realmType);
        console.printf("User Name = %s.%n", userName);
        // update configuration
        doAppXml(realmType, userName, userCredential);
        // on selection of "Basic" auth, generate a "realm.xml" (overwrite)
        if ("Basic".equals(realmType)) {
            doRealmXml(userName, userCredential);
        }
        console.printf("Configuration update successful.%n");
        System.exit(0);
    }

    /**
     * Update "conf/app.xml" with collected information.
     */
    private static void doAppXml(final String realmType,
                                 final String name, final String credential) throws IOException {
        // transform "app.xml"
        final URL urlXslt = ResourceU.resolve(XSLT_REALM);
        final byte[] bytesXslt = StreamU.read(urlXslt);
        final File fileXml = new File(Application.Const.FILE);
        final byte[] bytesXmlSource = StreamU.read(fileXml);
        // first pass enables the specified realm
        final XsltX xsltX1 = new XsltX(bytesXslt);
        xsltX1.setParameter("realmType", realmType);
        xsltX1.setParameter("credential", credential);
        xsltX1.setParameter("name", name);
        final byte[] bytesXmlTarget1 = xsltX1.transform(bytesXmlSource);
        // second pass updates realm attribute of webapps
        final XsltX xsltX2 = new XsltX(bytesXslt);
        final byte[] bytesXmlTarget2 = xsltX2.transform(bytesXmlTarget1);
        // persist "app.xml"
        StreamU.write(fileXml, bytesXmlTarget2);
    }

    /**
     * On Basic authentication, update "realm.xml" with collected information.
     */
    private static void doRealmXml(final String name, final String credential) throws IOException {
        // instantiate an empty document
        final XsdTypes xsdTypes = new XsdTypes(ResourceU.resolve(App.Realm.XSD));
        final QName qname = QNameU.getQName("{urn:arwo:realm}realm");
        final DocumentFactory documentFactory = new DocumentFactory(xsdTypes.getTypeDefinitions(), false);
        final Document document = documentFactory.generateEmpty(qname);
        final Xed xed = new Xed(document, xsdTypes);
        final XedCursor cursorRealm = new XedNav(xed).getRoot();
        // update realm element
        final TypeInstance instanceRealm = cursorRealm.getTypeInstance();
        final String salt = UUID.randomUUID().toString();
        final NameTypeValues ntvRealm = NameTypeValuesU.create(
                "realm.realmType.name", "Irby-Arwo",
                "realm.realmType.salt", salt);
        xed.update(cursorRealm.getElement(), ValueInstance.create(instanceRealm, ntvRealm));
        // create a principal record
        final TypeInstance instancePrincipals = instanceRealm.getInstance("principals");
        final TypeInstance instancePrincipal = instancePrincipals.getInstance("principal");
        final NameTypeValues ntvPrincipal = NameTypeValuesU.create(
                "principal.principalType.user", name,
                "principal.principalType.credential", credential,
                "principal.principalType.roles", "*");
        final ValueInstance valueInstance = ValueInstance.create(instancePrincipal, ntvPrincipal);
        // insert the principal record into the realm store
        final Element elementPrincipals = cursorRealm.getChild(cursorRealm.getChildInstance("principals"));
        xed.create(elementPrincipals, valueInstance);
        // persist "realm.xml"
        final File fileRealm = new File(AppFolder.getWebappRoot("arwo"), "root/realm.xml");
        StreamU.write(fileRealm, DocumentU.toXml(document));
    }

    private static final String PROMPT_REALM = "Enter realm type (Basic, Certificate): ";
    private static final String PROMPT_NAME = "Enter realm user name: ";
    private static final String PROMPT_CREDENTIAL = "Enter realm user credential: ";
    private static final String XSLT_REALM  = "io/github/greyp9/irby/xslt/config-realm.xslt";
}
