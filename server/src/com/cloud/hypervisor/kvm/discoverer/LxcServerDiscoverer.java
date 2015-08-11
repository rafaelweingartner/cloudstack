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
package com.cloud.hypervisor.kvm.discoverer;

import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.Discoverer;
import com.cloud.utils.exception.CloudRuntimeException;

import org.apache.log4j.Logger;

import javax.ejb.Local;

@Local(value=Discoverer.class)
public class LxcServerDiscoverer extends LibvirtServerDiscoverer {
    private static final Logger s_logger = Logger.getLogger(LxcServerDiscoverer.class);

    public Hypervisor.HypervisorType getHypervisorType() {
        return Hypervisor.HypervisorType.LXC;
    }

	@Override
	public void shutDownHost(HostVO host) {
		if (host.getType() != Host.Type.Routing || host.getHypervisorType() != HypervisorType.LXC) {
            return ;
        }
		throw new CloudRuntimeException("Shut down Host not implemented for LXC");
	}

}
