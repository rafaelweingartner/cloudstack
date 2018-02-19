//
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
//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.ResizeVolumeAnswer;
import com.cloud.agent.api.storage.ResizeVolumeCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.exception.CloudRuntimeException;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.PBD;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Types.VmPowerState;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VDI;
import com.xensource.xenapi.VM;

@ResourceWrapper(handles =  ResizeVolumeCommand.class)
public final class CitrixResizeVolumeCommandWrapper extends CommandWrapper<ResizeVolumeCommand, Answer, CitrixResourceBase> {
    private static final Logger s_logger = Logger.getLogger(CitrixResizeVolumeCommandWrapper.class);

    @Override
    public Answer execute(final ResizeVolumeCommand command, final CitrixResourceBase citrixResourceBase) {
        if (command.isManaged()) {
            Connection conn = citrixResourceBase.getConnection();
            resizeSr(conn, command);
        }
        try {
            executeVolumeResize(command, citrixResourceBase);
        } catch (XenAPIException | XmlRpcException ex) {
            String errorMsg = "Failed to resize volume:";
            s_logger.warn(errorMsg, ex);
            return new ResizeVolumeAnswer(command, false, errorMsg + ex);
        }
        long newSize = command.getNewSize();
        return new ResizeVolumeAnswer(command, true, "success", newSize);
    }

    private void executeVolumeResize(ResizeVolumeCommand command, CitrixResourceBase citrixResourceBase) throws XenAPIException, XmlRpcException {
        Connection conn = citrixResourceBase.getConnection();
        String volumeUuid = command.getPath();
        long newSize = command.getNewSize();

        VDI vdi = citrixResourceBase.getVDIbyUuid(conn, volumeUuid);
        if (vdi == null) {
            throw new CloudRuntimeException(String.format("The volume [%s] was not found to execute the resize.", volumeUuid));
        }
        if (isOnlineResize(command, conn)) {
            vdi.resizeOnline(conn, newSize);
        } else {
            vdi.resize(conn, newSize);
        }
    }

    private boolean isOnlineResize(ResizeVolumeCommand command, Connection conn) throws XenAPIException, XmlRpcException {
        String instanceName = command.getInstanceName();
        if (StringUtils.isBlank(instanceName)) {
            return false;
        }
        Set<VM> vms = VM.getByNameLabel(conn, instanceName);
        if (CollectionUtils.isEmpty(vms)) {
            return false;
        }
        if (vms.size() > 1) {
            throw new CloudRuntimeException(String.format("There seems to be more than one VM with the same instance name [%s] in the cluster.", instanceName));
        }
        VM vm = vms.iterator().next();
        VmPowerState powerState = vm.getPowerState(conn);
        return powerState == VmPowerState.RUNNING;
    }

    private void resizeSr(Connection conn, ResizeVolumeCommand command) {
        // If this is managed storage, re-size the SR, too.
        // The logical unit/volume has already been re-sized, so the SR needs to fill up the new space.

        String iScsiName = command.get_iScsiName();

        try {
            Set<SR> srs = SR.getByNameLabel(conn, iScsiName);
            Set<PBD> allPbds = new HashSet<>();

            for (SR sr : srs) {
                if (!CitrixResourceBase.SRType.LVMOISCSI.equals(sr.getType(conn))) {
                    continue;
                }

                Set<PBD> pbds = sr.getPBDs(conn);

                if (pbds.size() <= 0) {
                    s_logger.debug("No PBDs found for the following SR: " + sr.getNameLabel(conn));
                }

                allPbds.addAll(pbds);
            }

            for (PBD pbd: allPbds) {
                PBD.Record pbdr = pbd.getRecord(conn);

                if (pbdr.currentlyAttached) {
                    pbd.unplug(conn);
                    pbd.plug(conn);
                }
            }
        }
        catch (Throwable ex) {
            throw new CloudRuntimeException("Unable to resize volume: " +  ex.getMessage());
        }
    }
}
