package edu.columbia.cul.sword.holder;


public class WebContextStructure  {
	
	public String uploadTempDirectory;
	public String maxUploadSize;
	public String authenticationMethod;
	
	public String propertyConfigLocation;
	public String fedoraHome;
	public String contextConfigLocation;

} // ====================================================== //
	

//String tempDirectory = m_context.getInitParameter("upload-temp-directory");
//
//if ((tempDirectory == null) || (tempDirectory.equals(""))) {
//	tempDirectory = System.getProperty("java.io.tmpdir");
//}
//
//if (!tempDirectory.endsWith(System.getProperty("file.separator"))){
//	tempDirectory += System.getProperty("file.separator");
//}
//
//m_tempDir = new File(tempDirectory);
//LOGGER.info("Upload temporary directory set to: {}", m_tempDir.getPath());
//
//if (!m_tempDir.exists()) {
//	if (!m_tempDir.mkdirs()) {
//		LOGGER.error("Upload directory did not exist and I can't create it. {}", m_tempDir.getPath());
//		throw new IllegalArgumentException(
//				"Upload directory did not exist and I can't create it. "
//						+ m_tempDir.getPath());
//	}
//}
//
//if (!m_tempDir.isDirectory()) {
//	LOGGER.error("Upload temporary directory is not a directory: {}", m_tempDir.getPath());
//	throw new IllegalArgumentException(
//			"Upload temporary directory is not a directory: " + m_tempDir.getPath());
//}
//
//if (!m_tempDir.canWrite()) {
//	LOGGER.error("Upload temporary directory cannot be written to: {}", m_tempDir.getPath());
//	throw new IllegalArgumentException(
//			"Upload temporary directory cannot be written to: "
//					+ m_tempDir.getPath());
//}
//
//String maxUploadSizeStr = m_context.getInitParameter("maxUploadSize");
//
//if ((maxUploadSizeStr == null) ||
//        (maxUploadSizeStr.equals("")) ||
//        (maxUploadSizeStr.equals("-1"))) {
//    m_maxUpload = -1;
//    LOGGER.warn("No maxUploadSize set, so setting max file upload size to unlimited.");
//} else {
//    try {
//        m_maxUpload = Integer.parseInt(maxUploadSizeStr);
//        LOGGER.info("Setting max file upload size to " + m_maxUpload);
//    } catch (NumberFormatException nfe) {
//        m_maxUpload = -1;
//        LOGGER.warn("maxUploadSize not a number, so setting max file upload size to unlimited.");
//    }
//}
