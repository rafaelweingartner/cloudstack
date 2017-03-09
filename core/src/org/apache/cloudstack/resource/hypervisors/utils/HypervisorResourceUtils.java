package org.apache.cloudstack.resource.hypervisors.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import com.cloud.agent.api.CopyFileInVmAnswer;
import com.cloud.agent.api.CopyFileInVmCommand;
import com.cloud.utils.ssh.SshHelper;

/**
 * This class provides utilitarian methods for hypervisors managed by Apache CloudStack.
 */
public class HypervisorResourceUtils {

	protected static Logger logger = Logger.getLogger(HypervisorResourceUtilsTest.class);
	
	/**
	 * This method copies a file or a set of files contained in the source specified in the {@link CopyFileInVmCommand#getSrc()}.
	 * If the file defined with {link CopyFileInVmCommand#getSrc()} does not exist, we log debug message saying that the file was not copied because it does not exist; then, we return a {@link CopyFileInVmAnswer} with a message saying that the origin does not exist in the file system.
	 * If the file exists, we proceed executing the method {@link #executeFileCopiesAndReturnResponse(CopyFileInVmCommand, File, File)}, which will execute the copy and return a {@link CopyFileInVmAnswer}.
	 */
	public static CopyFileInVmAnswer copyFileInVm(CopyFileInVmCommand copyFileCommand, File vmSshKeyFile){
		String fileFullQualifiedPath = copyFileCommand.getSrc();
		File fileToBeCopied = new File(fileFullQualifiedPath);
		if(!fileToBeCopied.exists()) {
			  logger.debug(String.format("Not trying to copy file [%s] to VM IP[%s] because it does not exist in the file system. ", fileFullQualifiedPath, copyFileCommand.getVmIp()));
			  return new CopyFileInVmAnswer(copyFileCommand);
		}
		return executeFileCopiesAndReturnResponse(copyFileCommand, vmSshKeyFile, fileToBeCopied);
	}

	/**
	 * This method executes the copy of files to the virtual machine defined in the {@link CopyFileInVmCommand#getVmIp()}.
	 * If the {@link CopyFileInVmCommand#getSrc()} is a directory we proceed listing all file in that directory and then copying all of them to the VM; be aware that we will not do this process recursively.
	 * If the source is a normal file, we simply copy it to the VM as specified in the {@link CopyFileInVmCommand#getDest()}.
	 * In case of exception, the copy is interrupted and a {@link CopyFileInVmAnswer} object is returned with the exception as its details; otherwise a success {@link CopyFileInVmAnswer} is returned .
	 */
	protected static CopyFileInVmAnswer executeFileCopiesAndReturnResponse(CopyFileInVmCommand copyFileCommand, File vmSshKeyFile, File fileToBeCopied) {
		CopyFileInVmAnswer copyFileInVmAnswer = new CopyFileInVmAnswer(copyFileCommand);
		try {
			if(!fileToBeCopied.isDirectory()){
				SshHelper.scpTo(copyFileCommand.getVmIp(), 3922, "root", vmSshKeyFile, null, copyFileCommand.getDest(), fileToBeCopied.getCanonicalPath(), null);
				return copyFileInVmAnswer;
			}
			for (File f : FileUtils.listFiles(fileToBeCopied, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
                SshHelper.scpTo(copyFileCommand.getVmIp(), 3922, "root", vmSshKeyFile, null, copyFileCommand.getDest(), f.getCanonicalPath(), null);
            }
		} catch (IOException e) {
			e.printStackTrace();
			return new CopyFileInVmAnswer(copyFileCommand, e);
		}
		return copyFileInVmAnswer;
	}
}
