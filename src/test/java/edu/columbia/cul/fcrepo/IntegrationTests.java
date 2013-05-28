package edu.columbia.cul.fcrepo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.fcrepo.server.Context;
import org.fcrepo.server.Module;
import org.fcrepo.server.PackageProxy;
import org.fcrepo.server.Server;
import org.fcrepo.server.access.Access;
import org.fcrepo.server.access.DefaultAccess;
import org.fcrepo.server.access.RepositoryInfo;
import org.fcrepo.server.config.Parameter;
import org.fcrepo.server.errors.InitializationException;
import org.fcrepo.server.errors.ModuleInitializationException;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.errors.ServerInitializationException;
import org.fcrepo.server.management.ManagementModule;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericApplicationContext;

import edu.columbia.cul.sword.DepositHandler;
import edu.columbia.cul.sword.SWORDException;
import edu.columbia.cul.sword.SWORDResource;
import edu.columbia.cul.sword.impl.DepositRequest;
import edu.columbia.cul.sword.xml.entry.Entry;
import edu.columbia.cul.sword.xml.service.ServiceDocument;
import edu.columbia.cul.sword.xml.entry.Link;

public class IntegrationTests {
	
	private Namespace ATOM = Namespace.getNamespace("atom","http://www.w3.org/2005/Atom");
	private static Namespace SWORD = Namespace.getNamespace("sword","http://purl.org/net/sword/");
	
	private static String EMPTY_MD5 = "d41d8cd98f00b204e9800998ecf8427e";
	
    private String depositURI;
    
    private String limitedDepositURI;
    
    private SWORDResource test;
    
    private UriInfo mockUriInfo;
    
    private ServletContext mockContext;
    
    @Before
    public void setUp()
    		throws NoSuchFieldException,
    		SecurityException, IllegalArgumentException,
    		IllegalAccessException, SWORDException,
    		JAXBException, BeansException, ServerException {
    	mockUriInfo = mock(UriInfo.class);
    	depositURI = "http://fedora.info/deposit";
    	limitedDepositURI = depositURI + "/limited";
    	Server server = mock(Server.class);
    	final DOManager mgmt = mock(DOManager.class);
    	final DefaultAccess access = mock(DefaultAccess.class);
    	RepositoryInfo mockInfo = mock(RepositoryInfo.class);
    	mockInfo.repositoryName = "mockRepo";
    	mockInfo.repositoryVersion = "0.0";
    	when(access.describeRepository(any(Context.class))).thenReturn(mockInfo);
    	// getModule is final, so we have to mock the context
    	GenericApplicationContext context = mock(GenericApplicationContext.class);
    	when(server.getBean(DOManager.class)).thenReturn(mgmt);
    	DOReader mockReader = getMockReader("test");
    	when(mgmt.getReader(eq(false), any(Context.class), eq("test"))).thenReturn(mockReader);
    	when(mgmt.objectExists("test")).thenReturn(true);
    	when(server.getBean("org.fcrepo.server.access.Access")).thenReturn(access);
    	// getParameter is final, so we have to actually give it some values
    	List<Parameter> parameters = new ArrayList<Parameter>(1);
    	parameters.add(new Parameter("fedoraServerHost", "localhost", false, null, null));
    	PackageProxy.setApplicationContext(server, context);
    	PackageProxy.setParameters(server, parameters);
        
    	test = new SWORDResource(server);
    	DepositHandler handler = mock(DepositHandler.class);
    	when(handler.handles(anyString(), anyString())).thenReturn(true);
    	Map<String, DepositHandler> handlers = new HashMap<String, DepositHandler>(1);
    	handlers.put("test", handler);

    	when(handler.ingestDeposit(any(DepositRequest.class), any(Context.class))).thenReturn(getMockEntry());
    	test.setDepositHandlers(handlers);
    	mockContext = mock(ServletContext.class);
    	when(mockContext.getInitParameter("maxUploadSize")).thenReturn(Integer.toString(1024*1024));
    	when(mockContext.getInitParameter("authentication-method")).thenReturn("None");
    	test.setServletContext(mockContext);
    	String [] collections = new String[]{"test"};
    	test.setCollectionPids(Arrays.asList(collections));
    }
    
    @After
    public void tearDown(){
    	
    }
    
