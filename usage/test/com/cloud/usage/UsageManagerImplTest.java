// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.usage;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventVO;
import com.cloud.usage.dao.UsageVPNUserDao;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;

@RunWith(MockitoJUnitRunner.class)
public class UsageManagerImplTest {

    @Spy
    @InjectMocks
    private UsageManagerImpl usageManagerImpl;

    @Mock
    private UsageVPNUserDao usageVPNUserDaoMock;

    @Mock
    private AccountDao accountDaoMock;

    @Mock
    private UsageVPNUserVO vpnUserMock;

    @Mock
    private AccountVO accountMock;

    @Mock
    private UsageEventVO usageEventVOMock;

    private long accountMockId = 1l;
    private long acountDomainIdMock = 2l;

    @Before
    public void before() {
        Mockito.when(accountMock.getId()).thenReturn(accountMockId);
        Mockito.when(accountMock.getDomainId()).thenReturn(acountDomainIdMock);

        Mockito.doReturn(accountMock).when(accountDaoMock).findByIdIncludingRemoved(Mockito.anyLong());

    }

    @Test
    public void createUsageVpnUserTestUserExit() {
        List<UsageVPNUserVO> vpnUsersMock = new ArrayList<UsageVPNUserVO>();
        vpnUsersMock.add(vpnUserMock);

        Mockito.doReturn(vpnUsersMock).when(usageManagerImpl).findUsageVPNUsers(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong());
        Mockito.doReturn(vpnUserMock).when(usageVPNUserDaoMock).persist(Mockito.any(UsageVPNUserVO.class));

        usageManagerImpl.createUsageVpnUser(usageEventVOMock);

        Mockito.verify(usageVPNUserDaoMock, Mockito.never()).persist(Mockito.any(UsageVPNUserVO.class));

    }

    @Test
    public void createUsageVpnUserTestUserDoesNotExit() {
        List<UsageVPNUserVO> vpnUsersMock = new ArrayList<UsageVPNUserVO>();

        Mockito.doReturn(vpnUsersMock).when(usageManagerImpl).findUsageVPNUsers(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong());
        Mockito.doReturn(vpnUserMock).when(usageVPNUserDaoMock).persist(Mockito.any(UsageVPNUserVO.class));

        usageManagerImpl.createUsageVpnUser(usageEventVOMock);

        Mockito.verify(usageVPNUserDaoMock, Mockito.times(1)).persist(Mockito.any(UsageVPNUserVO.class));

    }

    @Test
    public void deleteUsageVpnUserNoUserFound() {
        List<UsageVPNUserVO> vpnUsersMock = new ArrayList<UsageVPNUserVO>();

        Mockito.doReturn(vpnUsersMock).when(usageManagerImpl).findUsageVPNUsers(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong());
        Mockito.doNothing().when(usageVPNUserDaoMock).update(Mockito.any(UsageVPNUserVO.class));

        usageManagerImpl.deleteUsageVpnUser(usageEventVOMock);

        Mockito.verify(usageVPNUserDaoMock, Mockito.never()).update(Mockito.any(UsageVPNUserVO.class));
    }

    @Test
    public void deleteUsageVpnUserOneUserFound() {
        List<UsageVPNUserVO> vpnUsersMock = new ArrayList<UsageVPNUserVO>();
        vpnUsersMock.add(vpnUserMock);

        Mockito.doReturn(vpnUsersMock).when(usageManagerImpl).findUsageVPNUsers(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong());
        Mockito.doNothing().when(usageVPNUserDaoMock).update(Mockito.any(UsageVPNUserVO.class));

        usageManagerImpl.deleteUsageVpnUser(usageEventVOMock);

        Mockito.verify(usageVPNUserDaoMock, Mockito.times(1)).update(Mockito.any(UsageVPNUserVO.class));
    }

    @Test
    public void deleteUsageVpnUserMultipleUsersFound() {
        List<UsageVPNUserVO> vpnUsersMock = new ArrayList<UsageVPNUserVO>();
        vpnUsersMock.add(vpnUserMock);
        vpnUsersMock.add(Mockito.mock(UsageVPNUserVO.class));
        vpnUsersMock.add(Mockito.mock(UsageVPNUserVO.class));

        Mockito.doReturn(vpnUsersMock).when(usageManagerImpl).findUsageVPNUsers(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong());
        Mockito.doNothing().when(usageVPNUserDaoMock).update(Mockito.any(UsageVPNUserVO.class));

        usageManagerImpl.deleteUsageVpnUser(usageEventVOMock);

        Mockito.verify(usageVPNUserDaoMock, Mockito.times(3)).update(Mockito.any(UsageVPNUserVO.class));
    }

    @Test
    public void handleVpnUserEventTestAddUser() {
        Mockito.when(this.usageEventVOMock.getType()).thenReturn(EventTypes.EVENT_VPN_USER_ADD);
        Mockito.doNothing().when(this.usageManagerImpl).createUsageVpnUser(usageEventVOMock);

        this.usageManagerImpl.handleVpnUserEvent(usageEventVOMock);

        Mockito.verify(usageManagerImpl).createUsageVpnUser(usageEventVOMock);
        Mockito.verify(usageManagerImpl, Mockito.never()).deleteUsageVpnUser(usageEventVOMock);
    }

    @Test
    public void handleVpnUserEventTestRemoveUser() {
        Mockito.when(this.usageEventVOMock.getType()).thenReturn(EventTypes.EVENT_VPN_USER_REMOVE);
        Mockito.doNothing().when(this.usageManagerImpl).deleteUsageVpnUser(usageEventVOMock);

        this.usageManagerImpl.handleVpnUserEvent(usageEventVOMock);

        Mockito.verify(usageManagerImpl, Mockito.never()).createUsageVpnUser(usageEventVOMock);
        Mockito.verify(usageManagerImpl).deleteUsageVpnUser(usageEventVOMock);
    }

    @Test
    public void handleVpnUserEventTestEventIsNeitherAddNorRemove() {
        Mockito.when(this.usageEventVOMock.getType()).thenReturn("VPN.USER.UPDATE");

        this.usageManagerImpl.handleVpnUserEvent(usageEventVOMock);

        Mockito.verify(usageManagerImpl, Mockito.never()).createUsageVpnUser(usageEventVOMock);
        Mockito.verify(usageManagerImpl, Mockito.never()).deleteUsageVpnUser(usageEventVOMock);
    }
}
