package at.reisisoft.sigui.commons.installation

class WindowsInstallException(val errorCode: Int) : Exception("msiexec returned with errorcode $errorCode")