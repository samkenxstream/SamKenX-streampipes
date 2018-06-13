package org.streampipes.rest.impl.connect;

import com.jayway.restassured.RestAssured;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.streampipes.commons.Utils;
import org.streampipes.empire.core.empire.annotation.InvalidRdfException;
import org.streampipes.model.SpDataSet;
import org.streampipes.model.modelconnect.AdapterStreamDescription;
import org.streampipes.model.schema.EventSchema;
import org.streampipes.rest.Mock;
import org.streampipes.rest.TestUtil;
import org.streampipes.serializers.jsonld.JsonLdTransformer;
import org.streampipes.storage.couchdb.impl.AdapterStorageImpl;

import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

public class SpConnectResourceTest {

    SpConnectResource spConnectResource;
    Server server;

    @Before
    public  void before() {

        spConnectResource = new SpConnectResource();
        RestAssured.port = Mock.PORT;

        ResourceConfig config = new ResourceConfig().register(spConnectResource);

        URI baseUri = UriBuilder
                .fromUri(Mock.HOST)
                .build();

        server = JettyHttpContainerFactory.createServer(baseUri, config);
    }

    @After
    public void after() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addAdapterSuccess() {

        // Mock adatper management
        SpConnect spConnect = mock(SpConnect.class);
        org.mockito.Mockito.when(spConnect.addAdapter(any(AdapterStreamDescription.class), any(String.class)))
                .thenReturn(SpConnectUtils.SUCCESS);
        spConnectResource.setSpConnect(spConnect);

        // perform test
        String data = TestUtil.getMinimalStreamAdapterJsonLD();
        given().contentType("application/json").body(data).when()
                .post("v2/adapter").then().assertThat()
                .body("success", equalTo(true))
                .body("optionalMessage", equalTo(""))
                .body("elementId", equalTo("http://test.de/1"));
    }

    @Test
    public void addAdapterFail() {

        String errorMessage = "ERROR";

        SpConnect spConnect = mock(SpConnect.class);
        org.mockito.Mockito.when(spConnect.addAdapter(any(AdapterStreamDescription.class), any(String.class)))
                .thenReturn(errorMessage);
        spConnectResource.setSpConnect(spConnect);

        String data = TestUtil.getMinimalStreamAdapterJsonLD();
        given().contentType("application/json").body(data).when()
                .post("v2/adapter").then().assertThat()
                .body("success", equalTo(false))
                .body("optionalMessage", equalTo(errorMessage))
                .body("elementId", equalTo("http://test.de/1"));
    }

    @Test
    public void invokeAdapterSuccess() {

        SpConnect spConnect = mock(SpConnect.class);
        org.mockito.Mockito.when(spConnect.invokeAdapter(any(String.class), any(SpDataSet.class), any(String.class),
                any(AdapterStorageImpl.class)))
                .thenReturn(SpConnectUtils.SUCCESS);
        spConnectResource.setSpConnect(spConnect);

        String data = TestUtil.getMinimalDataSet();
        given().contentType("application/json").body(data).when()
                .post("v2/adapter/all/1234/streams").then().assertThat()
                .body("success", equalTo(true))
                .body("optionalMessage", equalTo(""))
                .body("elementId", equalTo("1234"));
    }

    @Test
    public void invokeAdapterFail() {

        String errorMessage = "ERROR";

        SpConnect spConnect = mock(SpConnect.class);
        org.mockito.Mockito.when(spConnect.invokeAdapter(any(String.class), any(SpDataSet.class), any(String.class),
                any(AdapterStorageImpl.class)))
                .thenReturn(errorMessage);
        spConnectResource.setSpConnect(spConnect);

        String data = TestUtil.getMinimalDataSet();
        given().contentType("application/json").body(data).when()
                .post("v2/adapter/all/1234/streams").then().assertThat()
                .body("success", equalTo(false))
                .body("optionalMessage", equalTo(errorMessage))
                .body("elementId", equalTo("1234"));
    }

}