    @Test
    public void testWrongPackaging() throws JDOMException, IOException, InitializationException{
    	HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/mets/dspace");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
    	
    	Response response = test.postDeposit("deposit", mockContext, mockUriInfo);
    	String entity = response.getEntity().toString();
    	System.err.println(entity);
    	assertEquals("Unexpected response status from deposit with bad packaging", 415, response.getStatus());
    	
    	SAXBuilder tBuilder = new SAXBuilder();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));
    	
    	assertEquals("Wrong root element. ", tEntityDoc.getRootElement().getName(), "error");
    	assertEquals("Wrong error URI given ", tEntityDoc.getRootElement().getAttributeValue("href"), "http://purl.org/net/sword/error/ErrorContent");
    	assertNotNull("Missing title" , tEntityDoc.getRootElement().getChild("title", ATOM));
    	assertNotNull("Missing summary" , tEntityDoc.getRootElement().getChild("summary", ATOM));
    	assertNotNull("Missing updated" , tEntityDoc.getRootElement().getChild("updated", ATOM));
    	assertNotNull("Missing userAgent" , tEntityDoc.getRootElement().getChild("userAgent", SWORD));
    }
    
    @Test
    public void testCorrectPackaging() throws JDOMException, IOException, InitializationException {
    	HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();

    	Response response = test.postDeposit("deposit", mockContext, mockUriInfo);
    	assertEquals("Unexpected response status from deposit with good packaging", 201, response.getStatus());

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));
    	
    	assertNotNull("Missing content element", tEntityDoc.getRootElement().getChild("content", ATOM));
    	Element tContent = tEntityDoc.getRootElement().getChild("content", ATOM);
    	assertEquals("Invalid content type", tContent.getAttributeValue("type"), "application/zip");
    	assertNotNull("Missing packaging element", tEntityDoc.getRootElement().getChild("packaging", SWORD));
    	Element tPackaging = tEntityDoc.getRootElement().getChild("packaging", SWORD);
    	assertEquals("Invalid content type", tPackaging.getText(), "http://purl.org/net/sword-types/METSDSpaceSIP");
    }
	@Test
	public void testWrongContentType() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/TOTALY_UNSUPPORTED");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/mets/dspace");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
    	
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		
		int tStatus = response.getStatus();	

		assertEquals("Should have thrown a 415 error", 415, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertEquals("Wrong root element. ", tEntityDoc.getRootElement().getName(), "error");
		assertEquals("Wrong error URI given ", tEntityDoc.getRootElement().getAttributeValue("href"), "http://purl.org/net/sword/error/ErrorContent");

		assertNotNull("Missing title" , tEntityDoc.getRootElement().getChild("title", ATOM));
		assertNotNull("Missing summary" , tEntityDoc.getRootElement().getChild("summary", ATOM));
		assertNotNull("Missing updated" , tEntityDoc.getRootElement().getChild("updated", ATOM));
		assertNotNull("Missing userAgent" , tEntityDoc.getRootElement().getChild("userAgent", SWORD));

	}

	@Test
	public void testNonMediatedAuthor() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
    	
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertNotNull("Missing author element", tEntityDoc.getRootElement().getChild("author", ATOM));
		assertNotNull("Missing name element", tEntityDoc.getRootElement().getChild("author", ATOM).getChild("name", ATOM));
		Element tAuthorName = tEntityDoc.getRootElement().getChild("author", ATOM).getChild("name", ATOM);
		assertEquals("Author name incorrect", tAuthorName.getText(), "sword");
	}

	@Test
	public void testMediatedAuthor() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getHeader("X-On-Behalf-Of")).thenReturn("Glen");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertNotNull("Missing author element", tEntityDoc.getRootElement().getChild("author", ATOM));
		assertNotNull("Missing name element", tEntityDoc.getRootElement().getChild("author", ATOM).getChild("name", ATOM));
		Element tAuthorName = tEntityDoc.getRootElement().getChild("author", ATOM).getChild("name", ATOM);
		assertEquals("Author name incorrect", tAuthorName.getText(), "sword");

		assertNotNull("Missing contributor element", tEntityDoc.getRootElement().getChild("contributor", ATOM));
		assertNotNull("Missing name element", tEntityDoc.getRootElement().getChild("contributor", ATOM).getChild("name", ATOM));
		Element tContributerName = tEntityDoc.getRootElement().getChild("contributor", ATOM).getChild("name", ATOM);
		assertEquals("Contributer name incorrect", tContributerName.getText(), "Glen");
	}

	@Test
	public void testUnkownMediatedAuthor() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getHeader("X-On-Behalf-Of")).thenReturn("THIS_USER_SHOULD_NOT_EXIST");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 401 result", 401, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertEquals("Wrong root element. ", tEntityDoc.getRootElement().getName(), "error");
		assertEquals("Wrong error URI given ", tEntityDoc.getRootElement().getAttributeValue("href"), "http://purl.org/net/sword/error/TargetOwnerUnknown");

		assertNotNull("Missing title" , tEntityDoc.getRootElement().getChild("title", ATOM));
		assertNotNull("Missing summary" , tEntityDoc.getRootElement().getChild("summary", ATOM));
		assertNotNull("Missing updated" , tEntityDoc.getRootElement().getChild("updated", ATOM));
		assertNotNull("Missing userAgent" , tEntityDoc.getRootElement().getChild("userAgent", SWORD));
	}

	@Test
	public void testVerbose() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getHeader("X-Verbose")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertNotNull("Missing verbose element", tEntityDoc.getRootElement().getChild("verboseDescription", SWORD));
	}
	
	@Test
	public void testNoVerbose() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getHeader("X-Verbose")).thenReturn("false");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertNull("Verbose element present where should be removed", tEntityDoc.getRootElement().getChild("verboseDescription", SWORD));
	}

	@Test
	public void testNoOp() throws IOException, JDOMException, InitializationException {


		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("image/jpg");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getHeader("X-Verbose")).thenReturn("false");
    	when(req.getHeader("Content-MD5")).thenReturn("4862271b2bf8a358e6854e78c2847743");
    	when(req.getInputStream()).thenReturn(getMockStream());
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		Element tContent = tEntityDoc.getRootElement().getChild("content", ATOM);
		assertNotNull("Missing content element", tContent);

		//GetMethod tMethod = new GetMethod(tContent.getAttributeValue("src"));

		//tStatus = this.getClient().executeMethod(tMethod);

		//assertEquals("No op set so should return 404 from " + tContent.getAttributeValue("src") + ". This will always fail on fedora 2.x because it doesn't throw the correct errors", 404, tStatus);
	}

	@Test
	public void testOp() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("image/jpg");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-Verbose")).thenReturn("false");
    	when(req.getHeader("Content-MD5")).thenReturn("4862271b2bf8a358e6854e78c2847743");
    	when(req.getInputStream()).thenReturn(getMockStream());
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		Element tContent = tEntityDoc.getRootElement().getChild("content", ATOM);
		assertNotNull("Missing content element", tContent);

		//GetMethod tMethod = new GetMethod(tContent.getAttributeValue("src"));

		//tStatus = this.getClient().executeMethod(tMethod);

		//assertEquals("No op isn't set so should return 200", 200, tStatus);
	}


	@Test
	public void testUserAgent() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertNotNull("Missing user agent element", tEntityDoc.getRootElement().getChild("userAgent", SWORD));
		
		assertNotNull("Missing server user agent element", tEntityDoc.getRootElement().getChild("generator", ATOM));
	}

	@Test
	public void testLocation() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		String tLocation = response.getMetadata().getFirst("Location").toString();
		assertNotNull("Missing Location in header", tLocation);
		assertNotNull("Missing link element", tEntityDoc.getRootElement().getChild("link", ATOM));

		Element tEditLink = tEntityDoc.getRootElement().getChild("link", ATOM);
		assertEquals("Location header doesn't match link href", tEditLink.getAttributeValue("href"), tLocation);
	}
		
	/**
	 *  Test section 1.3 Package description entry document of SWORD 1.3 Spec
	 * @throws InitializationException 
	 */
	@Test
	public void testContentType() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		assertNotNull("Missing Content type in header", response.getMetadata().getFirst("Content-Type"));
		Element tContent = tEntityDoc.getRootElement().getChild("content", ATOM);
		assertNotNull("Missing content element", tContent);

		assertEquals("Content header not equal to what was submitted", tContent.getAttributeValue("type"), "application/zip");
	}

	@Test
	public void testGetEntry() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("application/zip");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	when(req.getHeader("Content-MD5")).thenReturn(EMPTY_MD5);
    	when(req.getInputStream()).thenReturn(getEmptyMockStream());
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		String tLocation = response.getMetadata().getFirst("Location").toString();
		assertNotNull("Missing Location in header", tLocation);
		String [] parts = tLocation.split("/");
		
		response = test.getDepositEntry(parts[parts.length - 2], parts[parts.length - 1], mockContext);
		
		tStatus = response.getStatus();
		assertEquals("Get returned a non 200 result", 200, tStatus);

		Document tSavedEntityDoc = tBuilder.build(response.getEntity().toString());
		for (int i = 0; i < tSavedEntityDoc.getRootElement().getChildren().size(); i++) {
			Element tCurEl = (Element)tSavedEntityDoc.getRootElement().getChildren().get(i);
			assertEquals("Elements '" + tCurEl.getName() + "' are not equal. ", tCurEl.getText(), tEntityDoc.getRootElement().getChild(tCurEl.getName(), tCurEl.getNamespace()).getText());
		}
	}

	@Test
	public void testSuggestedFilename() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Disposition")).thenReturn("logo.jpg");
    	when(req.getHeader("Content-Type")).thenReturn("image/jpg");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getHeader("Content-MD5")).thenReturn("4862271b2bf8a358e6854e78c2847743");
    	when(req.getInputStream()).thenReturn(getMockStream());
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		Element tContent = tEntityDoc.getRootElement().getChild("content", ATOM);
		assertNotNull("Missing content element", tContent);

		String[] tURI = tContent.getAttributeValue("src").split("/");
		assertEquals("Content disposition ignored", "logo", tURI[tURI.length - 1]);
	}

	@Test
	public void testRequiredFieldsInEntryDoc() throws IOException, JDOMException, InitializationException {

		HttpServletRequest req= mock(HttpServletRequest.class);
    	when(req.getHeader("Content-Type")).thenReturn("image/jpg");
    	when(req.getHeader("X-Packaging")).thenReturn("http://purl.org/net/sword-types/METSDSpaceSIP");
    	when(req.getHeader("X-No-Op")).thenReturn("true");
    	when(req.getHeader("X-On-Behalf")).thenReturn("gmr");
    	when(req.getHeader("X-Verbose")).thenReturn("true");
    	when(req.getHeader("Content-MD5")).thenReturn("4862271b2bf8a358e6854e78c2847743");
    	when(req.getInputStream()).thenReturn(getMockStream());
    	when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/fedora/"));
    	test.setServletRequest(req);
    	test.init();
		
    	Response response = test.postDeposit("test", mockContext, mockUriInfo);
		
		int tStatus = response.getStatus();	

		assertEquals("Post returned a non 201 result", 201, tStatus);

    	SAXBuilder tBuilder = new SAXBuilder();
    	String entity = response.getEntity().toString();
    	Document tEntityDoc = tBuilder.build(new StringReader(entity));

		Element tContributor = tEntityDoc.getRootElement().getChild("contributor", ATOM);
		assertNotNull("Missing contributor element", tContributor);

		Element tGenerator = tEntityDoc.getRootElement().getChild("generator", ATOM);
		assertNotNull("Missing generator element", tGenerator);

		Element tUserAgent = tEntityDoc.getRootElement().getChild("userAgent", SWORD);
		assertNotNull("Missing user agent element", tUserAgent);

		Element tTreatement = tEntityDoc.getRootElement().getChild("treatment", SWORD);
		assertNotNull("Missing treatment element", tTreatement);

		Element tVerbose = tEntityDoc.getRootElement().getChild("verboseDescription", SWORD);
		assertNotNull("Missing verbose description element", tVerbose);

		Element tNoOp = tEntityDoc.getRootElement().getChild("noOp", SWORD);
		assertNotNull("Missing no op element", tNoOp);
		assertTrue("No op must have a value of true or false not " + tNoOp.getText(), tNoOp.getText().equals("true") || tNoOp.getText().equals("false"));

		Element tPackaging = tEntityDoc.getRootElement().getChild("packaging", SWORD);
		assertNotNull("Missing packaging element", tPackaging);
	}
	
	private static DOReader getMockReader(String pid) {
		DOReader mock = mock(DOReader.class);
		try {
			when(mock.GetObjectPID()).thenReturn(pid);
		} catch (ServerException e) {
			e.printStackTrace();
		}
		return mock;
	}
	
	public static ServletInputStream getEmptyMockStream() {
		return new ServletInputStream() {
			@Override
			public int read(byte[] buf) throws IOException {
				return -1;
			}
			@Override
			public int read() throws IOException {
				return -1;
			}
		};
	}
	
	public static ServletInputStream getMockStream() {
		final InputStream stream = IntegrationTests.class.getResourceAsStream("logo.jpg");
		return new ServletInputStream() {
			@Override
			public int read(byte[] buf) throws IOException {
				return stream.read(buf);
			}
			@Override
			public int read() throws IOException {
				return stream.read();
			}
		};
	}
	
	public static Entry getMockEntry() {
    	Link link = new Link(URI.create("http://localhost:8080/expected"), "edit");
    	final ArrayList<Link> links = new ArrayList<Link>(1);
    	links.add(link);
    	return new Entry(){
    		@Override
    		public List<Link> getLinks() {
    			return links;
    		}
    		@Override
    		public String getId() {
    			return "entry_id";
    		}
    	};
	}


}