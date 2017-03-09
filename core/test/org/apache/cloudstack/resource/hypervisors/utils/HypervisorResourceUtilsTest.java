package org.apache.cloudstack.resource.hypervisors.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloud.agent.api.CopyFileInVmAnswer;
import com.cloud.agent.api.CopyFileInVmCommand;
import com.cloud.utils.ssh.SshHelper;

@RunWith(PowerMockRunner.class)
public class HypervisorResourceUtilsTest {

	@Test
	@PrepareForTest({HypervisorResourceUtils.class})
	public void copyFileInVmTestSourceFileDoesNotExist() throws Exception {
		executeCopyFileInVmTest(false);
	}
	
	@Test
	@PrepareForTest({HypervisorResourceUtils.class})
	public void copyFileInVmTestSourceFileExist() throws Exception {
		executeCopyFileInVmTest(true);
	}

	private void executeCopyFileInVmTest(boolean doesFileExist) throws Exception {
		CopyFileInVmCommand copyFileInVmCommandMock = Mockito.mock(CopyFileInVmCommand.class);

		File sshKeyMockFile = Mockito.mock(File.class);
		File sourceFileMock = Mockito.mock(File.class);
		
		String sourcePathForCommand = "fullQualifiedSourceFilePath";
		Mockito.when(copyFileInVmCommandMock.getSrc()).thenReturn(sourcePathForCommand);
		
		
		PowerMockito.mockStatic(File.class, HypervisorResourceUtils.class );
		Mockito.when(sourceFileMock.exists()).thenReturn(doesFileExist);
		
		
		PowerMockito.whenNew(File.class).withArguments(sourcePathForCommand).thenReturn(sourceFileMock);
		PowerMockito.when(HypervisorResourceUtils.executeFileCopiesAndReturnResponse(Mockito.eq(copyFileInVmCommandMock),  Mockito.eq(sshKeyMockFile), Mockito.eq(sourceFileMock))).thenReturn(new CopyFileInVmAnswer());

		PowerMockito.when(HypervisorResourceUtils.copyFileInVm(copyFileInVmCommandMock, sshKeyMockFile)).thenCallRealMethod();
		
		CopyFileInVmAnswer copyFileInVmCommandAnswer =  HypervisorResourceUtils.copyFileInVm(copyFileInVmCommandMock, sshKeyMockFile);
		
		Mockito.verify(sourceFileMock).exists();
		PowerMockito.verifyStatic(Mockito.times(doesFileExist ? 1 : 0));
		HypervisorResourceUtils.executeFileCopiesAndReturnResponse(Mockito.any(CopyFileInVmCommand.class),  Mockito.any(File.class), Mockito.any(File.class));
		
		Assert.assertTrue(copyFileInVmCommandAnswer.getResult());
	}
	
	@Test
	@PrepareForTest({SshHelper.class, FileUtils.class})
	public void executeFileCopiesAndReturnResponseTestForSingleFile() throws IOException{
		configureAndExecuteTestsForExecuteFileCopiesAndReturnResponse(1);
	}
	
	@Test
	@PrepareForTest({SshHelper.class, FileUtils.class})
	public void executeFileCopiesAndReturnResponseTestForFolder() throws IOException{
		configureAndExecuteTestsForExecuteFileCopiesAndReturnResponse(3);
	}

	private void configureAndExecuteTestsForExecuteFileCopiesAndReturnResponse(int numberOfFilesExpectedtoBeSendToVm) throws IOException {
		String vmIp = "VM-IP";
		String canonicalPath = "canonicalPath";
		String fileDestination = "fileDestination";
		boolean isDirectory = numberOfFilesExpectedtoBeSendToVm > 1;

		CopyFileInVmCommand CopyFileInVmCommandMock = Mockito.mock(CopyFileInVmCommand.class);
		File fileToBeCopiedMock = Mockito.mock(File.class);
		File vmSshKeyFileMock = Mockito.mock(File.class);
		
		PowerMockito.mockStatic(SshHelper.class, FileUtils.class);
		Mockito.when(fileToBeCopiedMock.isDirectory()).thenReturn(isDirectory);
		Mockito.when(fileToBeCopiedMock.getCanonicalPath()).thenReturn(canonicalPath);
		Mockito.when(CopyFileInVmCommandMock.getVmIp()).thenReturn(vmIp);
		Mockito.when(CopyFileInVmCommandMock.getDest()).thenReturn(fileDestination);
		if(isDirectory){
			List<File> filesMockThatAreInThefolder = createMockFilesInFolder(numberOfFilesExpectedtoBeSendToVm);
			PowerMockito.when(FileUtils.listFiles(fileToBeCopiedMock, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)).thenReturn(filesMockThatAreInThefolder);
		}
		CopyFileInVmAnswer executeFileCopiesAndReturnResponse = HypervisorResourceUtils.executeFileCopiesAndReturnResponse(CopyFileInVmCommandMock, vmSshKeyFileMock, fileToBeCopiedMock);
		
		Assert.assertTrue(executeFileCopiesAndReturnResponse.getResult());
		if(!isDirectory){
			PowerMockito.verifyStatic(Mockito.times(numberOfFilesExpectedtoBeSendToVm));
			SshHelper.scpTo(vmIp, 3922, "root", vmSshKeyFileMock, null, fileDestination, canonicalPath, null);
		} else{
			PowerMockito.verifyStatic(Mockito.times(1));
			FileUtils.listFiles(fileToBeCopiedMock, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			PowerMockito.verifyStatic(Mockito.times(numberOfFilesExpectedtoBeSendToVm));
			SshHelper.scpTo(Mockito.eq(vmIp), Mockito.eq(3922), Mockito.eq("root"), Mockito.eq(vmSshKeyFileMock), Mockito.any(), Mockito.eq(fileDestination), Mockito.any(), Mockito.any());
		}
	}

	private List<File> createMockFilesInFolder(int numberOfFilesExpectedtoBeSendToVm) throws IOException {
		List<File> filesMockThatAreInThefolder = new ArrayList<>();
		for(int i = 0; i < numberOfFilesExpectedtoBeSendToVm; i++){
			File mockFile = Mockito.mock(File.class);
			Mockito.when(mockFile.getCanonicalPath()).thenReturn(""+i);
			filesMockThatAreInThefolder.add(mockFile);
		}
		return filesMockThatAreInThefolder;
	}

}
