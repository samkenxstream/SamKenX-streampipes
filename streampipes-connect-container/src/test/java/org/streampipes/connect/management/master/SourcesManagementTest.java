/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.streampipes.connect.management.master;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.streampipes.connect.exception.AdapterException;
import org.streampipes.model.SpDataSet;
import org.streampipes.model.connect.adapter.AdapterDescription;
import org.streampipes.model.connect.adapter.AdapterSetDescription;
import org.streampipes.model.connect.adapter.GenericAdapterSetDescription;
import org.streampipes.storage.couchdb.impl.AdapterStorageImpl;

import java.util.Arrays;

import static groovy.xml.Entity.iexcl;
import static groovy.xml.Entity.times;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ WorkerRestClient.class })
public class SourcesManagementTest {
    @Before
    public  void before() {
        PowerMockito.mockStatic(WorkerRestClient.class);
    }

    @Test
    public void addAdapterSuccess() throws Exception {
        AdapterStorageImpl adapterStorage = mock(AdapterStorageImpl.class);
        when(adapterStorage.getAdapter(anyString())).thenReturn(new GenericAdapterSetDescription());
        SourcesManagement sourcesManagement = new SourcesManagement(adapterStorage);
        doNothing().when(WorkerRestClient.class, "invokeSetAdapter", anyString(), any());

        sourcesManagement.addAdapter("/", "id", new SpDataSet());

        verify(adapterStorage, times(1)).getAdapter(anyString());
        verifyStatic(WorkerRestClient.class, times(1));
        WorkerRestClient.invokeSetAdapter(eq("/"), any());

    }

    @Test(expected = AdapterException.class)
    public void addAdapterFail() throws Exception {
        AdapterStorageImpl adapterStorage = mock(AdapterStorageImpl.class);
        when(adapterStorage.getAdapter(anyString())).thenReturn(new GenericAdapterSetDescription());
        SourcesManagement sourcesManagement = new SourcesManagement(adapterStorage);

        org.powermock.api.mockito.PowerMockito.doThrow(new AdapterException()).when(WorkerRestClient.class, "stopSetAdapter", anyString(), any());

        sourcesManagement.detachAdapter("/", "id0", "id1");
    }

    @Test
    public void detachAdapterSuccess() throws Exception {
        AdapterStorageImpl adapterStorage = mock(AdapterStorageImpl.class);
        when(adapterStorage.getAdapter(anyString())).thenReturn(new GenericAdapterSetDescription());
        SourcesManagement sourcesManagement = new SourcesManagement(adapterStorage);
        doNothing().when(WorkerRestClient.class, "stopSetAdapter", anyString(), any());

        sourcesManagement.detachAdapter("/", "id0", "id1");

        verify(adapterStorage, times(1)).getAdapter(anyString());
        verifyStatic(WorkerRestClient.class, times(1));
        WorkerRestClient.stopSetAdapter(eq("/"), any());
    }

    @Test(expected = AdapterException.class)
    public void detachAdapterFail() throws Exception {
        AdapterStorageImpl adapterStorage = mock(AdapterStorageImpl.class);
        when(adapterStorage.getAdapter(anyString())).thenReturn(new GenericAdapterSetDescription());
        SourcesManagement sourcesManagement = new SourcesManagement(adapterStorage);
        org.powermock.api.mockito.PowerMockito.doThrow(new AdapterException()).when(WorkerRestClient.class, "stopSetAdapter", anyString(), any());

        sourcesManagement.detachAdapter("/", "id0", "id1");
    }

    @Test
    public void getAllAdaptersInstallDescriptionSuccess() throws Exception {
        AdapterDescription adapterDescription = new GenericAdapterSetDescription();
        adapterDescription.setId("1234");
        AdapterStorageImpl adapterStorage = mock(AdapterStorageImpl.class);
        when(adapterStorage.getAllAdapters()).thenReturn(Arrays.asList(adapterDescription));
        SourcesManagement sourcesManagement = new SourcesManagement(adapterStorage);
        sourcesManagement.setConnectHost("host");

        String result = sourcesManagement.getAllAdaptersInstallDescription("user@fzi.de");
        assertEquals(getJsonString(), result);

    }

    @Test(expected = AdapterException.class)
    public void getAllAdaptersInstallDescriptionFail() throws Exception {
        AdapterStorageImpl adapterStorage = mock(AdapterStorageImpl.class);
        when(adapterStorage.getAllAdapters()).thenReturn(Arrays.asList(new GenericAdapterSetDescription()));
        SourcesManagement sourcesManagement = new SourcesManagement(adapterStorage);
        sourcesManagement.setConnectHost("host");

        sourcesManagement.getAllAdaptersInstallDescription(" ");

    }


    private String getJsonString() {
        return "[" +
                "{" +
                "\"uri\":\"http://host/streampipes-connect/api/v1/user@fzi.de/master/adapters/1234\"," +
                "\"name\":\"Adapter Stream\"," +
                "\"description\":\"This stream is generated by an StreamPipes Connect adapter. ID of adapter: 1234\"," +
                "\"type\":\"source\"," +
                "\"streams\":[" +
                "{" +
                "\"uri\":\"http://host/streampipes-connect/api/v1/user@fzi.de/master/adapters/1234\"," +
                "\"name\":\"GenericAdapterSetDescription\"," +
                "\"description\":\"\"," +
                "\"type\":\"set\"" +
                "}" +
                "]" +
                "}" +
                "]";
    }
}