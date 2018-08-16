import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty
import com.cloudbees.hudson.plugins.folder.AbstractFolder
import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import jenkins.model.*



def call(projectName, token){


	println('echo starting...')
	String id = "vault_token"
	Credentials c = new StringCredentialsImpl(CredentialsScope.GLOBAL, id, "description: Token for passing to library functions", Secret.fromString("$token"))

	def items = Jenkins.instance.getAllItems(Folder.class)

	for (folder in items) {
		if(folder.name == "$projectName-folder") {
			println("executing...")
			FolderCredentialsProperty property = folder.getProperties().get(FolderCredentialsProperty.class)
			property.getStore().addCredentials(Domain.global(), c)
			println("finished!")
		}
	}


}