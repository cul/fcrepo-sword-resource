package edu.columbia.libraries.sword.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.errors.ServerException;
import org.fcrepo.server.rest.DatastreamResource;
import org.fcrepo.server.rest.FedoraObjectsResource;
import org.fcrepo.server.storage.DOManager;
import org.fcrepo.server.storage.DOWriter;
import org.fcrepo.server.storage.types.DigitalObject;
import org.fcrepo.server.utilities.DCFields;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.columbia.libraries.fcrepo.Utils;
import edu.columbia.libraries.sword.DepositHandler;
import edu.columbia.libraries.sword.SWORDException;
import edu.columbia.libraries.sword.SWORDResource;
import edu.columbia.libraries.sword.xml.entry.Entry;
import edu.columbia.libraries.utils.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class})
public class DefaultDepositHandlerTest {
	
	private static String UTF_8 = "UTF-8";
	
	private static final String MOCK_COLLECTION = "mock:collection";
	
	private DefaultDepositHandler testObj;
	
	private Set<String> mockCollectionIds;
	
	private DOManager mockManager;
	
	private UriInfo mockUris;
	
	private UriBuilder mockBase;
	
	private UriBuilder mockDatastreamsBuilder;
	
	private UriBuilder mockObjectsBuilder;
	
	private UriBuilder mockSwordBuilder;

	@Before
	public void setUp() throws ServerException {
		mockManager = mock(DOManager.class);
		mockUris = mock(UriInfo.class);
		mockCollectionIds = new HashSet<String>(1); 
		mockCollectionIds.add(MOCK_COLLECTION);
		mockBase = mock(UriBuilder.class);
		when(mockUris.getBaseUriBuilder()).thenReturn(mockBase);
		mockDatastreamsBuilder = mock(UriBuilder.class);
		mockObjectsBuilder = mock(UriBuilder.class);
		mockSwordBuilder = mock(UriBuilder.class);
		when(mockBase.path(eq(DatastreamResource.class), anyString())).thenReturn(mockDatastreamsBuilder);
		when(mockBase.path(eq(FedoraObjectsResource.class), anyString())).thenReturn(mockObjectsBuilder);
		when(mockBase.path(eq(SWORDResource.class), anyString())).thenReturn(mockSwordBuilder);
		testObj = new DefaultDepositHandler(mockManager, mockCollectionIds);
	}
	
	@Test
	public void testIngestDeposit() throws SWORDException, ServerException {
		String mockPid = "foo:bar";
		DepositRequest mockDeposit = mock(DepositRequest.class);
		// deposit needs a payload
		int payloadSize = 512;
		InputStream payload = TestUtils.getRandomData(payloadSize);
		when(mockDeposit.getFile()).thenReturn(payload);
		when(mockDeposit.getContentLength()).thenReturn(payloadSize);
		when(mockDeposit.getCollection()).thenReturn(MOCK_COLLECTION);
		Context mockContext = mock(Context.class);
		when(mockDeposit.getBaseUri()).thenReturn(mockUris);
		URI mockContentURI = URI.create("info:fedora/foo:bar/content");
		URI mockDescURI = URI.create("info:fedora/foo:bar/describe");
		URI mockDsURI = URI.create("info:fedora/foo:bar/datastream");
		when(mockObjectsBuilder.build(mockPid)).thenReturn(mockContentURI);
		when(mockSwordBuilder.build(MOCK_COLLECTION, mockPid)).thenReturn(mockDescURI);
		when(mockDatastreamsBuilder.build(mockPid, DepositHandler.DEPOSIT_DSID)).thenReturn(mockDsURI);
		// the manager will be asked for a pid
		String [] mockPids = new String[] {mockPid};
		when(mockManager.getNextPID(eq(1), anyString())).thenReturn(mockPids);
		// DOWriter writer = m_mgmt.getIngestWriter(false, context, in, Constants.FOXML1_1.uri, "UTF-8", pid);
		// the manager will be asked for a DOWriter
		DOWriter mockWriter = mock(DOWriter.class);
		when(mockManager.getIngestWriter(eq(false), eq(mockContext), any(InputStream.class), eq(Constants.FOXML1_1.uri), eq(UTF_8), eq(mockPid)))
		    .thenReturn(mockWriter);
		// the DOWriter will be asked for a DigitalObject
		DigitalObject mockObject = mock(DigitalObject.class);
		when(mockWriter.getObject()).thenReturn(mockObject);
		// we'll need to get a DCFields instance
		mockStatic(Utils.class);
		DCFields mockDC = mock(DCFields.class);
		when(Utils.getDCFields(mockWriter)).thenReturn(mockDC);
		Entry actual = testObj.ingestDeposit(mockDeposit, mockContext);
		assertEquals(mockPid, actual.getId());
		assertEquals("text/html", actual.getContent().type);
		// Default handler has no configured packaging
		assertEquals(null, actual.getPackaging());
		verify(mockWriter).commit(anyString());
	}
	
}
